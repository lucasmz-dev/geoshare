package page.ooooo.geoshare

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.NetworkTools
import java.net.HttpURLConnection
import java.net.URL

class NetworkToolsTest {

    private lateinit var networkTools: NetworkTools

    @Before
    fun before() {
        networkTools = NetworkTools(FakeLog())
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
            URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
            networkTools.requestLocationHeader(mockUrl)
        )
    }

    @Test
    fun requestLocationHeader_302ResponseMissingLocation() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        assertEquals(null, networkTools.requestLocationHeader(mockUrl))
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
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
        assertEquals(null, networkTools.requestLocationHeader(mockUrl))
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
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
        assertEquals(null, networkTools.requestLocationHeader(mockUrl))
        Mockito.verify(mockConnection).setRequestMethod("HEAD")
        Mockito.verify(mockConnection).setInstanceFollowRedirects(false)
    }

    @Test
    fun requestLocationHeader_invalidLocationUrl() = runTest {
        val mockUrl = Mockito.mock(URL::class.java)
        val mockConnection = Mockito.mock(HttpURLConnection::class.java)
        Mockito.`when`(mockUrl.openConnection()).thenReturn(mockConnection)
        Mockito.`when`(mockConnection.getResponseCode())
            .thenReturn(HttpURLConnection.HTTP_MOVED_TEMP)
        Mockito.`when`(mockConnection.getHeaderField("Location"))
            .thenReturn("spam")
        assertEquals(null, networkTools.requestLocationHeader(mockUrl))
    }

}
