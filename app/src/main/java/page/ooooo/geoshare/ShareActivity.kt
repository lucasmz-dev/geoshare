package page.ooooo.geoshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val networkTools = NetworkTools()

    private val extraProcessed = "page.ooooo.geoshare.EXTRA_PROCESSED"

    private fun isIntentProcessed() =
        intent.getStringExtra(extraProcessed) != null

    private fun showToast(text: String) =
        Toast.makeText(this@ShareActivity, text, Toast.LENGTH_SHORT).show()

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
        lifecycleScope.launch {
            val action =
                googleMapsUrlConverter.processIntent(intent, networkTools)
            when (action) {
                is GeoUriAction.Fail -> {
                    showToast(action.message)
                }

                is GeoUriAction.Open -> {
                    showToast("Opened geo URL")
                    open(action.geoUri)
                }

                is GeoUriAction.OpenUnchanged -> {
                    showToast("Opened geo URL unchanged")
                    open(action.geoUri)
                }

                is GeoUriAction.Noop -> {}
            }
        }
        finish()
    }
}
