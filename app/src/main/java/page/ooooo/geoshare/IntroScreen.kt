package page.ooooo.geoshare

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.components.ParagraphText
import page.ooooo.geoshare.components.codeStyle
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IntroScreen(
    initialPage: Int = 0,
    onCloseIntro: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)
    val pageCount = 3
    var page by remember { mutableStateOf(initialPage) }
    val animatedProgress by animateFloatAsState(
        targetValue = (page + 1f) / pageCount,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        // Do nothing.
    }

    fun showOpenByDefaultSettings(packageName: String) {
        try {
            val action =
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    // Samsung supposedly doesn't allow going to the "Open by
                    // default" settings page.
                    Build.MANUFACTURER.lowercase(Locale.ROOT) != "samsung"
                ) {
                    Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                } else {
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                }
            val intent = Intent(action, Uri.parse("package:$packageName"))
            settingsLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Failed to open settings",
                Toast.LENGTH_LONG,
            )
                .show()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                { animatedProgress },
                Modifier.padding(vertical = Spacing.tiny),
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                when (page) {
                    0 -> IntroPage(
                        "How to View Google Maps Locations in Other Map Apps",
                        page,
                    ) {
                        IntroFigure(
                            R.drawable.google_maps_share,
                            "Screenshot of Google Maps' share screen",
                            AnnotatedString("Share a location from Google Maps app or from your web browser.")
                        )
                        IntroFigure(
                            R.drawable.geo_share_open,
                            "Screenshot of $appName's share screen",
                            buildAnnotatedString {
                                append("Choose ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Share geo:")
                                }
                                append(" and Geo Share will let you open the location in any installed map app.")
                            }
                        )
                    }

                    1 -> IntroPage(
                        "Configure Android to Offer Alternative Maps for Google Links (Optional)",
                        page,
                    ) {
                        IntroFigure(
                            R.drawable.open_by_default_google_maps,
                            "Screenshot of Google Maps' Open By default settings",
                            buildAnnotatedString {
                                append("First, go to Settings > Apps > Maps > ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Open by default")
                                }
                                append(" and disable Maps from opening links automatically.")
                            },
                        ) {
                            OutlinedButton({
                                showOpenByDefaultSettings("com.google.android.apps.maps")
                            }) {
                                Text("Open Google Maps settings")
                            }
                        }
                        IntroFigure(
                            R.drawable.open_by_default_geo_share,
                            "Screenshot of $appName's Open By default settings",
                            buildAnnotatedString {
                                append("Then go to Settings > Apps > $appName > ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Open by default")
                                }
                                append(" > ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Add links")
                                }
                                append(" and enable $appName to handle its supported links.")
                            },
                        ) {
                            OutlinedButton({
                                showOpenByDefaultSettings(context.packageName)
                            }) {
                                Text("Open $appName settings")
                            }
                            ParagraphText(
                                buildAnnotatedString {
                                    append("Make sure to select at least ")
                                    withStyle(codeStyle()) {
                                        append("maps.google.com")
                                    }
                                    append(" and ")
                                    withStyle(codeStyle()) {
                                        append("maps.app.goo.gl")
                                    }
                                    append(". If some links are grayed out, other Google apps may be set as their default handlers. You can locate these apps and adjust their settings as we did with Google Maps.")
                                },
                                Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    2 -> IntroPage(
                        "How to Create geo: Links",
                        page,
                    ) {
                        IntroFigure(
                            R.drawable.google_maps_copy,
                            "Screenshot of Google Maps' share screen",
                            buildAnnotatedString {
                                append("Select ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append("Copy geo:")
                                }
                                append(" when sharing from Google Maps.")
                            },
                        )
                        IntroFigure(
                            R.drawable.geo_share_main,
                            "Screenshot $appName's geo: link conversion form",
                            AnnotatedString("Or simply open $appName and paste your Google Maps link."),
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.tiny),
            ) {
                if (page != pageCount - 1) {
                    TextButton({ onCloseIntro() }) {
                        Text("Close")
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    {
                        if (page != pageCount - 1) {
                            coroutineScope.launch {
                                scrollState.scrollTo(0)
                                page++
                            }
                        } else {
                            onCloseIntro()
                        }
                    },
                    Modifier.testTag("geoShareIntroScreenNextButton"),
                ) {
                    Text(
                        if (page != pageCount - 1) {
                            "Next"
                        } else {
                            "Get started"
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun IntroPage(
    headline: String,
    page: Int,
    content: @Composable () -> Unit = {},
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            headline,
            Modifier
                .testTag("geoShareIntroPage${page}HeadingText")
                .padding(vertical = Spacing.small),
            style = MaterialTheme.typography.headlineSmall,
        )
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            content()
        }
    }
}

@Composable
fun IntroFigure(
    drawableId: Int,
    contentDescription: String,
    caption: AnnotatedString,
    content: @Composable () -> Unit = {},
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
    ) {
        ParagraphText(caption, Modifier.fillMaxWidth())
        Image(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .padding(horizontal = Spacing.large)
                .clip(MaterialTheme.shapes.medium),
        )
        content()
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun PageOnePreview() {
    AppTheme {
        IntroScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageOnePreview() {
    AppTheme {
        IntroScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun PageTwoPreview() {
    AppTheme {
        IntroScreen(initialPage = 1)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageTwoPreview() {
    AppTheme {
        IntroScreen(initialPage = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun PageThreePreview() {
    AppTheme {
        IntroScreen(initialPage = 2)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPageThreePreview() {
    AppTheme {
        IntroScreen(initialPage = 2)
    }
}
