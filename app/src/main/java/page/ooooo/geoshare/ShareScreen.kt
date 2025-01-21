package page.ooooo.geoshare

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.ConfirmationDialog
import page.ooooo.geoshare.lib.RequestedSharePermission

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareScreen(viewModel: ConversionViewModel = hiltViewModel()) {
    val appName = stringResource(R.string.app_name)
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    ConversionScreen(viewModel)

    when (currentState) {
        is RequestedSharePermission -> {
            ConfirmationDialog(
                title = "Missing permission",
                confirmText = "Open Android settings",
                dismissText = "Dismiss",
                onConfirmation = { viewModel.grant(doNotAsk = false) },
                onDismissRequest = { viewModel.deny(doNotAsk = false) },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .testTag("geoShareXiaomiPermissionDialog"),
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
    }
}
