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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.components.ListItemParagraphText
import page.ooooo.geoshare.components.ListItemText
import page.ooooo.geoshare.components.ParagraphText
import page.ooooo.geoshare.components.codeStyle
import page.ooooo.geoshare.components.linkStyle
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun FaqScreen(
    onNavigateToMainScreen: () -> Unit = {},
    onNavigateToUserPreferencesScreen: () -> Unit = {},
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
                Modifier.padding(top = Spacing.tiny),
                style = MaterialTheme.typography.headlineSmall,
            )
            ParagraphText(buildAnnotatedString {
                append("There are three scenarios how $appName turns a Google Maps URL into a geo: URI. Two of them ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("can connect to Google's servers")
                }
                append(".")
            })
            ListItemText(buildAnnotatedString {
                append("1. If the Google Maps URL already contains geographical coordinates (for example ")
                withStyle(codeStyle()) {
                    append(
                        "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398"
                    )
                }
                append("), then it's parsed and no request to Google's servers is made.")
            })
            ListItemText(buildAnnotatedString {
                append("2. If the Google Maps URL doesn't contain geographical coordinates (for example ")
                withStyle(codeStyle()) {
                    append(
                        "https://www.google.com/maps/place/Central+Park/"
                    )
                }
                append("), then $appName asks you if it can connect to Google.")
            })
            ListItemParagraphText(buildAnnotatedString {
                append("If you allow connecting to Google, then $appName makes an ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("HTTP GET request")
                }
                append(" to Google Maps and parses the coordinates from the HTML response. You can imagine it as ")
                withStyle(codeStyle()) {
                    append("curl https://www.google.com/maps/place/Central+Park/ | grep -E '/@[0-9.,-]+'")
                }
                append(".")
            })
            ListItemParagraphText(buildAnnotatedString {
                append("If you don't allow connecting to Google, then $appName creates a geo: link with a place search term (for example ")
                withStyle(codeStyle()) {
                    append("geo:0,0?q=Central%20Park")
                }
                append(").")
            })
            ListItemText(buildAnnotatedString {
                append("3. If the Google Maps URL is a short link (for example ")
                withStyle(codeStyle()) {
                    append("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA")
                }
                append("), then $appName asks you if it can connect to Google.")
            })
            ListItemParagraphText(buildAnnotatedString {
                append("If you allow connecting to Google, then $appName makes an ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("HTTP HEAD request")
                }
                append(" to the short link and reads the full link from the response headers. You can imagine it as ")
                withStyle(codeStyle()) {
                    append("curl -I https://maps.app.goo.gl/TmbeHMiLEfTBws9EA | grep location:")
                }
                append(". Then $appName continues with scenario 1. or 2., depending on whether the full link contains coordinates or not. In case of scenario 2., another connection to Google will be made, but this time without asking.")
            })
            ListItemParagraphText("If you don't allow connecting to Google, then $appName cancels the creation of the geo: link.")
            ParagraphText(
                buildAnnotatedString {
                    append("Go to ")
                    withLink(
                        LinkAnnotation.Clickable(
                            "preferences",
                            TextLinkStyles(linkStyle()),
                        ) { onNavigateToUserPreferencesScreen() }
                    ) {
                        append("Preferences")
                    }
                    append(" to permanently allow or deny connecting to Google instead of always asking, which is the default.")
                },
                Modifier.padding(bottom = Spacing.small),
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        FaqScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        FaqScreen()
    }
}
