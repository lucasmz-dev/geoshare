package page.ooooo.geoshare

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import page.ooooo.geoshare.data.local.preferences.lastRunVersionCode
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun UserPreferencesScreen(
    onNavigateToMainScreen: () -> Unit = {},
    onNavigateToFaqScreen: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel(),
) {
    val userPreferencesValues by viewModel.userPreferencesValues.collectAsStateWithLifecycle()
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
                userPreferencesValues.connectToGooglePermissionValue,
                onNavigateToFaqScreen,
                Modifier.padding(top = Spacing.tiny),
            )
            if (BuildConfig.DEBUG) {
                UserPreferencesItem(
                    viewModel,
                    lastRunVersionCode,
                    userPreferencesValues.introShownForVersionCodeValue,
                    onNavigateToFaqScreen,
                )
            }
        }
    }
}

@Composable
fun <T> UserPreferencesItem(
    viewModel: ConversionViewModel,
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
        userPreference.component(value) {
            viewModel.setUserPreferenceValue(userPreference, it)
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        UserPreferencesScreen(
            viewModel = ConversionViewModel(
                FakeUserPreferencesRepository(),
                SavedStateHandle(),
            )
        )
    }
}
