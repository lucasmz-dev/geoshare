package page.ooooo.sharetogeo

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.sharetogeo.ui.theme.AppTheme
import page.ooooo.sharetogeo.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToAboutScreen: () -> Unit = {}) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Share to Geo") },
                actions = {
                    Box {
                        IconButton(
                            modifier = Modifier.padding(end = Spacing.windowPadding - Spacing.builtInTopBarPaddingEnd),
                            onClick = { menuExpanded = true }
                        ) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToAboutScreen()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.medium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.share_to),
                    contentDescription = "Screenshot of a Google Maps link being shared with Share to Geo",
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                )
                Text(
                    "Go to Google Maps or a web browser and share a link with Share to Geo.",
                    Modifier.padding(Spacing.medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.share_from),
                    contentDescription = "Screenshot of Share to Geo sharing a geo: link",
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                )
                Text(
                    "Share to Geo will turn the link into a geo: URL and open it with one of your installed apps.",
                    Modifier.padding(Spacing.medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenPreview() {
    AppTheme {
        MainScreen()
    }
}
