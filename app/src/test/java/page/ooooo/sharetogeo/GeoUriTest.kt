package page.ooooo.sharetogeo

import org.junit.Assert.assertEquals

import org.junit.Test
import org.mockito.Mockito
import java.net.HttpURLConnection
import java.net.URL

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

    @Test
    fun googleMapsUriToGeoUri_query() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            "geo:0,0?q=Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland"
        )
    }

    /*
    @Test
    fun requestLocationHeader_302Response() {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        assertEquals(
            requestLocationHeader("https://maps.app.goo.gl/foobar?g_st=ac"),
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
        )
    }

    @Test
    fun requestLocationHeader_302ResponseMissingLocation() {
        // TODO
    }

    @Test
    fun requestLocationHeader_200Response() {
        // TODO
    }

    @Test
    fun requestLocationHeader_500Response() {
        // TODO
    }
    */
}
