package page.ooooo.geoshare

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class CopyActivity() : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

    private fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@CopyActivity, text, duration).show()
    }

    private fun onSucceeded(geoUri: String, unchanged: Boolean) {
        val systemHasClipboardEditor =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (!systemHasClipboardEditor) {
            showToast(if (!unchanged) "Copied geo: link to clipboard" else "Copied geo: link to clipboard unchanged")
        }
        val clipboardManager =
            this@CopyActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("geo: link", geoUri)
        clipboardManager.setPrimaryClip(clip)
        finish() // FIXME Copy before destroying the activity.
    }

    private fun onFailed(message: String) {
        showToast(message, Toast.LENGTH_LONG)
        finish()
    }

    private fun onNoop() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                ConversionScreen(
                    intent,
                    onSucceeded = { geoUri, unchanged ->
                        onSucceeded(geoUri, unchanged)
                    },
                    onFailed = { message -> onFailed(message) },
                    onNoop = { onNoop() },
                    viewModel = viewModel,
                )
            }
        }
    }
}
