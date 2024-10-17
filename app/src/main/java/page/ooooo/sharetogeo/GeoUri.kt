package page.ooooo.sharetogeo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

import java.net.HttpURLConnection
import java.net.URL

fun googleMapsUriToGeoUri(uriString: String): String? {
    val pattern =
        Regex(
            "^https://www.google.com/maps/place/([^/]+)/" +
                "(@(-?\\d{1,2}(\\.\\d{1,10})?),(-?\\d{1,3}(\\.\\d{1,10})?),(\\d{1,2}(\\.\\d{1,10})?)z/)?.*"
        )
    val m = pattern.matchEntire(uriString) ?: return null
    if (m.groups[2] != null) {
        val lat = m.groups[3]!!.value.toDouble()
        val lon = m.groups[5]!!.value.toDouble()
        val z = max(1, min(21, m.groups[7]!!.value.toDouble().roundToInt()))
        return "geo:$lat,$lon?z=$z"
    }
    val q = m.groups[1]!!.value
    return "geo:0,0?q=$q"
}

suspend fun requestLocationHeader(url: URL): String? =
    withContext(Dispatchers.IO) {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.instanceFollowRedirects = false  // TODO Test
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
