package page.ooooo.geoshare

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import page.ooooo.geoshare.components.ConfirmationDialog
import page.ooooo.geoshare.lib.GrantedSharePermission
import page.ooooo.geoshare.lib.RequestedSharePermission
import page.ooooo.geoshare.ui.theme.AppTheme

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val viewModel: ConversionViewModel by viewModels()

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
            val appName = stringResource(R.string.app_name)
            val currentState by viewModel.currentState.collectAsStateWithLifecycle()
            val settingsLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    viewModel.retryShare()
                }

            AppTheme {
                ConversionScreen(
                    intent,
                    onSucceeded = { geoUri, unchanged ->
                        viewModel.share(
                            activity = this,
                            geoUri = geoUri,
                            unchanged = unchanged,
                        )
                    },
                    onFailed = { message -> onFailed(message) },
                    onNoop = { onNoop() },
                    viewModel = viewModel,
                )

                when (currentState) {
                    is RequestedSharePermission -> {
                        ConfirmationDialog(
                            title = "Missing permission",
                            confirmText = "Open Android settings",
                            dismissText = "Dismiss",
                            onConfirmation = {
                                viewModel.showSharePermissionsEditor(
                                    activity = this,
                                    launcher = settingsLauncher,
                                    onError = { message ->
                                        showToast(message, Toast.LENGTH_LONG)
                                    },
                                )
                            },
                            onDismissRequest = { viewModel.dismissShare() },
                            modifier = Modifier.testTag("xiaomiPermissionDialog"),
                        ) {
                            Text(buildAnnotatedString {
                                append("To share geo: links, you must allow ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Other permissions")
                                }
                                append(" > ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Display pop-up windows while running in the background")
                                }
                                append(" to $appName.")
                            })
                        }
                    }

                    is GrantedSharePermission -> {
                        (currentState as GrantedSharePermission).let {
                            onSucceeded(it.geoUri, it.unchanged)
                        }
                    }
                }
            }
        }
    }
}
