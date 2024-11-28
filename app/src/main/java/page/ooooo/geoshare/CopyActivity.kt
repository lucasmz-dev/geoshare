package page.ooooo.geoshare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CopyActivity : ComponentActivity() {

    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val networkTools = NetworkTools()

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun copy(context: Context, geoUri: Uri) {
        val clipboardManager =
            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("geo: link", geoUri.toString())
        clipboardManager.setPrimaryClip(clip)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val action = googleMapsUrlConverter.processIntent(
                intent,
                networkTools
            )
            when (action) {
                is GeoUriAction.Fail -> {
                    showToast(action.message)
                }

                is GeoUriAction.Open -> {
                    showToast("Copied geo: link to clipboard") // TODO if androidVersion < 13
                    copy(this@CopyActivity, action.geoUri)
                }

                is GeoUriAction.OpenUnchanged -> {
                    showToast("Copied geo: link to clipboard unchanged") // TODO if androidVersion < 13
                    copy(this@CopyActivity, action.geoUri)
                }

                is GeoUriAction.Noop -> {}
            }
        }
        finish()
    }
}
