package page.ooooo.geoshare.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.truncateMiddle
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun PermissionDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirmation: (doNotAsk: Boolean) -> Unit,
    onDismissRequest: (doNotAsk: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var doNotAsk by remember { mutableStateOf(false) }

    ConfirmationDialog(
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirmation = { onConfirmation(doNotAsk) },
        onDismissRequest = { onDismissRequest(doNotAsk) },
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
            content()
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Don't ask me again",
                    style = MaterialTheme.typography.labelLarge,
                )
                Switch(checked = doNotAsk, onCheckedChange = { doNotAsk = it })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        val appName = stringResource(R.string.app_name)
        val url =
            "https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg?g_ep=CAISDTYuMTE5LjEuNjYwNTAYASC33wEqbCw5NDIyNDgxOSw5NDIyNzI0NSw5NDIyNzI0Niw0NzA3MTcwNCw5NDIwNjE2Niw0NzA2OTUwOCw5NDIxNDE3Miw5NDIxODY0MSw5NDIwMzAxOSw0NzA4NDMwNCw5NDIwODQ1OCw5NDIwODQ0N0ICREU%3D&g_st=isi"
        PermissionDialog(
            title = "Connect to Google?",
            confirmText = "Allow",
            dismissText = "Create a search geo: link",
            onConfirmation = {},
            onDismissRequest = {},
        ) {
            Text(
                buildAnnotatedString {
                    append("The link ")
                    withStyle(codeStyle()) {
                        append(truncateMiddle(url))
                    }
                    append(" doesn't contain coordinates. $appName can connect to Google to get them, or it can create a geo: link with a search term instead.")
                },
                style = TextStyle(lineBreak = LineBreak.Paragraph),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParseHtmlPermissionPreview() {
    AppTheme {
        val appName = stringResource(R.string.app_name)
        val url = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
        PermissionDialog(
            title = "Connect to Google?",
            confirmText = "Allow",
            dismissText = "Quit",
            onConfirmation = {},
            onDismissRequest = {},
        ) {
            Text(
                buildAnnotatedString {
                    append("The link ")
                    withStyle(codeStyle()) {
                        append(truncateMiddle(url))
                    }
                    append(" doesn't contain coordinates or place name. $appName must connect to Google to get the information.")
                },
                style = TextStyle(lineBreak = LineBreak.Paragraph),
            )
        }
    }
}
