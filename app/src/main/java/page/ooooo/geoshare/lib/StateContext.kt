package page.ooooo.geoshare.lib

abstract class StateContext {
    private val maxIterations = 10

    abstract var currentState: State

    suspend fun transition() {
        var i = 0
        var newState = currentState
        while (i < maxIterations) {
            newState = newState.transition() ?: break
            i++
        }
        if (i >= maxIterations) {
            throw Exception("Exceeded max state iterations")
        }
        currentState = newState
    }
}
