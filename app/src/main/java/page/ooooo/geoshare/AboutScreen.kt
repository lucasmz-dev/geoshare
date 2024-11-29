package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun AboutScreen(
    onNavigateToMainScreen: () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("About") }, navigationIcon = {
            IconButton(onClick = onNavigateToMainScreen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back to main screen"
                )
            }
        })
    }) { innerPadding ->
        val context = LocalContext.current
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.applicationInfo.packageName, 0
        )
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Application icon",
                modifier = Modifier
                    .size(144.dp)
                    .align(Alignment.CenterHorizontally),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                val appName = stringResource(R.string.app_name)
                val appVersion = packageInfo?.versionName
                Text(
                    appName + if (appVersion != null) " $appVersion" else "",
                    style = MaterialTheme.typography.headlineSmall
                )
                val paragraphStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph
                )
                val linkStyle = SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
                Text(buildAnnotatedString {
                    append("$appName is a free and open-source app distributed under the ")
                    withLink(
                        LinkAnnotation.Url(
                            "https://www.gnu.org/licenses/gpl-3.0.txt",
                            TextLinkStyles(linkStyle)
                        )
                    ) {
                        append("GPL 3.0")
                    }
                    append(" or later license.")
                }, style = paragraphStyle)
                Text(buildAnnotatedString {
                    append("You can find the code at ")
                    withLink(
                        LinkAnnotation.Url(
                            "https://github.com/jakubvalenta/geoshare",
                            TextLinkStyles(linkStyle)
                        )
                    ) {
                        append("GitHub")
                    }
                    append(", where you can also report ")
                    withLink(
                        LinkAnnotation.Url(
                            "https://github.com/jakubvalenta/geoshare/issues",
                            TextLinkStyles(linkStyle)
                        )
                    ) {
                        append("issues")
                    }
                    append(".")
                }, style = paragraphStyle)
                Text(buildAnnotatedString {
                    append("Your feedback is welcome. You can reach me at ")
                    withLink(
                        LinkAnnotation.Url(
                            "mailto:jakub@jakubvalenta.cz",
                            TextLinkStyles(linkStyle)
                        )
                    ) {
                        append("jakub@jakubvalenta.cz")
                    }
                    append(".")
                }, style = paragraphStyle)
                Text(
                    "If you enjoy using $appName, please donate to my Ko-Fi account. Your donations keep me motivated to work on the project.",
                    style = paragraphStyle
                )
                Button(
                    { uriHandler.openUri("https://ko-fi.com/jakubvalenta") },
                    Modifier.padding(bottom = Spacing.small),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("donate ")
                    Icon(
                        painterResource(R.drawable.open_in_new_24px),
                        contentDescription = "External link",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen()
    }
}
