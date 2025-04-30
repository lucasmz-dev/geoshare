package page.ooooo.geoshare.lib

import android.os.Build
import androidx.compose.ui.platform.Clipboard
import page.ooooo.geoshare.data.UserPreferencesRepository

data class ConversionStateContext(
    val googleMapsUrlConverter: GoogleMapsUrlConverter,
    val intentTools: IntentTools,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val xiaomiTools: XiaomiTools,
    val clipboardTools: ClipboardTools = ClipboardTools(),
    val log: ILog = DefaultLog(),
    val onMessage: (message: Message) -> Unit,
    val getBuildVersionSdkInt: () -> Int = { Build.VERSION.SDK_INT },
) : StateContext() {
    override var currentState: State = Initial()
}
