package page.ooooo.geoshare

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.withStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.PermissionDialog
import page.ooooo.geoshare.components.codeStyle
import page.ooooo.geoshare.lib.*

@Composable
fun ConversionScreen(viewModel: ConversionViewModel = hiltViewModel()) {
    val appName = stringResource(R.string.app_name)
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    when (currentState) {
        is RequestedUnshortenPermission -> {
            val requestedUnshortenPermission =
                currentState as RequestedUnshortenPermission
            PermissionDialog(
                title = "Connect to Google?",
                confirmText = "Allow",
                dismissText = "Quit",
                onConfirmation = { viewModel.grant(it) },
                onDismissRequest = { viewModel.deny(it) },
                modifier = Modifier.testTag("unshortenPermissionDialog"),
            ) {
                Text(
                    buildAnnotatedString {
                        append("The link ")
                        withStyle(codeStyle()) {
                            append(requestedUnshortenPermission.url.toString())
                        }
                        append(" doesn't contain coordinates or place name. $appName must connect to Google to get the information.")
                    },
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }

        is RequestedParseHtmlPermission -> {
            val requestedParseHtmlPermission =
                currentState as RequestedParseHtmlPermission
            PermissionDialog(
                title = "Connect to Google?",
                confirmText = "Allow",
                dismissText = "Create a search geo: link",
                onConfirmation = { viewModel.grant(it) },
                onDismissRequest = { viewModel.deny(it) },
                modifier = Modifier.testTag("parseHtmlPermissionDialog")
            ) {
                Text(
                    buildAnnotatedString {
                        append("The link ")
                        withStyle(codeStyle()) {
                            append(truncateMiddle(requestedParseHtmlPermission.url.toString()))
                        }
                        append(" doesn't contain coordinates. $appName can connect to Google to get them, or it can create a geo: link with a search term instead.")
                    },
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                )
            }
        }
    }
}
