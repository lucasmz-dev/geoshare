package page.ooooo.geoshare

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CopyActivity : ComponentActivity() {

    private val googleMapsUrlConverter = GoogleMapsUrlConverter()
    private val networkTools = NetworkTools()

    private fun showToast(text: String) =
        runOnUiThread {
            Toast.makeText(this@CopyActivity, text, Toast.LENGTH_SHORT).show()
        }

    private fun copy(geoUri: Uri) =
        runOnUiThread {
            val clipboardManager =
                this@CopyActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("geo: link", geoUri.toString())
            clipboardManager.setPrimaryClip(clip)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Launch in application scope instead of lifecycle scope, so that the
        // job doesn't get cancelled.
        CoroutineScope(SupervisorJob()).launch {
            try {
                val action = googleMapsUrlConverter.processIntent(
                    intent,
                    networkTools
                )
                val systemHasClipboardEditor =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                when (action) {
                    is GeoUriAction.Fail -> {
                        showToast(action.message)
                    }

                    is GeoUriAction.Open -> {
                        if (!systemHasClipboardEditor) {
                            showToast("Copied geo: link to clipboard")
                        }
                        copy(action.geoUri)
                    }

                    is GeoUriAction.OpenUnchanged -> {
                        if (!systemHasClipboardEditor) {
                            showToast("Copied geo: link to clipboard unchanged")
                        }
                        copy(action.geoUri)
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
