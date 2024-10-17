package page.ooooo.sharetogeo

import java.net.URL

class ShareGoogleMapsShortActivity : BaseShareActivity() {
    override suspend fun getGeoUri(uriString: String): String? {
        val location = requestLocationHeader(URL(uriString))
        if (location != null) {
            return googleMapsUriToGeoUri(location)
        }
        return null
    }
}
