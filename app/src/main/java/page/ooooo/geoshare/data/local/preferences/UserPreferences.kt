package page.ooooo.geoshare.data.local.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import page.ooooo.geoshare.R
import page.ooooo.geoshare.components.ParagraphText
import page.ooooo.geoshare.components.linkStyle

interface UserPreference<T> {
    val title: String
    val description: @Composable (onNavigateToFaqScreen: () -> Unit) -> Unit
    val key: Preferences.Key<String>
    val default: T
    val options: List<Pair<T, String>>

    fun getValue(preferences: Preferences): T
    fun setValue(preference: MutablePreferences, value: T)
}

class PermissionUserPreference(
    override val title: String,
    override val description: @Composable (onNavigateToFaqScreen: () -> Unit) -> Unit,
    override val key: Preferences.Key<String>,
    override val default: Permission,
    override val options: List<Pair<Permission, String>>,
) : UserPreference<Permission> {
    override fun getValue(preferences: Preferences) =
        preferences[key]?.let(Permission::valueOf) ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission) {
        preferences[key] = value.name
    }
}

val connectToGooglePermission = PermissionUserPreference(
    title = "Allow connecting to Google?",
    description = { onNavigateToFaqScreen ->
        val appName = stringResource(R.string.app_name)
        ParagraphText(buildAnnotatedString {
            append("$appName can connect to Google to support short Google Maps links, and to get coordinates from the Google Maps page when the link doesn't contain them. For more information, see the ")
            withLink(
                LinkAnnotation.Clickable(
                    "faq",
                    TextLinkStyles(linkStyle()),
                ) { onNavigateToFaqScreen() }
            ) {
                append("FAQ")
            }
            append(".")
        })
    },
    key = stringPreferencesKey("connect_to_google_permission"),
    default = Permission.ASK,
    options = listOf(
        Pair(
            Permission.ALWAYS,
            "Yes"
        ),
        Pair(
            Permission.ASK,
            "Ask before connecting"
        ),
        Pair(
            Permission.NEVER,
            "No"
        ),
    ),
)

data class UserPreferencesValues(
    var connectToGooglePermissionValue: Permission = connectToGooglePermission.default
)
