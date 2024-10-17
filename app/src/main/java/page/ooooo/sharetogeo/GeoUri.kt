package page.ooooo.sharetogeo

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

fun requestLocationHeader(url: URL): String? {
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "HEAD"
    try {
        connection.connect()
        return if (connection.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            connection.getHeaderField("Location")
        } else {
            null
        }
    } finally {
        connection.disconnect()
    }
    return null
}
