package page.ooooo.geoshare.lib

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import java.net.MalformedURLException
import java.net.URL

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

class Initial : ConversionState()

data class ReceivedIntent(
    val stateContext: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        if (stateContext.intentTools.isProcessed(intent)) {
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_nothing_to_do
            )
        }
        val geoUri = stateContext.intentTools.getIntentGeoUri(intent)
        if (geoUri != null) {
            return ConversionSucceeded(geoUri, true)
        }
        val urlString = stateContext.intentTools.getIntentUrlString(intent)
            ?: return ConversionFailed(
                stateContext,
                R.string.conversion_failed_missing_url
            )
        return ReceivedUrlString(stateContext, urlString, null)
    }
}

data class ReceivedUriString(
    val stateContext: ConversionStateContext,
    val uriString: String,
    private val parseUri: (String) -> Uri = { s -> Uri.parse(s) },
) : ConversionState() {
    override suspend fun transition(): State {
        val uri = parseUri(uriString)
        if (uri.scheme == "geo") {
            return ConversionSucceeded(uriString, true)
        }
        return ReceivedUrlString(stateContext, uriString, null)
    }
}

data class ReceivedUrlString(
    val stateContext: ConversionStateContext,
    val urlString: String,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val urlStringWithScheme = urlString.replace(
            "^(https:)?(//)?(.)".toRegex(),
            "https://$3",
        )
        val url = try {
            URL(urlStringWithScheme)
        } catch (_: MalformedURLException) {
            return ConversionFailed(
                stateContext,
                R.string.conversion_failed_invalid_url
            )
        }
        return ReceivedUrl(stateContext, url, permission)
    }
}

data class ReceivedUrl(
    val stateContext: ConversionStateContext,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val isShortUrl = stateContext.googleMapsUrlConverter.isShortUrl(url)
        if (!isShortUrl) {
            return UnshortenedUrl(stateContext, url, permission)
        }
        return when (permission
            ?: stateContext.userPreferencesRepository.getValue(
                connectToGooglePermission
            )) {
            Permission.ALWAYS -> GrantedUnshortenPermission(stateContext, url)

            Permission.ASK -> RequestedUnshortenPermission(stateContext, url)

            Permission.NEVER -> DeniedUnshortenPermission(stateContext)
        }
    }
}

data class RequestedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val url: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedUnshortenPermission(stateContext, url)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedUnshortenPermission(stateContext)
    }
}

data class GrantedUnshortenPermission(
    val stateContext: ConversionStateContext,
    val url: URL,
) : ConversionState() {
    override suspend fun transition(): State =
        stateContext.networkTools.requestLocationHeader(url)?.let {
            UnshortenedUrl(stateContext, it, Permission.ALWAYS)
        } ?: ConversionFailed(
            stateContext,
            R.string.conversion_failed_unshorten_network_error
        )
}

class DeniedUnshortenPermission(
    val stateContext: ConversionStateContext,
) : ConversionState() {
    override suspend fun transition(): State = ConversionFailed(
        stateContext,
        R.string.conversion_failed_unshorten_permission_denied,
    )
}

data class UnshortenedUrl(
    val stateContext: ConversionStateContext,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val geoUriBuilderFromUrl =
            stateContext.googleMapsUrlConverter.parseUrl(url)
                ?: return ConversionFailed(
                    stateContext,
                    R.string.conversion_failed_parse_url_error
                )
        val geoUriFromUrl = geoUriBuilderFromUrl.toString()
        if (geoUriBuilderFromUrl.coords.lat == "0" && geoUriBuilderFromUrl.coords.lon == "0") {
            return when (permission
                ?: stateContext.userPreferencesRepository.getValue(
                    connectToGooglePermission
                )) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriFromUrl,
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriFromUrl,
                )

                Permission.NEVER -> DeniedParseHtmlPermission(geoUriFromUrl)
            }
        }
        return ConversionSucceeded(geoUriFromUrl, false)
    }
}

data class RequestedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedParseHtmlPermission(stateContext, url, geoUriFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            stateContext.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedParseHtmlPermission(geoUriFromUrl)
    }
}

data class GrantedParseHtmlPermission(
    val stateContext: ConversionStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State {
        val html =
            stateContext.networkTools.getText(url) ?: return ConversionFailed(
                stateContext,
                R.string.conversion_failed_parse_html_error,
            )
        val geoUriBuilderFromHtml =
            stateContext.googleMapsUrlConverter.parseHtml(html)
        if (geoUriBuilderFromHtml != null) {
            return ConversionSucceeded(geoUriBuilderFromHtml.toString(), false)
        }
        val googleMapsUrl =
            stateContext.googleMapsUrlConverter.parseGoogleSearchHtml(html)
        if (googleMapsUrl != null) {
            return ReceivedUrl(stateContext, googleMapsUrl, Permission.ALWAYS)
        }
        return ConversionSucceeded(geoUriFromUrl, false)
    }
}

data class DeniedParseHtmlPermission(
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State =
        ConversionSucceeded(geoUriFromUrl, false)
}

data class ConversionSucceeded(
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState()

data class ConversionFailed(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.ERROR))
        return null
    }
}

data class AcceptedSharing(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State =
        if (stateContext.xiaomiTools.isBackgroundStartActivityPermissionGranted(
                context
            )
        ) {
            GrantedSharePermission(
                stateContext,
                context,
                geoUri,
                unchanged,
            )
        } else {
            RequestedSharePermission(
                stateContext,
                context,
                settingsLauncherWrapper,
                geoUri,
                unchanged,
            )
        }
}

data class RequestedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State =
        if (stateContext.xiaomiTools.showPermissionEditor(
                context,
                settingsLauncherWrapper,
            )
        ) {
            ShowedSharePermissionEditor(
                stateContext,
                context,
                settingsLauncherWrapper,
                geoUri,
                unchanged,
            )
        } else {
            SharingFailed(
                stateContext,
                R.string.sharing_failed_xiaomi_permission_show_editor_error
            )
        }

    override suspend fun deny(doNotAsk: Boolean): State =
        DismissedSharePermissionEditor()
}

data class ShowedSharePermissionEditor(
    val stateContext: ConversionStateContext,
    val context: Context,
    val settingsLauncherWrapper: ManagedActivityResultLauncherWrapper,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState(), PermissionState {
    /**
     * Share again after the permission editor has been closed.
     */
    override suspend fun grant(doNotAsk: Boolean): State =
        AcceptedSharing(
            stateContext,
            context,
            settingsLauncherWrapper,
            geoUri,
            unchanged,
        )

    override suspend fun deny(doNotAsk: Boolean): State {
        throw NotImplementedError("It is not possible to deny sharing again after the permission editor has been closed")
    }
}

class DismissedSharePermissionEditor : ConversionState()

data class GrantedSharePermission(
    val stateContext: ConversionStateContext,
    val context: Context,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State? = try {
        stateContext.intentTools.share(context, Intent.ACTION_VIEW, geoUri)
        SharingSucceeded(
            stateContext,
            if (unchanged) R.string.sharing_succeeded_unchanged else R.string.sharing_succeeded
        )
    } catch (_: ActivityNotFoundException) {
        SharingFailed(
            stateContext,
            R.string.sharing_failed_activity_not_found,
        )
    }
}

data class SharingSucceeded(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.SUCCESS))
        return null
    }
}

data class SharingFailed(
    val stateContext: ConversionStateContext,
    val messageResId: Int,
) : ConversionState() {
    override suspend fun transition(): State? {
        stateContext.onMessage(Message(messageResId, Message.Type.ERROR))
        return null
    }
}

data class AcceptedCopying(
    val stateContext: ConversionStateContext,
    val clipboardManager: ClipboardManager,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State {
        clipboardManager.setText(AnnotatedString(geoUri))
        return CopyingFinished(stateContext, unchanged)
    }
}

data class CopyingFinished(
    val stateContext: ConversionStateContext,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State? {
        val systemHasClipboardEditor =
            stateContext.getBuildVersionSdkInt() >= Build.VERSION_CODES.TIRAMISU
        if (!systemHasClipboardEditor) {
            stateContext.onMessage(
                Message(
                    if (unchanged) R.string.copying_finished_unchanged else R.string.copying_finished,
                    Message.Type.SUCCESS
                )
            )
        }
        return null
    }
}
