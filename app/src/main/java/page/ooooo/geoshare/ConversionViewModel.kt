package page.ooooo.geoshare

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.UserPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.Initial
import page.ooooo.geoshare.lib.IntentParser
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.PermissionState
import page.ooooo.geoshare.lib.ReceivedIntent
import page.ooooo.geoshare.lib.ConversionStateContext
import page.ooooo.geoshare.lib.DismissedSharePermission
import page.ooooo.geoshare.lib.ReceivedGeoUri
import page.ooooo.geoshare.lib.State
import page.ooooo.geoshare.lib.XiaomiTools
import javax.inject.Inject

@HiltViewModel
class ConversionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val stateContext = ConversionStateContext(
        googleMapsUrlConverter = GoogleMapsUrlConverter(),
        intentParser = IntentParser(),
        networkTools = NetworkTools(),
        userPreferencesRepository = userPreferencesRepository,
        xiaomiTools = XiaomiTools(),
    )

    private val _currentState =
        MutableStateFlow<State>(Initial())
    val currentState: StateFlow<State> = _currentState

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPreferencesValues: StateFlow<UserPreferencesValues> =
        userPreferencesRepository.values.mapLatest { it }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesValues(),
        )

    fun start(intent: Intent) {
        transition(ReceivedIntent(stateContext, intent))
    }

    fun grant(doNotAsk: Boolean) {
        viewModelScope.launch {
            assert(stateContext.currentState is PermissionState)
            transition(
                (stateContext.currentState as PermissionState).grant(
                    doNotAsk
                )
            )
        }
    }

    fun deny(doNotAsk: Boolean) {
        viewModelScope.launch {
            assert(stateContext.currentState is PermissionState)
            transition(
                (stateContext.currentState as PermissionState).deny(
                    doNotAsk
                )
            )
        }
    }

    fun share(activity: Activity, geoUri: String, unchanged: Boolean) {
        transition(ReceivedGeoUri(stateContext, activity, geoUri, unchanged))
    }

    fun showSharePermissionsEditor(
        activity: Activity,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        onError: (message: String) -> Unit,
    ) {
        stateContext.xiaomiTools.showPermissionsEditor(
            activity,
            launcher,
            onError,
        )
    }

    fun retryShare() {
        viewModelScope.launch {
            stateContext.transition()
            _currentState.value = stateContext.currentState
        }
    }

    fun dismissShare() {
        transition(DismissedSharePermission())
    }

    private fun transition(newState: State) {
        viewModelScope.launch {
            stateContext.currentState = newState
            stateContext.transition()
            _currentState.value = stateContext.currentState
        }
    }

    fun <T> setUserPreferenceValue(
        userPreference: UserPreference<T>,
        value: T,
    ) {
        viewModelScope.launch {
            userPreferencesRepository.setValue(userPreference, value)
        }
    }
}
