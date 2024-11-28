package page.ooooo.geoshare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class NetworkTools(private val log: ILog = DefaultLog()) {

    val connectTimeout = 5_000
    val readTimeout = 10_000

    suspend fun requestLocationHeader(url: URL): URL? =
        withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            connection.instanceFollowRedirects = false
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    val locationUrlString =
                        connection.getHeaderField("Location")
                    val locationUrl = try {
                        URL(locationUrlString)
                    } catch (_: MalformedURLException) {
                        log.w(null, "Invalid location URL $locationUrlString")
                        return@withContext null
                    }
                    log.i(null, "Resolved short URL $url to $locationUrlString")
                    return@withContext locationUrl
                }
                log.w(null, "Received HTTP code $responseCode for $url")
            } catch (e: Exception) {
                log.w(
                    null,
                    "Network error for $url ${log.getStackTraceString(e)}"
                )
            } finally {
                connection.disconnect()
            }
            return@withContext null
        }

    suspend fun getText(url: URL): String? =
        withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            connection.instanceFollowRedirects = false
            // Remove User-Agent, otherwise we receive Google Maps Lite HTML,
            // which doesn't contain coordinates.
            connection.setRequestProperty("User-Agent", "")
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@withContext connection.getInputStream().reader()
                        .use { it.readText() }
                }
                log.w(null, "Received HTTP code $responseCode for $url")
            } catch (e: Exception) {
                log.w(
                    null,
                    "Network error for $url ${log.getStackTraceString(e)}"
                )
            } finally {
                connection.disconnect()
            }
            return@withContext null
        }
}
