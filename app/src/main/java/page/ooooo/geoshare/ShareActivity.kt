package page.ooooo.geoshare

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val extraProcessed = "page.ooooo.geoshare.EXTRA_PROCESSED"

    private fun isIntentProcessed() =
        intent.getStringExtra(extraProcessed) != null

    private fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) =
        runOnUiThread {
            Toast.makeText(this@ShareActivity, text, duration).show()
        }

    private fun onSucceeded(geoUri: String, unchanged: Boolean) {
        showToast(if (!unchanged) "Opened geo: link" else "Opened geo: link unchanged")
        try {
            startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                putExtra(extraProcessed, "true")
                data = Uri.parse(geoUri)
            })
        } catch (_: ActivityNotFoundException) {
            showToast(
                "No app that can open geo: links is installed",
                Toast.LENGTH_LONG,
            )
        }
        finish()
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
        if (isIntentProcessed()) {
            onFailed("Nothing to do")
            return
        }
        setContent {
            AppTheme {
                ShareScreen(
                    intent,
                    onSucceeded = { geoUri, unchanged ->
                        onSucceeded(geoUri, unchanged)
                    },
                    onFailed = { message -> onFailed(message) },
                    onNoop = { onNoop() },
                )
            }
        }
    }
}
