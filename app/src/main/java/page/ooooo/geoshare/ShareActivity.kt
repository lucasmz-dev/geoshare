package page.ooooo.geoshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

const val EXTRA_PROCESSED = "page.ooooo.geoshare.EXTRA_PROCESSED"

class ShareActivity : ComponentActivity() {

    private val intentUrlRegex = Regex("https?://\\S+")

    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val networkTools = NetworkTools()

    private fun isIntentProcessed() =
        intent.getStringExtra(EXTRA_PROCESSED) != null

    private fun getIntentGeoUri(): Uri? {
        return if (intent.action == Intent.ACTION_VIEW && intent.data != null && intent.scheme == "geo") {
            intent.data
        } else {
            null
        }
    }

    private fun getIntentUrl(): URL? {
        val intentAction = intent.action
        val urlString = if (intentAction == Intent.ACTION_VIEW) {
            val intentData: Uri? = intent?.data
            if (intentData == null) {
                Log.w(null, "Missing intent data")
                return null
            }
            intentData.toString()
        } else if (intentAction == Intent.ACTION_SEND) {
            val intentText = intent.getStringExtra("android.intent.extra.TEXT")
            if (intentText == null) {
                Log.w(null, "Missing intent extra text")
                return null
            }
            val intentUrlMatch = intentUrlRegex.find(intentText)
            if (intentUrlMatch == null) {
                Log.w(null, "Intent extra text does not contain a URL")
                return null
            }
            intentUrlMatch.value
        } else {
            Log.w(null, "Unsupported intent action $intentAction")
            return null
        }
        return try {
            URL(urlString)
        } catch (_: MalformedURLException) {
            Log.w(null, "Invalid URL $urlString")
            return null
        }
    }

    private fun showToast(context: Context, text: String) =
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    private suspend fun processIntent(context: Context) {
        if (isIntentProcessed()) {
            showToast(context, "Nothing to do")
            return
        }
        val intentGeoUri = getIntentGeoUri()
        val geoUri = if (intentGeoUri != null) {
            showToast(context, "Opened geo URL unchanged")
            intentGeoUri
        } else {
            val intentUrl = getIntentUrl() ?: return
            val url = if (googleMapsUrlConverter.isShortUrl(intentUrl)) {
                val locationHeader =
                    networkTools.requestLocationHeader(intentUrl)
                if (locationHeader == null) {
                    showToast(context, "Failed to resolve short URL")
                    return
                }
                locationHeader
            } else {
                intentUrl
            }
            val geoUriBuilderFromUrl = googleMapsUrlConverter.parseUrl(url)
            if (geoUriBuilderFromUrl == null) {
                showToast(context, "Failed to create geo URL")
                return
            }
            val geoUriBuilder =
                if (geoUriBuilderFromUrl.coords.lat != "0" || geoUriBuilderFromUrl.coords.lon != "0") {
                    geoUriBuilderFromUrl
                } else {
                    val html = networkTools.getText(url)
                    if (html == null) {
                        showToast(context, "Failed to fetch Google Maps page")
                        return
                    }
                    val geoUriBuilderFromHtml =
                        googleMapsUrlConverter.parseHtml(html)
                    geoUriBuilderFromHtml ?: geoUriBuilderFromUrl
                }
            showToast(context, "Opened geo URL")
            Uri.parse(geoUriBuilder.toString())
        }
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_PROCESSED, "true")
            data = geoUri
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        lifecycleScope.launch {
            processIntent(context)
            finish()
        }
    }
}
