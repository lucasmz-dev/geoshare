package page.ooooo.geoshare

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals

import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.net.HttpURLConnection
import java.net.URL

class GeoUriTest {

    @Test
    fun googleMapsUriToGeoUri_unknownDomain() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.example.com/"),
            null
        )
    }

    @Test
    fun googleMapsUriToGeoUri_coordinatesOnly() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/@52.5067296,13.2599309,6z"),
            "geo:52.5067296,13.2599309?z=6"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndPositiveCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
            "geo:52.5067296,13.2599309?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndNegativeCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"),
            "geo:-17.2165721,-149.9470294?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndIntegerCoordinates() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"),
            "geo:52.0,13.0?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndFractionalZoom() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"),
            "geo:52.5067296,13.2599309?z=6"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeOnly() {
        assertEquals(
            googleMapsUriToGeoUri("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            "geo:0,0?q=Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland"
        )
    }

    @Test
    fun requestLocationHeader_302Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        assertEquals(
            requestLocationHeader(mockUrl),
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
        )
    }

    @Test
    fun isGoogleMapsShortUri_correct() {
        assertEquals(isGoogleMapsShortUri("https://maps.app.goo.gl/foo"), true)
    }

    @Test
    fun isGoogleMapsShortUri_missingPath() {
        assertEquals(isGoogleMapsShortUri("https://maps.app.goo.gl/"), false)
    }

    @Test
    fun isGoogleMapsShortUri_unknownDomain() {
        assertEquals(isGoogleMapsShortUri("https://www.example.com/foo"), false)
    }

    @Test
    fun requestLocationHeader_302ResponseMissingLocation() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        assertEquals(requestLocationHeader(mockUrl), null)
        verify(mockConnection).setRequestMethod("HEAD")
        verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_200Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_OK)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        assertEquals(requestLocationHeader(mockUrl), null)
        verify(mockConnection).setRequestMethod("HEAD")
        verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_500Response() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_INTERNAL_ERROR)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")
        assertEquals(requestLocationHeader(mockUrl), null)
        verify(mockConnection).setRequestMethod("HEAD")
        verify(mockConnection).setInstanceFollowRedirects(false)
    }
}
