package page.ooooo.geoshare

import androidx.compose.ui.util.fastJoinToString
import java.net.URL
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GoogleMapsParser(
    private val log: ILog = DefaultLog(),
    private val uriQuote: UriQuote = DefaultUriQuote()
) {

    val hostRegex =
        Regex("^((www|maps)\\.)?google(\\.[a-z]{2,3})?\\.[a-z]{2,3}$")
    val coordinatesRegex =
        Regex("^(-?\\d{1,2}(\\.\\d{1,10})?),(-?\\d{1,3}(\\.\\d{1,10})?).*$")
    val placeRegexes = arrayOf(
        Regex("^/maps/@.*[?&]center=([^&]+).*$"),
        Regex("^/maps/@.*[?&]viewpoint=([^&]+).*$"),
        Regex("^/maps/@([^/]+).*$"),
        Regex("^/maps/place/[^/]+/@([^/]+).*$"),
        Regex("^/maps/place/([^/]+).*$"),
        Regex("^/maps/search/.*[?&]query=([^&]+).*$"),
        Regex("^/maps/search/([^/]+).*$"),
        Regex("^/maps/dir/.*[?&]destination=([^&]+).*$"),
        Regex("^/maps/dir/.*/([^/]+)/data[^/]*$"),
        Regex("^/maps/dir/.*/([^/]+)$")
    )
    val zoomRegexes = arrayOf(
        Regex("/@[\\d.,-]+,(\\d{1,2}(\\.\\d{1,10})?)z"),
        Regex("[?&]zoom=(\\d{1,2}(\\.\\d{1,10})?)"),
    )

    fun isShortUrl(url: URL): Boolean =
        (url.protocol == "http" || url.protocol == "https") && url.host == "maps.app.goo.gl" && (url.path?.length
            ?: 0) > 2

    fun toGeoUri(url: URL): String? {
        if (url.protocol != "http" && url.protocol != "https") {
            log.w(null, "Unknown protocol in Google Maps URL $url")
            return null
        }
        if (!hostRegex.matches(url.host)) {
            log.w(null, "Unknown host in Google Maps URL $url")
            return null
        }
        val pathAndQuery = (url.path ?: "") + (url.query ?: "")
        if (pathAndQuery.isEmpty()) {
            log.w(null, "Missing both path and query in Google Maps URL $url")
            return null
        }
        val placeMatch = placeRegexes.firstNotNullOfOrNull {
            it.matchEntire(pathAndQuery)
        }
        if (placeMatch == null) {
            log.w(null, "Failed to parse Google Maps URL $url")
            return null
        }
        val place = uriQuote.decode(placeMatch.groups[1]!!.value)
        val lat: String
        val lon: String
        val params = hashMapOf<String, String>()
        val coordinatesMatch = coordinatesRegex.matchEntire(place)
        if (coordinatesMatch != null) {
            lat = coordinatesMatch.groups[1]!!.value
            lon = coordinatesMatch.groups[3]!!.value
        } else {
            lat = "0"
            lon = "0"
            params["q"] = place
        }
        val zoomMatch = zoomRegexes.firstNotNullOfOrNull {
            it.find(pathAndQuery)
        }
        if (zoomMatch != null) {
            params["z"] = max(
                1,
                min(
                    21,
                    zoomMatch.groups[1]!!.value.toDouble().roundToInt()
                )
            ).toString()
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
