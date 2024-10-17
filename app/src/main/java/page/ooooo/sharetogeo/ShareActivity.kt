package page.ooooo.sharetogeo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.URL

open class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        val uriString = intent.getStringExtra("android.intent.extra.TEXT")
        if (action == Intent.ACTION_SEND && uriString != null) {
            val context = this
            lifecycleScope.launch {
                val geoUriString = getGeoUri(uriString)
                if (geoUriString != null) {
                    Log.i(null, "Converted $uriString to $geoUriString")
                    showToast(context, "Opened geo URL")
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(geoUriString)
                        )
                    )
                } else {
                    showToast(context, "Failed to create geo URL")
                }
                finish()
            }
        } else {
            finish()
        }
    }

    private fun showToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private suspend fun getGeoUri(uriString: String): String? {
        if (isGoogleMapsShortUri(uriString)) {
            val location = requestLocationHeader(URL(uriString))
            if (location != null) {
                return googleMapsUriToGeoUri(location)
            }
            return null
        }
        return googleMapsUriToGeoUri(uriString)
    }
}
