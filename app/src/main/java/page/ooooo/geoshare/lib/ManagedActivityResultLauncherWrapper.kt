package page.ooooo.geoshare.lib

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult

/**
 * Wrap `ManagedActivityResultLauncher<Intent, ActivityResult>`, so that we can
 * mock it, because Mockito in Kotlin doesn't seem to support mocking generic
 * classes.
 */
data class ManagedActivityResultLauncherWrapper(
    val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
)
