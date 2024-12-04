package page.ooooo.geoshare.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun ConfirmationDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { content() },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) { Text(dismissText) }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ConfirmationDialog(
            title = "My title",
            confirmText = "Confirm",
            dismissText = "Dismiss",
            onConfirmation = {},
            onDismissRequest = {},
        ) {
            Text(buildAnnotatedString {
                append("My text ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("in italics.")
                }
            })
        }
    }
}
