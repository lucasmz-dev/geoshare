package page.ooooo.geoshare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class NetworkTools(private val log: ILog = DefaultLog()) {

    suspend fun requestLocationHeader(url: URL): URL? =
        withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
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
                log.w(null, e.message ?: "Unknown network error for $url")
            } finally {
                connection.disconnect()
            }
            return@withContext null
        }
}
