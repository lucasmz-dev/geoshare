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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.components.RadioButtonGroup
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun UserPreferencesScreen(
    onNavigateToMainScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    viewModel: ShareViewModel = hiltViewModel(),
) {
    val values by viewModel.userPreferencesValues.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Preferences") }, navigationIcon = {
            IconButton(onClick = onNavigateToMainScreen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back to main screen",
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
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            UserPreferencesItem(
                viewModel,
                connectToGooglePermission,
                values.connectToGooglePermissionValue,
                onNavigateToFaqScreen,
                Modifier.padding(top = Spacing.tiny),
            )
        }
    }
}

@Composable
fun <T> UserPreferencesItem(
    viewModel: ShareViewModel,
    userPreference: UserPreference<T>,
    value: T,
    onNavigateToFaqScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            userPreference.title,
            Modifier.padding(bottom = Spacing.small),
            style = MaterialTheme.typography.bodyLarge,
        )
        userPreference.description(onNavigateToFaqScreen)
        RadioButtonGroup(
            options = userPreference.options,
            selectedValue = value,
            onSelect = { viewModel.setUserPreferenceValue(userPreference, it) },
            modifier = Modifier.padding(top = Spacing.tiny)
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ShareViewModel(FakeUserPreferencesRepository())
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ShareViewModel(FakeUserPreferencesRepository())
        )
    }
}
