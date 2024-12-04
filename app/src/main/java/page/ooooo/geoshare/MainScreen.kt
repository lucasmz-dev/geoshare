package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAboutScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    onNavigateToUserPreferencesScreen: () -> Unit = {},
) {
    val appName = stringResource(R.string.app_name)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(appName) },
                actions = {
                    Box {
                        IconButton(
                            modifier = Modifier.padding(end = Spacing.windowPadding - Spacing.builtInTopBarPaddingEnd),
                            onClick = { menuExpanded = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Preferences") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToUserPreferencesScreen()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("FAQ") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToFaqScreen()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToAboutScreen()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(
                    horizontal = Spacing.windowPadding
                )
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Column(
                Modifier.padding(top = Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    "Go to Google Maps or a web browser and share a link with $appName:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.share_to_dark else R.drawable.share_to_light),
                    contentDescription = "Screenshot of a Google Maps link being shared with $appName",
                    modifier = Modifier
                        .padding(horizontal = Spacing.large)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    "$appName will turn the link into a geo: link and open it with one of your installed apps:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.share_from_dark else R.drawable.share_from_light),
                    contentDescription = "Screenshot of $appName sharing a geo: link",
                    modifier = Modifier
                        .padding(horizontal = Spacing.large)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Card(
                Modifier.padding(bottom = Spacing.small),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Row(
                    Modifier.Companion.padding(Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.lightbulb_24px), null)
                    Text(
                        buildAnnotatedString {
                            append("$appName supports many Google Maps URL formats. Still, if you find a link that doesn't work, please report an ")
                            withLink(
                                LinkAnnotation.Url(
                                    "https://github.com/jakubvalenta/geoshare/issues",
                                    TextLinkStyles(
                                        style = SpanStyle(
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                )
                            ) {
                                append("issue")
                            }
                            append(".")
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainScreen()
    }
}
