package page.ooooo.geoshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val networkTools = NetworkTools()

    private val extraProcessed = "page.ooooo.geoshare.EXTRA_PROCESSED"

    private fun isIntentProcessed() =
        intent.getStringExtra(extraProcessed) != null

    private fun showToast(text: String) =
        runOnUiThread {
            Toast.makeText(this@ShareActivity, text, Toast.LENGTH_SHORT).show()
        }

    private fun open(geoUri: Uri) {
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            putExtra(extraProcessed, "true")
            data = geoUri
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isIntentProcessed()) {
            return showToast("Nothing to do")
        }
        // Launch in application scope instead of lifecycle scope, so that the
        // job doesn't get cancelled.
        CoroutineScope(SupervisorJob()).launch {
            try {
                val action =
                    googleMapsUrlConverter.processIntent(intent, networkTools)
                when (action) {
                    is GeoUriAction.Fail -> {
                        showToast(action.message)
                    }

                    is GeoUriAction.Open -> {
                        showToast("Opened geo: link")
                        open(action.geoUri)
                    }

                    is GeoUriAction.OpenUnchanged -> {
                        showToast("Opened geo: link unchanged")
                        open(action.geoUri)
                    }

                    is GeoUriAction.Noop -> {}
                }
            } catch (e: Exception) {
                Log.e(null, Log.getStackTraceString(e))
                showToast("Unknown error when creating geo: link")
            }
        }
        finish()
    }
}
