package page.ooooo.sharetogeo

import org.junit.Assert.assertEquals

import org.junit.Test

class GeoUriTest {

    @Test
    fun googleMapsUriToGeoUri_invalidDomain() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.example.com/"),
            null
        )
    }

    @Test
    fun googleMapsUriToGeoUri_positiveCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
            "geo:52.5067296,13.2599309?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_negativeCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"),
            "geo:-17.2165721,-149.9470294?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_intCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"),
            "geo:52.0,13.0?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_fractionalZoom() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"),
            "geo:52.5067296,13.2599309?z=6"
        )
    }
}
