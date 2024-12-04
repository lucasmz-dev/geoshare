package page.ooooo.geoshare.lib

interface State {
    suspend fun transition(): State?
}

interface PermissionState {
    suspend fun grant(doNotAsk: Boolean): State
    suspend fun deny(doNotAsk: Boolean): State
}
