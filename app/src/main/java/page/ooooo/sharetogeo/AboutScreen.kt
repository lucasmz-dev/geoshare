package page.ooooo.sharetogeo

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.sharetogeo.ui.theme.Spacing
import page.ooooo.sharetogeo.ui.theme.AppTheme
import android.content.res.Configuration

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Application icon",
                modifier = Modifier
                    .size(192.dp)
                    .align(Alignment.CenterHorizontally)
            )
            val appName = stringResource(R.string.app_name)
            val appVersion = packageInfo?.versionName
            Text(
                appName + if (appVersion != null) " $appVersion" else "",
                style = MaterialTheme.typography.headlineLarge
            )
            Text("$appName is a noncommercial app made with passion by a sole developer.")
            Text(buildAnnotatedString {
                append("The ")
                withLink(
                    LinkAnnotation.Url(
                        "https://github.com/jakubvalenta/sharetogeo",
                        TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("code")
                }
                append(" is distributed under the free and open-source ")
                withLink(
                    LinkAnnotation.Url(
                        "https://www.gnu.org/licenses/gpl-3.0.txt",
                        TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("GPL 3.0")
                }
                append(" license.")
            })
            Text(buildAnnotatedString {
                append("Your feedback is welcome. You can reach me at ")
                withLink(
                    LinkAnnotation.Url(
                        "mailto:jakub@jakubvalenta.cz",
                        TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("jakub@jakubvalenta.cz")
                }
                append(".")
            })
            Text("If you enjoy using $appName, please donate to the project. Your donations keep me motivated.")
            Button(
                { uriHandler.openUri("https://ko-fi.com/jakubvalenta") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
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

// Previews

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen()
    }
}
