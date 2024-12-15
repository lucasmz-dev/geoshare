package page.ooooo.geoshare.lib

import android.app.Activity
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import java.util.Locale

class XiaomiTools {

    /**
     * See [Stack Overflow](https://stackoverflow.com/a/77842542)
     */
    fun isBackgroundStartActivityPermissionGranted(activity: Activity): Boolean =
        if (Build.MANUFACTURER.lowercase(Locale.ROOT) == "xiaomi") {
            try {
                val appOpsManager =
                    activity.getSystemService(APP_OPS_SERVICE) as AppOpsManager
                val opBackgroundStartActivity = 10021
                val checkOpNoThrow = AppOpsManager::class.java.getMethod(
                    "checkOpNoThrow",
                    Int::class.java,
                    Int::class.java,
                    String::class.java,
                )
                val result = checkOpNoThrow.invoke(
                    appOpsManager,
                    opBackgroundStartActivity,
                    Process.myUid(),
                    activity.packageName,
                )
                result == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                Log.e(null, Log.getStackTraceString(e))
                false
            }
        } else {
            true
        }

    /**
     * See [GitHub](https://github.com/zoontek/react-native-permissions/issues/412#issuecomment-1590376224)
     */
    fun showPermissionsEditor(
        activity: Activity,
        settingsLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        onError: (message: String) -> Unit,
    ) {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            putExtra(
                "extra_package_uid", Process.myUid()
            )
            @Suppress("SpellCheckingInspection") putExtra(
                "extra_pkgname", activity.packageName
            )
            putExtra("extra_package_name", activity.packageName)
        }
        try {
            settingsLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            onError("Failed to open permission settings")
        }
    }
}
