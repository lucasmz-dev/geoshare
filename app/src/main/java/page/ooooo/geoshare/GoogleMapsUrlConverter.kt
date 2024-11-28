package page.ooooo.geoshare

import android.content.Intent
import android.net.Uri
import java.net.URL
import com.google.re2j.Pattern

sealed class GeoUriAction {
    data class Open(val geoUri: Uri) : GeoUriAction()
    data class OpenUnchanged(val geoUri: Uri) : GeoUriAction()
    data class Fail(val message: String) : GeoUriAction()
    class Noop() : GeoUriAction()
}

class GoogleMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote()
) {

    val hostPattern =
        Pattern.compile("""^((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}$""")
    val coordRegex =
        """\+?(?P<lat>-?\d{1,2}(\.\d{1,15})?),[+\s]?(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val coordPattern = Pattern.compile(coordRegex)
    val dataCoordRegex =
        """!3d(?P<lat>-?\d{1,2}(\.\d{1,15})?)!4d(?P<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val zoomRegex = """(?P<z>\d{1,2}(\.\d{1,15})?)"""
    val zoomPattern = Pattern.compile(zoomRegex)
    val queryPattern = Pattern.compile("""(?P<q>.+)""")
    val placeRegex = """(?P<q>[^/]+)"""
    val pathPatterns = listOf(
        Pattern.compile("""^/maps/.*/@[\d.,+-]+,${zoomRegex}z/data=.*$dataCoordRegex.*$"""),
        Pattern.compile("""^/maps/.*/data=.*$dataCoordRegex.*$"""),
        Pattern.compile("""^/maps/@$coordRegex,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/@$coordRegex.*$"""),
        Pattern.compile("""^/maps/@$"""),
        Pattern.compile("""^/maps/place/$coordRegex/@[\d.,+-]+,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex,${zoomRegex}z.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex/@$coordRegex.*$"""),
        Pattern.compile("""^/maps/place/$coordRegex.*$"""),
        Pattern.compile("""^/maps/place/$placeRegex.*$"""),
        Pattern.compile("""^/maps/placelists/list/.*$"""),
        Pattern.compile("""^/maps/search/$coordRegex.*$"""),
        Pattern.compile("""^/maps/search/$placeRegex.*$"""),
        Pattern.compile("""^/maps/search/$"""),
        Pattern.compile("""^/maps/dir/.*/$coordRegex/data[^/]*$"""),
        Pattern.compile("""^/maps/dir/.*/$placeRegex/data[^/]*$"""),
        Pattern.compile("""^/maps/dir/.*/$coordRegex$"""),
        Pattern.compile("""^/maps/dir/.*/$placeRegex$"""),
        Pattern.compile("""^/maps/dir/$"""),
        Pattern.compile("""^/maps$"""),
        Pattern.compile("""^/?$"""),
    )
    val queryPatterns = hashMapOf<String, List<Pattern>>(
        "center" to listOf(coordPattern),
        "destination" to listOf(coordPattern, queryPattern),
        "q" to listOf(coordPattern, queryPattern),
        "query" to listOf(coordPattern, queryPattern),
        "viewpoint" to listOf(coordPattern),
        "zoom" to listOf(zoomPattern)
    )
    val htmlPatterns = listOf(
        Pattern.compile("""/@$coordRegex"""),
        Pattern.compile("""\[null,null,$coordRegex\]"""),
    )
    val shortUrlPattern =
        Pattern.compile("""^https?://(maps\.app\.goo\.gl/|(app\.)?goo\.gl/maps/).+$""")

    fun isShortUrl(url: URL): Boolean =
        shortUrlPattern.matcher(url.toString()).matches()

    fun parseUrl(url: URL): GeoUriBuilder? {
        if (url.protocol != "http" && url.protocol != "https") {
            log.w(null, "Unknown protocol in Google Maps URL $url")
            return null
        }
        if (!hostPattern.matcher(url.host).matches()) {
            log.w(null, "Unknown host in Google Maps URL $url")
            return null
        }
        val rawPath = url.path
        if (rawPath == null) {
            log.w(null, "Missing both path and query in Google Maps URL $url")
            return null
        }
        val path = uriQuote.decode(rawPath)
        val m = pathPatterns.firstNotNullOfOrNull {
            val m = it.matcher(path)
            if (m.matches()) m else null
        }
        if (m == null) {
            log.w(null, "Failed to parse Google Maps URL $url")
            return null
        }
        val geoUriBuilder = GeoUriBuilder(uriQuote = uriQuote)
        geoUriBuilder.fromMatcher(m)
        if (url.query != null) {
            for (rawParam in url.query.split('&')) {
                val paramParts = rawParam.split('=')
                val paramName = paramParts.firstOrNull() ?: continue
                val rawParamValue = paramParts.drop(1).firstOrNull() ?: continue
                val patterns = queryPatterns.get(paramName) ?: continue
                val paramValue = uriQuote.decode(rawParamValue)
                val m = patterns.firstNotNullOfOrNull {
                    val m = it.matcher(paramValue)
                    if (m.matches()) m else null
                } ?: continue
                geoUriBuilder.fromMatcher(m)
            }
        }
        log.i(null, "Converted $url to $geoUriBuilder")
        return geoUriBuilder
    }

    fun parseHtml(html: String): GeoUriBuilder? {
        val m = htmlPatterns.firstNotNullOfOrNull {
            val m = it.matcher(html)
            if (m.find()) m else null
        }
        if (m == null) {
            log.w(null, "Failed to parse Google Maps HTML document")
            return null
        }
        val geoUriBuilder = GeoUriBuilder(uriQuote = uriQuote)
        geoUriBuilder.fromMatcher(m)
        log.i(null, "Parsed HTML document to $geoUriBuilder")
        return geoUriBuilder
    }

    suspend fun processIntent(
        intent: Intent,
        networkTools: NetworkTools
    ): GeoUriAction {
        val intentGeoUri = getIntentGeoUri(intent)
        if (intentGeoUri != null) {
            return GeoUriAction.OpenUnchanged(intentGeoUri)
        }
        val intentUrl = getIntentUrl(intent) ?: return GeoUriAction.Noop()
        val url = if (isShortUrl(intentUrl)) {
            networkTools.requestLocationHeader(intentUrl)
                ?: return GeoUriAction.Fail("Failed to resolve short link")
        } else {
            intentUrl
        }
        val geoUriBuilderFromUrl = parseUrl(url)
            ?: return GeoUriAction.Fail("Failed to create geo: link")
        val geoUriBuilder =
            if (geoUriBuilderFromUrl.coords.lat != "0" || geoUriBuilderFromUrl.coords.lon != "0") {
                geoUriBuilderFromUrl
            } else {
                val html = networkTools.getText(url)
                    ?: return GeoUriAction.Fail("Failed to fetch Google Maps page")
                val geoUriBuilderFromHtml = parseHtml(html)
                geoUriBuilderFromHtml ?: geoUriBuilderFromUrl
            }
        return GeoUriAction.Open(Uri.parse(geoUriBuilder.toString()))
    }
}
