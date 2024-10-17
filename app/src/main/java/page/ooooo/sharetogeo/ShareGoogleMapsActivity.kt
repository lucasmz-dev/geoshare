package page.ooooo.sharetogeo

class ShareGoogleMapsActivity : BaseShareActivity() {
    override suspend fun getGeoUri(uriString: String): String? =
        googleMapsUriToGeoUri(uriString)
}
