package page.ooooo.geoshare

import java.net.URL
import java.util.regex.Pattern

class GoogleMapsUrlConverter(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote()
) {

    val hostPattern =
        """^((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}$""".toPattern()
    val coordRegex =
        """\+?(?<lat>-?\d{1,2}(\.\d{1,15})?),[+\s]?(?<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val coordPattern = coordRegex.toPattern()
    val dataCoordRegex =
        """!3d(?<lat>-?\d{1,2}(\.\d{1,15})?)!4d(?<lon>-?\d{1,3}(\.\d{1,15})?)"""
    val htmlCoordRegex = """/@$coordRegex""".toPattern()
    val zoomRegex = """(?<z>\d{1,2}(\.\d{1,15})?)"""
    val zoomPattern = zoomRegex.toPattern()
    val queryPattern = """(?<q>.+)""".toPattern()
    val placeRegex = """(?<q>[^/]+)"""
    val pathPatterns = listOf(
        """^/maps/.*/@[\d.,+-]+,${zoomRegex}z/data=.*$dataCoordRegex.*$""".toPattern(),
        """^/maps/.*/data=.*$dataCoordRegex.*$""".toPattern(),
        """^/maps/@$coordRegex,${zoomRegex}z.*$""".toPattern(),
        """^/maps/@$coordRegex.*$""".toPattern(),
        """^/maps/@$""".toPattern(),
        """^/maps/place/$coordRegex/@[\d.,+-]+,${zoomRegex}z.*$""".toPattern(),
        """^/maps/place/$placeRegex/@$coordRegex,${zoomRegex}z.*$""".toPattern(),
        """^/maps/place/$placeRegex/@$coordRegex.*$""".toPattern(),
        """^/maps/place/$coordRegex.*$""".toPattern(),
        """^/maps/place/$placeRegex.*$""".toPattern(),
        """^/maps/search/$coordRegex.*$""".toPattern(),
        """^/maps/search/$placeRegex.*$""".toPattern(),
        """^/maps/search/$""".toPattern(),
        """^/maps/dir/.*/$coordRegex/data[^/]*$""".toPattern(),
        """^/maps/dir/.*/$placeRegex/data[^/]*$""".toPattern(),
        """^/maps/dir/.*/$coordRegex$""".toPattern(),
        """^/maps/dir/.*/$placeRegex$""".toPattern(),
        """^/maps/dir/$""".toPattern(),
        """^/maps$""".toPattern(),
        """^/$""".toPattern(),
    )
    val queryPatterns = hashMapOf<String, List<Pattern>>(
        "center" to listOf(coordPattern),
        "destination" to listOf(coordPattern, queryPattern),
        "q" to listOf(coordPattern),
        "query" to listOf(coordPattern, queryPattern),
        "viewpoint" to listOf(coordPattern),
        "zoom" to listOf(zoomPattern)
    )
    val shortUrlPattern =
        """^https?://(maps\.app\.goo\.gl/|(app\.)?goo\.gl/maps/).+$""".toPattern()

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
        val m = htmlCoordRegex.matcher(html)
        if (!m.find()) {
            log.w(null, "Failed to parse Google Maps HTML document")
            return null
        }
        val geoUriBuilder = GeoUriBuilder(uriQuote = uriQuote)
        geoUriBuilder.fromMatcher(m)
        log.i(null, "Parsed HTML document to $geoUriBuilder")
        return geoUriBuilder
    }
}
