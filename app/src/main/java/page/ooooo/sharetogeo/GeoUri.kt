package page.ooooo.sharetogeo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

import java.net.HttpURLConnection
import java.net.URL

fun isGoogleMapsShortUri(uriString: String): Boolean =
    Regex("^https://maps.app.goo.gl/.+").matches(uriString)

fun googleMapsUriToGeoUri(uriString: String): String? {
    val pattern =
        Regex(
            "^https://www.google.com/maps/(place/([^/]+)/)?" +
                "(@(-?\\d{1,2}(\\.\\d{1,10})?),(-?\\d{1,3}(\\.\\d{1,10})?),(\\d{1,2}(\\.\\d{1,10})?)z)?.*"
        )
    val m = pattern.matchEntire(uriString) ?: return null
    if (m.groups[3] != null) {
        val lat = m.groups[4]!!.value.toDouble()
        val lon = m.groups[6]!!.value.toDouble()
        val z = max(1, min(21, m.groups[8]!!.value.toDouble().roundToInt()))
        return "geo:$lat,$lon?z=$z"
    }
    if (m.groups[2] !== null) {
        val q = m.groups[2]!!.value
        return "geo:0,0?q=$q"
    }
    return null
}

suspend fun requestLocationHeader(url: URL): String? =
    withContext(Dispatchers.IO) {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.instanceFollowRedirects = false
        try {
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                return@withContext connection.getHeaderField("Location")
            }
        } finally {
            connection.disconnect()
        }
        return@withContext null
    }
