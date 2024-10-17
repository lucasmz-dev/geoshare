package page.ooooo.sharetogeo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import java.net.URL

class ShareGoogleMapsShortActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        val data = intent?.data
        if (action == Intent.ACTION_VIEW && data != null) {
            val location = requestLocationHeader(URL(data.toString()))
            if (location != null) {
                val geoUri = googleMapsUriToGeoUri(location)
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
