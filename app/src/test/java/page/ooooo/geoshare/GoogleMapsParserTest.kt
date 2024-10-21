package page.ooooo.geoshare

import org.junit.Assert.assertEquals

import org.junit.Test
import java.net.URLDecoder
import java.net.URLEncoder

class FakeUriQuote : UriQuote {
    override fun encode(s: String): String =
        URLEncoder.encode(s, "utf-8").replace("+", "%20")

    override fun decode(s: String): String =
        URLDecoder.decode(s, "utf-8")
}

class GoogleMapsParserTest {

    @Test
    fun googleMapsUriToGeoUri_unknownDomain() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.example.com/"),
            null
        )
    }

    @Test
    fun googleMapsUriToGeoUri_coordinatesOnly() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/@52.5067296,13.2599309,6z"),
            "geo:52.5067296,13.2599309?z=6"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndPositiveCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
            "geo:52.5067296,13.2599309?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_http() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
            "geo:52.5067296,13.2599309?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_ukDomain() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
            "geo:52.5067296,13.2599309?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndNegativeCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"),
            "geo:-17.2165721,-149.9470294?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndIntegerCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"),
            "geo:52,13?z=11"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAndFractionalZoom() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"),
            "geo:52.5067296,13.2599309?z=6"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAsCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://maps.google.com/maps/place/52.03877,-2.3416/@52.03877,-2.3416,15z/data=!3m1!1e3"),
            "geo:52.03877,-2.3416?z=15"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeAsCoordinatesWithZoom() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://maps.google.com/maps/place/52.03877,-2.3416,3z/@52.03877,-2.3416,15z/data=!3m1!1e3"),
            "geo:52.03877,-2.3416?z=15"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placeOnly() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            "geo:0,0?q=Pozna%C5%84%20Old%20Town%2C%2061-001%20Pozna%C5%84%2C%20Poland"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_searchQueryPlace() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"),
            "geo:0,0?q=centurylink%20field"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_searchQueryCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518%2C-122.3316393&api=1"),
            "geo:47.5951518,-122.3316393"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_center() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206%2C150.311941&zoom=12&basemap=terrain"),
            "geo:-33.712206,150.311941?z=12"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_viewpoint() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832%2C2.295226&map_action=pano&api=1"),
            "geo:48.857832,2.295226"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_directions() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/dir/?api=1&origin=Paris%2CFrance&destination=Cherbourg%2CFrance&travelmode=driving&waypoints=Versailles%2CFrance%7CChartres%2CFrance%7CLe%2BMans%2CFrance%7CCaen%2CFrance"),
            "geo:0,0?q=Cherbourg%2CFrance"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_directionsCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437"),
            "geo:34.0522,-118.2437"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_directionsTwoPaths() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"),
            "geo:0,0?q=Los%20Angeles%2C%20CA"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_directionsThreePaths() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"),
            "geo:0,0?q=Washington%2C%20DC"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_searchPath() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/search/restaurants+near+me"),
            "geo:0,0?q=restaurants%20near%20me"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_searchCoordinates() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/search/48.8584,2.2945"),
            "geo:48.8584,2.2945"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_streetView() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120"),
            "geo:48.8584,2.2945"
        )
    }

    @Test
    fun googleMapsUriToGeoUri_placePlus() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).toGeoUri("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285"),
            "geo:40.785091,-73.968285?z=15"
        )
    }

    @Test
    fun isGoogleMapsShortUri_correct() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).isShortUrl("https://maps.app.goo.gl/foo"),
            true
        )
    }

    @Test
    fun isGoogleMapsShortUri_missingPath() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).isShortUrl("https://maps.app.goo.gl/"),
            false
        )
    }

    @Test
    fun isGoogleMapsShortUri_unknownDomain() {
        assertEquals(
            GoogleMapsParser(FakeUriQuote()).isShortUrl("https://www.example.com/foo"),
            false
        )
    }
}
