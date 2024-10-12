package page.ooooo.sharetogeo

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun googleMapsUriToGeoUri(uri: String): String? {
    // TODO Support URLs like https://maps.app.goo.gl/eukZjpeYrrvX3tDw6?g_st=ac
    val pattern =
        Regex(
            "^https://www.google.com/maps/place/[^/]+/" +
                "@(?<lat>-?\\d{1,2}(\\.\\d{1,10})?)," +
                "(?<lon>-?\\d{1,3}(\\.\\d{1,10})?)," +
                "(?<z>\\d{1,2}(\\.\\d{1,10})?)z/.*"
        )
    val m = pattern.matchEntire(uri) ?: return null
    val lat = m.groups[1]!!.value.toDouble()
    val lon = m.groups[3]!!.value.toDouble()
    val z = max(1, min(21, m.groups[5]!!.value.toDouble().roundToInt()))
    return "geo:$lat,$lon?z=$z"
}
