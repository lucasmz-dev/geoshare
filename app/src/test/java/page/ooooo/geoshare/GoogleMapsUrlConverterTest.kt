package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Before

import org.junit.Test
import java.net.URL

class GoogleMapsUrlConverterTest {

    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter

    @Before
    fun before() {
        googleMapsUrlConverter =
            GoogleMapsUrlConverter(FakeLog(), FakeUriQuote())
    }

    @Test
    fun toGeoUri_coordinatesOnly() {
        assertEquals(
            "geo:52.5067296,13.2599309?z=6",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z"))
        )
    }

    @Test
    fun toGeoUri_coordinatesOnlyStreetView() {
        assertEquals(
            "geo:53.512825,57.6891441",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def"))
        )
    }

    @Test
    fun toGeoUri_placeAndPositiveCoordinates() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"))
        )
    }

    @Test
    fun toGeoUri_placeAndNegativeCoordinates() {
        assertEquals(
            "geo:-17.2165721,-149.9470294?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"))
        )
    }

    @Test
    fun toGeoUri_placeAndIntegerCoordinates() {
        assertEquals(
            "geo:52,13?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"))
        )
    }

    @Test
    fun toGeoUri_placeAndFractionalZoom() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=6",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"))
        )
    }

    @Test
    fun toGeoUri_placeAndData() {
        assertEquals(
            "geo:40.785091,-73.968285?q=Central%20Park&z=15",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285")),
        )
    }

    @Test
    fun toGeoUri_placeAsCoordinates() {
        assertEquals(
            "geo:52.04,-2.35?z=15",
            googleMapsUrlConverter.toGeoUri(URL("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3"))
        )
    }

    @Test
    fun toGeoUri_placeAsCoordinatesWithPlus() {
        assertEquals(
            "geo:52.492611,13.431726?z=17",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2!3d52.4926111!4d13.4317261?force=pwa"))
        )
    }

    @Test
    fun toGeoUri_placeCoordinatesOnly() {
        assertEquals(
            "geo:52.03877,-2.3416",
            googleMapsUrlConverter.toGeoUri(URL("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3"))
        )
    }

    @Test
    fun toGeoUri_placeOnly() {
        assertEquals(
            "geo:0,0?q=Pozna%C5%84%20Old%20Town%2C%2061-001%20Pozna%C5%84%2C%20Poland",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"))
        )
    }

    @Test
    fun toGeoUri_searchCoordinates() {
        assertEquals(
            "geo:48.8584,2.2945",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/search/48.8584,2.2945"))
        )
    }

    @Test
    fun toGeoUri_searchPlace() {
        assertEquals(
            "geo:0,0?q=restaurants%20near%20me",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/search/restaurants+near+me"))
        )
    }

    @Test
    fun toGeoUri_searchQueryCoordinates() {
        assertEquals(
            "geo:47.5951518,-122.3316393",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518%2C-122.3316393&api=1"))
        )
    }

    @Test
    fun toGeoUri_searchQueryPlace() {
        assertEquals(
            "geo:0,0?q=centurylink%20field",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"))
        )
    }

    @Test
    fun toGeoUri_directionsCoordinates() {
        assertEquals(
            "geo:34.0522,-118.2437",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437"))
        )
    }

    @Test
    fun toGeoUri_directionsFromTo() {
        assertEquals(
            "geo:0,0?q=Los%20Angeles%2C%20CA",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"))
        )
    }

    @Test
    fun toGeoUri_directionsFromToVia() {
        assertEquals(
            "geo:0,0?q=Washington%2C%20DC",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"))
        )
    }

    @Test
    fun toGeoUri_directionsFromToWithData() {
        assertEquals(
            "geo:0,0?q=Potsdam",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/dir/Berlin/Potsdam/data=abcd"))
        )
    }

    @Test
    fun toGeoUri_streetView() {
        assertEquals(
            "geo:48.8584,2.2945",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120"))
        )
    }

    @Test
    fun toGeoUri_apiCenter() {
        assertEquals(
            "geo:-33.712206,150.311941?z=12",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206%2C150.311941&zoom=12&basemap=terrain"))
        )
    }

    @Test
    fun toGeoUri_apiDirections() {
        assertEquals(
            "geo:0,0?q=Cherbourg%2CFrance",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/dir/?api=1&origin=Paris%2CFrance&destination=Cherbourg%2CFrance&travelmode=driving&waypoints=Versailles%2CFrance%7CChartres%2CFrance%7CLe%2BMans%2CFrance%7CCaen%2CFrance"))
        )
    }

    @Test
    fun toGeoUri_apiViewpoint() {
        assertEquals(
            "geo:48.857832,2.295226",
            googleMapsUrlConverter.toGeoUri(URL("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832%2C2.295226&map_action=pano&api=1"))
        )
    }

    @Test
    fun toGeoUri_http() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.toGeoUri(URL("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")),
        )
    }

    @Test
    fun toGeoUri_ukDomain() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.toGeoUri(URL("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")),
        )
    }

    @Test
    fun toGeoUri_unknownProtocol() {
        assertEquals(
            null,
            googleMapsUrlConverter.toGeoUri(URL("ftp://www.google.com/maps/@52.5067296,13.2599309,6z"))
        )
    }

    @Test
    fun toGeoUri_unknownHost() {
        assertEquals(
            null,
            googleMapsUrlConverter.toGeoUri(URL("https://www.example.com/"))
        )
    }

    @Test
    fun isGoogleMapsSphortUri_mapsAppGooGlCorrect() {
        assertEquals(
            true,
            googleMapsUrlConverter.isShortUrl(URL("https://maps.app.goo.gl/foo"))
        )
    }

    @Test
    fun isGoogleMapsShortUri_mapsAppGooGlMissingPath() {
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://maps.app.goo.gl/"))
        )
    }

    @Test
    fun isGoogleMapsSphortUri_appGooGlCorrect() {
        assertEquals(
            true,
            googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps/foo"))
        )
    }

    @Test
    fun isGoogleMapsSphortUri_appGooGlWrongPath() {
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps"))
        )
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps/"))
        )
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/foo/bar"))
        )
    }

    @Test
    fun isGoogleMapsSphortUri_gooGlCorrect() {
        assertEquals(true, googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/foo")))
    }

    @Test
    fun isGoogleMapsSphortUri_gooGlWrongPath() {
        assertEquals(false, googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps")))
        assertEquals(false, googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/")))
        assertEquals(false, googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/foo/bar")))
    }

    @Test
    fun isGoogleMapsShortUri_unknownDomain() {
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://www.example.com/foo"))
        )
    }
}
