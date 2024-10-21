package page.ooooo.geoshare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

import java.net.URL

suspend fun requestLocationHeader(url: URL): String? =
    withContext(Dispatchers.IO) {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.instanceFollowRedirects = false
        try {
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                return@withContext connection.getHeaderField("Location")
            }
        } finally {
            connection.disconnect()
        }
        return@withContext null
    }
