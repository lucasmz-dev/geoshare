package page.ooooo.geoshare.lib

import android.app.Activity
import android.content.Intent
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import java.net.URL

open class ConversionState : State {
    override suspend fun transition(): State? = null
}

class Initial : ConversionState()

data class ReceivedIntent(
    val context: ConversionStateContext,
    val intent: Intent,
) : ConversionState() {
    override suspend fun transition(): State {
        val intentGeoUri = context.intentParser.getIntentGeoUri(intent)
        if (intentGeoUri != null) {
            return ConversionSucceeded(intentGeoUri, unchanged = true)
        }
        val url = context.intentParser.getIntentUrl(intent) ?: return Noop()
        return ReceivedUrl(context, url, null)
    }
}

data class ReceivedUrl(
    val context: ConversionStateContext,
    val intentUrl: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val isShortUrl = context.googleMapsUrlConverter.isShortUrl(intentUrl)
        if (!isShortUrl) {
            return UnshortenedUrl(context, intentUrl, permission)
        }
        return when (permission ?: context.userPreferencesRepository.getValue(
            connectToGooglePermission
        )) {
            Permission.ALWAYS -> GrantedUnshortenPermission(context, intentUrl)

            Permission.ASK -> RequestedUnshortenPermission(context, intentUrl)

            Permission.NEVER -> DeniedUnshortenPermission()
        }
    }
}

data class RequestedUnshortenPermission(
    val context: ConversionStateContext,
    val intentUrl: URL,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            context.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedUnshortenPermission(context, intentUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            context.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedUnshortenPermission()
    }
}

data class GrantedUnshortenPermission(
    val context: ConversionStateContext,
    val intentUrl: URL,
) : ConversionState() {
    override suspend fun transition(): State =
        context.networkTools.requestLocationHeader(intentUrl)?.let {
            UnshortenedUrl(context, it, Permission.ALWAYS)
        } ?: ConversionFailed("Failed to resolve short link")
}

class DeniedUnshortenPermission() : ConversionState() {
    override suspend fun transition(): State =
        ConversionFailed("This link is not supported without connecting to Google")
}

data class UnshortenedUrl(
    val context: ConversionStateContext,
    val url: URL,
    val permission: Permission?,
) : ConversionState() {
    override suspend fun transition(): State {
        val geoUriBuilderFromUrl = context.googleMapsUrlConverter.parseUrl(url)
            ?: return ConversionFailed("Failed to create geo: link")
        var geoUriFromUrl = geoUriBuilderFromUrl.toString()
        if (geoUriBuilderFromUrl.coords.lat == "0" && geoUriBuilderFromUrl.coords.lon == "0") {
            return when (permission
                ?: context.userPreferencesRepository.getValue(
                    connectToGooglePermission
                )) {
                Permission.ALWAYS -> GrantedParseHtmlPermission(
                    context,
                    url,
                    geoUriFromUrl,
                )

                Permission.ASK -> RequestedParseHtmlPermission(
                    context,
                    url,
                    geoUriFromUrl,
                )

                Permission.NEVER -> DeniedParseHtmlPermission(geoUriFromUrl)
            }
        }
        return ConversionSucceeded(geoUriFromUrl)
    }
}

data class RequestedParseHtmlPermission(
    val context: ConversionStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState(), PermissionState {
    override suspend fun grant(doNotAsk: Boolean): State {
        if (doNotAsk) {
            context.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.ALWAYS,
            )
        }
        return GrantedParseHtmlPermission(context, url, geoUriFromUrl)
    }

    override suspend fun deny(doNotAsk: Boolean): State {
        if (doNotAsk) {
            context.userPreferencesRepository.setValue(
                connectToGooglePermission,
                Permission.NEVER,
            )
        }
        return DeniedParseHtmlPermission(geoUriFromUrl)
    }
}

data class GrantedParseHtmlPermission(
    val context: ConversionStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ConversionState() {
    override suspend fun transition(): State {
        val html = context.networkTools.getText(url)
            ?: return ConversionFailed("Failed to fetch Google Maps page")
        val geoUriBuilderFromHtml =
            context.googleMapsUrlConverter.parseHtml(html)
        if (geoUriBuilderFromHtml != null) {
            return ConversionSucceeded(geoUriBuilderFromHtml.toString())
        }
        val googleMapsUrl =
            context.googleMapsUrlConverter.parseGoogleSearchHtml(html)
        if (googleMapsUrl != null) {
            return ReceivedUrl(context, googleMapsUrl, Permission.ALWAYS)
        }
        return ConversionSucceeded(geoUriFromUrl)
    }
}

data class DeniedParseHtmlPermission(val geoUriFromUrl: String) :
    ConversionState() {
    override suspend fun transition(): State =
        ConversionSucceeded(geoUriFromUrl)
}

data class ConversionSucceeded(
    val geoUri: String,
    val unchanged: Boolean = false
) :
    ConversionState()

data class ConversionFailed(val message: String) : ConversionState()

data class ReceivedGeoUri(
    val context: ConversionStateContext,
    val activity: Activity,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State? =
        if (context.xiaomiTools.isBackgroundStartActivityPermissionGranted(
                activity,
            )
        ) {
            GrantedSharePermission(geoUri, unchanged)
        } else {
            RequestedSharePermission(context, activity, geoUri, unchanged)
        }
}

data class RequestedSharePermission(
    val context: ConversionStateContext,
    val activity: Activity,
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState() {
    override suspend fun transition(): State? =
        if (context.xiaomiTools.isBackgroundStartActivityPermissionGranted(
                activity
            )
        ) {
            GrantedSharePermission(geoUri, unchanged)
        } else {
            null
        }
}

data class GrantedSharePermission(
    val geoUri: String,
    val unchanged: Boolean,
) : ConversionState()

class DismissedSharePermission : ConversionState()

class Noop() : ConversionState()
