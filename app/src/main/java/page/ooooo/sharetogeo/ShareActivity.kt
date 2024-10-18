package page.ooooo.sharetogeo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.URL

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentAction = intent.action
        val intentText = intent.getStringExtra("android.intent.extra.TEXT")
        if (intentAction == Intent.ACTION_SEND && intentText != null) {
            val context = this
            lifecycleScope.launch {
                val uriString = if (isGoogleMapsShortUri(intentText)) {
                    requestLocationHeader(URL(intentText))
                } else {
                    intentText
                }
                if (uriString != null) {
                    val geoUriString = googleMapsUriToGeoUri(uriString)
                    if (geoUriString != null) {
                        Log.i(null, "Converted $uriString to $geoUriString")
                        Toast.makeText(
                            context,
                            "Opened geo URL",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString))
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to create geo URL",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                finish()
            }
        } else {
            finish()
        }
    }
}
