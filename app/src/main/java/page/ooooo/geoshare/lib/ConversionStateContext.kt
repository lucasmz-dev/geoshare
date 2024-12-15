package page.ooooo.geoshare.lib

import page.ooooo.geoshare.data.UserPreferencesRepository

data class ConversionStateContext(
    val googleMapsUrlConverter: GoogleMapsUrlConverter,
    val intentParser: IntentParser,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository,
    val xiaomiTools: XiaomiTools,
) : StateContext<ConversionState>() {
    override var currentState: State = Initial()
}
