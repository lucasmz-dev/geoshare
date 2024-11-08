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
        if (intentAction != Intent.ACTION_SEND) {
            Log.w(null, "Unsupported intent action $intentAction")
            return null
        }
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
        val urlString = intentUrlMatch.value
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
        var geoUri = getIntentGeoUri()
        if (geoUri != null) {
            showToast(context, "Opened geo URL unchanged")
        } else {
            val intentUrl = getIntentUrl() ?: return
            val url = if (googleMapsUrlConverter.isShortUrl(intentUrl)) {
                networkTools.requestLocationHeader(intentUrl) ?: return
            } else {
                intentUrl
            }
            val geoUriString = googleMapsUrlConverter.toGeoUri(url)
            if (geoUriString == null) {
                showToast(context, "Failed to create geo URL")
                return
            }
            geoUri = Uri.parse(geoUriString)
            showToast(context, "Opened geo URL")
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
