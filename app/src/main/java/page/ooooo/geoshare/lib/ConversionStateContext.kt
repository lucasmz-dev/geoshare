package page.ooooo.geoshare.lib

import android.os.Build
import page.ooooo.geoshare.data.UserPreferencesRepository

data class ConversionStateContext(
    val googleMapsUrlConverter: GoogleMapsUrlConverter,
    val intentTools: IntentTools,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val xiaomiTools: XiaomiTools,
    val log: ILog = DefaultLog(),
    val onMessage: (message: Message) -> Unit,
    val getBuildVersionSdkInt: () -> Int = { Build.VERSION.SDK_INT },
) : StateContext() {
    override var currentState: State = Initial()
}
