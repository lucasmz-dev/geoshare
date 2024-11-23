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
    fun parse_coordinatesOnly() {
        assertEquals(
            "geo:52.5067296,13.2599309?z=6",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z"))
                .toString()
        )
    }

    @Test
    fun parse_coordinatesOnlyStreetView() {
        assertEquals(
            "geo:53.512825,57.6891441",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinates() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinatesWithManyDecimalPlaces() {
        assertEquals(
            "geo:44.448337599999995,26.0834555",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Strada+Occidentului+7,+Bucure%C8%99ti,+Romania/data=!4m6!3m5!1s0x40b201fdfa573623:0x4f53bb5ad3fdc97f!7e2!8m2!3d44.448337599999995!4d26.0834555?utm_source=mstt_1&entry=gps&coh=192189&g_ep=abc"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndNegativeCoordinates() {
        assertEquals(
            "geo:-17.2165721,-149.9470294?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndIntegerCoordinates() {
        assertEquals(
            "geo:52,13?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndFractionalZoom() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=6",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndData() {
        assertEquals(
            "geo:40.785091,-73.968285?q=Central%20Park&z=15",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2"))
                .toString(),
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinatesAndPositiveDataCoordinates() {
        assertEquals(
            "geo:44.4490541,26.0888398?z=11",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate/@44.5190589,25.7489796,11.42z/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"))
                .toString()
        )
    }

    @Test
    fun parse_placeAndNegativeCoordinatesAndNegativeDataCoordinates() {
        assertEquals(
            "geo:40.785091,-73.968285?z=15",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.8,-73.9,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285"))
                .toString(),
        )
    }

    @Test
    fun parse_placeAndPositiveDataCoordinates() {
        assertEquals(
            "geo:44.4490541,26.0888398",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Rom%C3%A2nia/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"))
                .toString()
        )
    }

    @Test
    fun parse_placeAsCoordinates() {
        assertEquals(
            "geo:52.04,-2.35?z=15",
            googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3"))
                .toString()
        )
    }

    @Test
    fun parse_placeAsCoordinatesWithPlus() {
        assertEquals(
            "geo:52.492611,13.431726?z=17",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2?force=pwa"))
                .toString()
        )
    }

    @Test
    fun parse_placeCoordinatesOnly() {
        assertEquals(
            "geo:52.03877,-2.3416",
            googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3"))
                .toString()
        )
    }

    @Test
    fun parse_placeOnly() {
        assertEquals(
            "geo:0,0?q=Pozna%C5%84%20Old%20Town%2C%2061-001%20Pozna%C5%84%2C%20Poland",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"))
                .toString()
        )
    }

    @Test
    fun parse_searchCoordinates() {
        assertEquals(
            "geo:48.8584,2.2945",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/48.8584,2.2945"))
                .toString()
        )
    }

    @Test
    fun parse_searchPlace() {
        assertEquals(
            "geo:0,0?q=restaurants%20near%20me",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/restaurants+near+me"))
                .toString()
        )
    }

    @Test
    fun parse_searchQueryCoordinates() {
        assertEquals(
            "geo:47.5951518,-122.3316393",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518%2C-122.3316393&api=1"))
                .toString()
        )
    }

    @Test
    fun parse_searchQueryPlace() {
        assertEquals(
            "geo:0,0?q=centurylink%20field",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"))
                .toString()
        )
    }

    @Test
    fun parse_directionsCoordinates() {
        assertEquals(
            "geo:34.0522,-118.2437",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437"))
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromTo() {
        assertEquals(
            "geo:0,0?q=Los%20Angeles%2C%20CA",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"))
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromToVia() {
        assertEquals(
            "geo:0,0?q=Washington%2C%20DC",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"))
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromToWithData() {
        assertEquals(
            "geo:0,0?q=Potsdam",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/Berlin/Potsdam/data=abcd"))
                .toString()
        )
    }

    @Test
    fun parse_directionsEmpty() {
        assertEquals(
            "geo:0,0",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/"))
                .toString()
        )
    }

    @Test
    fun parse_streetView() {
        assertEquals(
            "geo:48.8584,2.2945",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120"))
                .toString()
        )
    }

    @Test
    fun parse_apiCenter() {
        assertEquals(
            "geo:-33.712206,150.311941?z=12",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206%2C150.311941&zoom=12&basemap=terrain"))
                .toString()
        )
    }

    @Test
    fun parse_apiDirections() {
        assertEquals(
            "geo:0,0?q=Cherbourg%2CFrance",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/?api=1&origin=Paris%2CFrance&destination=Cherbourg%2CFrance&travelmode=driving&waypoints=Versailles%2CFrance%7CChartres%2CFrance%7CLe%2BMans%2CFrance%7CCaen%2CFrance"))
                .toString()
        )
    }

    @Test
    fun parse_apiViewpoint() {
        assertEquals(
            "geo:48.857832,2.295226",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832%2C2.295226&map_action=pano&api=1"))
                .toString()
        )
    }

    @Test
    fun parse_qParameter() {
        assertEquals(
            "geo:48.857832,2.295226",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps?foo=bar&q=48.857832%2C2.295226&spam"))
                .toString()
        )
    }

    @Test
    fun parse_qParameterEmpty() {
        assertEquals(
            "geo:0,0",
            googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps"))
                .toString()
        )
    }

    @Test
    fun parse_http() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.parseUrl(URL("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"))
                .toString(),
        )
    }

    @Test
    fun parse_ukDomain() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            googleMapsUrlConverter.parseUrl(URL("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"))
                .toString(),
        )
    }

    @Test
    fun parse_unknownProtocol() {
        assertEquals(
            null,
            googleMapsUrlConverter.parseUrl(URL("ftp://www.google.com/maps/@52.5067296,13.2599309,6z"))
        )
    }

    @Test
    fun parse_unknownHost() {
        assertEquals(
            null,
            googleMapsUrlConverter.parseUrl(URL("https://www.example.com/"))
        )
    }

    @Test
    fun parseHtml_success() {
        val html =
            this.javaClass.classLoader!!.getResource("TmbeHMiLEfTBws9EA.html")!!
                .readText()
        assertEquals(
            "geo:44.4490541,26.0888398",
            googleMapsUrlConverter.parseHtml(html).toString()
        )
    }

    @Test
    fun parseHtml_failure() {
        assertEquals(
            null,
            googleMapsUrlConverter.parseHtml("spam")
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
    fun isGoogleMapsShortUri_appGooGlCorrect() {
        assertEquals(
            true,
            googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps/foo"))
        )
    }

    @Test
    fun isGoogleMapsShortUri_appGooGlWrongPath() {
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
    fun isGoogleMapsShortUri_gooGlCorrect() {
        assertEquals(
            true,
            googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/foo"))
        )
    }

    @Test
    fun isGoogleMapsShortUri_gooGlWrongPath() {
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps"))
        )
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/"))
        )
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/foo/bar"))
        )
    }

    @Test
    fun isGoogleMapsShortUri_unknownDomain() {
        assertEquals(
            false,
            googleMapsUrlConverter.isShortUrl(URL("https://www.example.com/foo"))
        )
    }
}
