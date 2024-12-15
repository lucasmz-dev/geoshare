package page.ooooo.geoshare.lib

import android.content.Intent
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import java.net.URL

open class ShareState : State {
    override suspend fun transition(): State? = null
}

class Initial : ShareState()

data class ReceivedIntent(
    val context: ShareStateContext,
    val intent: Intent,
) : ShareState() {
    override suspend fun transition(): State {
        val intentGeoUri = context.intentParser.getIntentGeoUri(intent)
        if (intentGeoUri != null) {
            return Succeeded(intentGeoUri, unchanged = true)
        }
        val url = context.intentParser.getIntentUrl(intent) ?: return Noop()
        return ReceivedUrl(context, url, null)
    }
}

data class ReceivedUrl(
    val context: ShareStateContext,
    val intentUrl: URL,
    val permission: Permission?,
) : ShareState() {
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
    val context: ShareStateContext,
    val intentUrl: URL,
) : ShareState(), PermissionState {
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
    val context: ShareStateContext,
    val intentUrl: URL,
) : ShareState() {
    override suspend fun transition(): State =
        context.networkTools.requestLocationHeader(intentUrl)?.let {
            UnshortenedUrl(context, it, Permission.ALWAYS)
        } ?: Failed("Failed to resolve short link")
}

class DeniedUnshortenPermission() : ShareState() {
    override suspend fun transition(): State =
        Failed("This link is not supported without connecting to Google")
}

data class UnshortenedUrl(
    val context: ShareStateContext,
    val url: URL,
    val permission: Permission?,
) : ShareState() {
    override suspend fun transition(): State {
        val geoUriBuilderFromUrl = context.googleMapsUrlConverter.parseUrl(url)
            ?: return Failed("Failed to create geo: link")
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
        return Succeeded(geoUriFromUrl)
    }
}

data class RequestedParseHtmlPermission(
    val context: ShareStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ShareState(), PermissionState {
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
    val context: ShareStateContext,
    val url: URL,
    val geoUriFromUrl: String,
) : ShareState() {
    override suspend fun transition(): State {
        val html = context.networkTools.getText(url)
            ?: return Failed("Failed to fetch Google Maps page")
        val geoUriBuilderFromHtml =
            context.googleMapsUrlConverter.parseHtml(html)
        if (geoUriBuilderFromHtml != null) {
            return Succeeded(geoUriBuilderFromHtml.toString())
        }
        val googleMapsUrl =
            context.googleMapsUrlConverter.parseGoogleSearchHtml(html)
        if (googleMapsUrl != null) {
            return ReceivedUrl(context, googleMapsUrl, Permission.ALWAYS)
        }
        return Succeeded(geoUriFromUrl)
    }
}

data class DeniedParseHtmlPermission(val geoUriFromUrl: String) : ShareState() {
    override suspend fun transition(): State =
        Succeeded(geoUriFromUrl)
}

data class Succeeded(val geoUri: String, val unchanged: Boolean = false) :
    ShareState()

data class Failed(val message: String) : ShareState()

class Noop() : ShareState()
