package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun FaqScreen(
    onNavigateToMainScreen: () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("FAQ") }, navigationIcon = {
            IconButton(onClick = onNavigateToMainScreen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back to main screen"
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            val appName = stringResource(R.string.app_name)
            Text(
                "How it works & anti-features",
                Modifier.padding(top = Spacing.small),
                style = MaterialTheme.typography.headlineSmall
            )
            val paragraphStyle = MaterialTheme.typography.bodyMedium.copy(
                lineBreak = LineBreak.Paragraph
            )
            val codeStyle = SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace
            )
            val listItemIndent = 15.sp
            val listItemStyle = paragraphStyle.copy(
                textIndent = TextIndent(restLine = listItemIndent)
            )
            val listItemParagraphStyle = listItemStyle.copy(
                textIndent = TextIndent(
                    firstLine = listItemIndent, restLine = listItemIndent
                )
            )
            Text(
                buildAnnotatedString {
                    append("There are three scenarios how $appName turns a Google Maps URL into a geo: URI. Two of them ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("communicate with Google's servers")
                    }
                    append(".")
                }, style = paragraphStyle
            )
            Text(buildAnnotatedString {
                append("1. If the Google Maps URL already contains geographical coordinates, then it's parsed and no request to Google's servers is made. Example: ")
                withStyle(codeStyle) {
                    append(
                        "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398"
                    )
                }
            }, style = listItemStyle)
            Text(buildAnnotatedString {
                append("2. If the Google Maps URL doesn't contain geographical coordinates, then an HTTP GET request is made to the Google Maps URL and the coordinates are parsed from the HTML response. Example: ")
                withStyle(codeStyle) {
                    append("https://www.google.com/maps/place/Central+Park/")
                }
            }, style = listItemStyle)
            Text(
                "You can imagine it as such a command:",
                style = listItemParagraphStyle
            )
            Text(
                buildAnnotatedString {
                    withStyle(codeStyle) {
                        append("curl https://www.google.com/maps/place/Central+Park/ | grep -E '/@[0-9.,-]+'")
                    }
                }, style = listItemParagraphStyle
            )
            Text(
                "3. If the Google Maps URL is a short link, then an HTTP HEAD request is made to the short link. Then the full Google Maps URL is read from the response headers and we go to scenario 1. or 2. Notice that in case of scenario 2., another request will be made, so two requests in total.",
                style = listItemStyle
            )
            Text(
                "You can imagine it as such a command:",
                style = listItemParagraphStyle
            )
            Text(
                buildAnnotatedString {
                    withStyle(codeStyle) {
                        append("curl -I https://maps.app.goo.gl/TmbeHMiLEfTBws9EA | grep location:")
                    }
                },
                Modifier.padding(bottom = Spacing.small),
                style = listItemParagraphStyle
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FaqScreenPreview() {
    AppTheme {
        FaqScreen()
    }
}
