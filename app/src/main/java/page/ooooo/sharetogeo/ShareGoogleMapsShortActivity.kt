package page.ooooo.sharetogeo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

class ShareGoogleMapsShortActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        val data = intent?.data
        if (action == Intent.ACTION_VIEW && data != null) {
            val uriString = requestLocationHeader(data.toString())
            if (uriString != null) {
                val geoUri = googleMapsUriToGeoUri(uriString)
                if (geoUri != null) {
                    Log.i(null, "Converted $data to $geoUri")
                    // TODO Try to show a toast message.
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
                }
            }
        }
        finish()
    }
}
