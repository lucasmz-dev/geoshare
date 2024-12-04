package page.ooooo.geoshare.lib

import page.ooooo.geoshare.data.UserPreferencesRepository

data class ShareStateContext(
    val googleMapsUrlConverter: GoogleMapsUrlConverter,
    val intentParser: IntentParser,
    val networkTools: NetworkTools,
    val userPreferencesRepository: UserPreferencesRepository
) : StateContext<ShareState>() {
    override var currentState: State = Initial()
}
