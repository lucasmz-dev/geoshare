package page.ooooo.geoshare.lib

import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class XiaomiTools {

    /**
     * See [GitHub Gist](https://gist.github.com/starry-shivam/901267c26eb030eb3faf1ccd4d2bdd32)
     */
    fun isMiuiDevice(): Boolean =
        setOf("xiaomi", "redmi", "poco").contains(Build.BRAND.lowercase()) &&
            (!getRuntimeProperty("ro.miui.ui.version.name").isNullOrBlank() ||
                !getRuntimeProperty("ro.mi.os.version.name").isNullOrBlank())

    private fun getRuntimeProperty(property: String): String? = try {
        Runtime.getRuntime()
            .exec("getprop $property").inputStream.use { input ->
                BufferedReader(InputStreamReader(input), 1024).readLine()
            }
    } catch (_: IOException) {
        null
    }

    /**
     * See [Stack Overflow](https://stackoverflow.com/a/77842542)
     */
    fun isBackgroundStartActivityPermissionGranted(context: Context): Boolean =
        if (isMiuiDevice()) {
            try {
                val appOpsManager =
                    context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
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
                    context.packageName,
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
    fun showPermissionEditor(
        context: Context,
        settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    ): Boolean {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            putExtra(
                "extra_package_uid", Process.myUid()
            )
            @Suppress("SpellCheckingInspection") putExtra(
                "extra_pkgname", context.packageName
            )
            putExtra("extra_package_name", context.packageName)
        }
        return try {
            settingsLauncherWrapper.launcher.launch(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
