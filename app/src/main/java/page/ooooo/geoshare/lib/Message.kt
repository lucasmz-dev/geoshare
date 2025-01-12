package page.ooooo.geoshare.lib

data class Message(
    val text: String,
    val type: Type = Type.SUCCESS,
) {
    enum class Type { SUCCESS, ERROR }
}
