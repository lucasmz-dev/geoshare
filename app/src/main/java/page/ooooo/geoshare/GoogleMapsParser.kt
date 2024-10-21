package page.ooooo.geoshare

import androidx.compose.ui.util.fastJoinToString
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GoogleMapsParser(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote()
) {

    val hostPattern =
        """^((www|maps)\.)?google(\.[a-z]{2,3})?\.[a-z]{2,3}$""".toPattern()
    val coordRegex =
        """(?<lat>[+-]?\d{1,2}(\.\d{1,10})?),(?<lon>[+-]?\d{1,3}(\.\d{1,10})?)"""
    val coordPattern = coordRegex.toPattern()
    val zoomRegex = """(?<z>\d{1,2}(\.\d{1,10})?)"""
    val zoomPattern = zoomRegex.toPattern()
    val queryPattern = """(?<q>.+)""".toPattern()
    val placeRegex = """(?<q>[^/]+)"""
    val pathPatterns = arrayOf(
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
    )
    val queryPatterns = hashMapOf<String, Array<Pattern>>(
        "center" to arrayOf(coordPattern),
        "destination" to arrayOf(coordPattern, queryPattern),
        "query" to arrayOf(coordPattern, queryPattern),
        "viewpoint" to arrayOf(coordPattern),
        "zoom" to arrayOf(zoomPattern)
    )

    private fun matchGroupOrNull(m: Matcher, name: String): String? =
        try {
            m.group(name)
        } catch (_: IllegalArgumentException) {
            null
        }

    fun isShortUrl(url: URL): Boolean =
        (url.protocol == "http" || url.protocol == "https") && url.host == "maps.app.goo.gl" && (url.path?.length
            ?: 0) > 2

    fun toGeoUri(url: URL): String? {
        if (url.protocol != "http" && url.protocol != "https") {
            log.w(null, "Unknown protocol in Google Maps URL $url")
            return null
        }
        if (!hostPattern.matcher(url.host).matches()) {
            log.w(null, "Unknown host in Google Maps URL $url")
            return null
        }
        val path = url.path
        if (path == null) {
            log.w(null, "Missing both path and query in Google Maps URL $url")
            return null
        }
        val m = pathPatterns.firstNotNullOfOrNull {
            val m = it.matcher(path)
            if (m.matches()) m else null
        }
        if (m == null) {
            log.w(null, "Failed to parse Google Maps URL $url")
            return null
        }
        var lat = matchGroupOrNull(m, "lat") ?: "0"
        var lon = matchGroupOrNull(m, "lon") ?: "0"
        val params = hashMapOf<String, String>()
        val rawZ = matchGroupOrNull(m, "z")
        if (rawZ != null) {
            params["z"] =
                max(1, min(21, rawZ.toDouble().roundToInt())).toString()
        }
        val rawQ = matchGroupOrNull(m, "q")
        if (rawQ != null) {
            params["q"] = uriQuote.decode(rawQ)
        }
        if (url.query != null) {
            for (rawParam in url.query.split('&')) {
                val paramParts = rawParam.split('=')
                val paramName = paramParts.firstOrNull()
                if (paramName == null) {
                    continue
                }
                val rawParamValue = paramParts.drop(1).firstOrNull()
                if (rawParamValue == null) {
                    continue
                }
                val patterns = queryPatterns.get(paramName)
                if (patterns == null) {
                    continue
                }
                val paramValue = uriQuote.decode(rawParamValue)
                for (pattern in patterns) {
                    val m = pattern.matcher(paramValue)
                    if (m.matches()) {
                        val latParam = matchGroupOrNull(m, "lat")
                        if (latParam != null) {
                            lat = latParam
                        }
                        val lonParam = matchGroupOrNull(m, "lon")
                        if (lonParam != null) {
                            lon = lonParam
                        }
                        val zParam = matchGroupOrNull(m, "z")
                        if (zParam != null) {
                            params["z"] = zParam
                        }
                        val qParam = matchGroupOrNull(m, "q")
                        if (qParam != null) {
                            params["q"] = qParam
                        }
                        break
                    }
                }
            }
        }
        val geoUriString = "geo:$lat,$lon" + if (params.isNotEmpty()) {
            "?" + params.map {
                "${it.key}=${uriQuote.encode(it.value.replace('+', ' '))}"
            }.fastJoinToString("&")
        } else ""
        log.i(null, "Converted $url to $geoUriString")
        return geoUriString
    }
}
