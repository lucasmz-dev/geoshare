package page.ooooo.geoshare.data.local.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import page.ooooo.geoshare.components.RadioButtonGroup
import page.ooooo.geoshare.components.linkStyle
import page.ooooo.geoshare.ui.theme.Spacing

interface UserPreference<T> {
    val title: String
    val description: @Composable (onNavigateToFaqScreen: () -> Unit) -> Unit
    val key: Preferences.Key<String>
    val loading: T
    val default: T

    fun getValue(preferences: Preferences): T
    fun setValue(preferences: MutablePreferences, value: T)

    @Composable
    fun component(value: T, onValueChange: (T) -> Unit)
}

class NullableIntUserPreference(
    override val title: String,
    override val description: @Composable (onNavigateToFaqScreen: () -> Unit) -> Unit,
    override val key: Preferences.Key<String>,
    override val default: Int?,
    override val loading: Int?,
) : UserPreference<Int?> {
    override fun getValue(preferences: Preferences) =
        fromString(preferences[key])

    override fun setValue(preferences: MutablePreferences, value: Int?) {
        preferences[key] = value.toString()
    }

    @Composable
    override fun component(value: Int?, onValueChange: (Int?) -> Unit) {
        var inputValue by remember { mutableStateOf(value.toString()) }
        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                inputValue = it
                onValueChange(fromString(it))
            },
            modifier = Modifier.padding(top = Spacing.tiny),
        )
    }

    private fun fromString(value: String?): Int? = try {
        value?.toInt()
    } catch (_: NumberFormatException) {
        null
    } ?: default
}

class PermissionUserPreference(
    override val title: String,
    override val description: @Composable (onNavigateToFaqScreen: () -> Unit) -> Unit,
    override val key: Preferences.Key<String>,
    override val default: Permission,
    override val loading: Permission = default,
    val options: List<Pair<Permission, String>>,
) : UserPreference<Permission> {
    override fun getValue(preferences: Preferences) =
        preferences[key]?.let(Permission::valueOf) ?: default

    override fun setValue(preferences: MutablePreferences, value: Permission) {
        preferences[key] = value.name
    }

    @Composable
    override fun component(
        value: Permission,
        onValueChange: (Permission) -> Unit,
    ) {
        RadioButtonGroup(
            options = options,
            selectedValue = value,
            onSelect = { onValueChange(it) },
            modifier = Modifier.padding(top = Spacing.tiny),
        )
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

val lastRunVersionCode = NullableIntUserPreference(
    title = "Which version of the app was last run?",
    description = {},
    key = stringPreferencesKey("intro_shown_for_version_code"),
    loading = null,
    default = 0,
)

data class UserPreferencesValues(
    var connectToGooglePermissionValue: Permission = connectToGooglePermission.loading,
    var introShownForVersionCodeValue: Int? = lastRunVersionCode.loading,
)
