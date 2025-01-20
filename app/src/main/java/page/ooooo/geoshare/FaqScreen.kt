package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.components.*
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
        val appName = stringResource(R.string.app_name)
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            FaqItem("How It Works and Privacy Considerations") {
                ParagraphText(buildAnnotatedString {
                    append("There are three scenarios how $appName turns a Google Maps URL into a geo: URI. Two of them ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("can connect to Google's servers")
                    }
                    append(".")
                })
                ListItemText(buildAnnotatedString {
                    append("1. If the Google Maps URL already contains geographic coordinates (for example ")
                    withStyle(codeStyle()) {
                        append(
                            "https://www.google.com/maps/place/Central+Park/data=!3d44.4490541!4d26.0888398"
                        )
                    }
                    append("), then it's parsed and no request to Google's servers is made.")
                })
                ListItemText(buildAnnotatedString {
                    append("2. If the Google Maps URL doesn't contain geographic coordinates (for example ")
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
                            ) { onNavigateToUserPreferencesScreen() }) {
                            append("Preferences")
                        }
                        append(" to permanently allow or deny connecting to Google instead of always asking, which is the default.")
                    },
                    Modifier.padding(bottom = Spacing.small),
                )
            }
            FaqItem("Compatibility with the GMaps WV App") {
                ParagraphText("It's not possible to share a link from GMaps WV with $appName, because GMaps WV already produces geo: links. So you don't need $appName and can directly share a location from GMaps WV with another map app.")
            }
        }
    }
}

@Composable
fun FaqItem(headline: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = Spacing.tiny)
                .clickable(
                    onClickLabel = if (expanded) "Collapse section" else "Expand section",
                    onClick = { expanded = !expanded },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(9f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.weight(1f),
            )
        }
        AnimatedVisibility(expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                content()
            }
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
