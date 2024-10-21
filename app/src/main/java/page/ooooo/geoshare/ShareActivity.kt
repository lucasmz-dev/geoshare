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

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        lifecycleScope.launch {
            processIntent(context)
            finish()
        }
    }

    private suspend fun processIntent(context: Context) {
        val intentAction = intent.action
        if (intentAction != Intent.ACTION_SEND) {
            return
        }
        val intentText =
            intent.getStringExtra("android.intent.extra.TEXT") ?: return
        val googleMapsParser = GoogleMapsParser(DefaultUriQuote())
        val url = if (googleMapsParser.isShortUrl(intentText)) {
            val shortUrl = try {
                URL(intentText)
            } catch (_: MalformedURLException) {
                return
            }
            requestLocationHeader(shortUrl) ?: return
        } else {
            intentText
        }
        val geoUriString = googleMapsParser.toGeoUri(url)
        if (geoUriString != null) {
            Log.i(null, "Converted $url to $geoUriString")
            Toast.makeText(
                context,
                "Opened geo URL",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString)))
        } else {
            Toast.makeText(
                context,
                "Failed to create geo URL",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
