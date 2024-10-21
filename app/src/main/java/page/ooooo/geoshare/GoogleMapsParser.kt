package page.ooooo.geoshare

import android.net.Uri
import androidx.compose.ui.util.fastJoinToString
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

interface UriQuote {
    fun encode(s: String): String
    fun decode(s: String): String
}

class DefaultUriQuote : UriQuote {
    override fun encode(s: String) = Uri.encode(s)
    override fun decode(s: String) = Uri.decode(s)
}

class GoogleMapsParser(private val uriQuote: UriQuote) {

    val urlRegex =
        Regex("^https?://((www|maps)\\.)?google(\\.[a-z]{2,3})?\\.[a-z]{2,3}(/.*)$")
    val shortUrlRegex = Regex("^https?://maps.app.goo.gl/.+$")
    val coordinatesRegex =
        Regex("^(-?\\d{1,2}(\\.\\d{1,10})?),(-?\\d{1,3}(\\.\\d{1,10})?).*$")
    val zoomRegexes = arrayOf(
        Regex("/@[\\d.,-]+,(\\d{1,2}(\\.\\d{1,10})?)z"),
        Regex("[?&]zoom=(\\d{1,2}(\\.\\d{1,10})?)"),
    )
    val pathRegexes = arrayOf(
        Regex("^/maps/@.*[?&]center=([^&]+).*$"),
        Regex("^/maps/@.*[?&]viewpoint=([^&]+).*$"),
        Regex("^/maps/@([^/]+).*$"),
        Regex("^/maps/place/[^/]+/@([^/]+).*$"),
        Regex("^/maps/place/([^/]+).*$"),
        Regex("^/maps/search/.*[?&]query=([^&]+).*$"),
        Regex("^/maps/search/([^/]+).*$"),
        Regex("^/maps/dir/.*[?&]destination=([^&]+).*$"),
        Regex("^/maps/dir/.*/([^/]+)$")
    )

    fun isShortUrl(urlString: String): Boolean =
        shortUrlRegex.matches(urlString)

    fun toGeoUri(urlString: String): String? {
        val urlMatch = urlRegex.matchEntire(urlString) ?: return null
        val path = urlMatch.groups[4]!!.value
        println(path)
        val pathMatch =
            pathRegexes.firstNotNullOfOrNull { it.matchEntire(path) }
                ?: return null
        val coordinatesOrQuery = uriQuote.decode(pathMatch.groups[1]!!.value)
        println("$urlString > $coordinatesOrQuery")
        val lat: String
        val lon: String
        val params = hashMapOf<String, String>()
        val coordinatesMatch = coordinatesRegex.matchEntire(coordinatesOrQuery)
        if (coordinatesMatch != null) {
            lat = coordinatesMatch.groups[1]!!.value
            lon = coordinatesMatch.groups[3]!!.value
        } else {
            lat = "0"
            lon = "0"
            params["q"] = coordinatesOrQuery
        }
        val zoomMatch = zoomRegexes.firstNotNullOfOrNull { it.find(urlString) }
        if (zoomMatch != null) {
            params["z"] = max(
                1,
                min(
                    21,
                    zoomMatch.groups[1]!!.value.toDouble().roundToInt()
                )
            ).toString()
        }
        return "geo:$lat,$lon" + if (params.isNotEmpty()) {
            "?" + params.map {
                "${it.key}=${uriQuote.encode(it.value.replace('+', ' '))}"
            }.fastJoinToString("&")
        } else ""
    }
}
