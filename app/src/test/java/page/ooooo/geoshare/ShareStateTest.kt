package page.ooooo.geoshare

import android.content.Intent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.connectToGooglePermission
import page.ooooo.geoshare.lib.DeniedUnshortenPermission
import page.ooooo.geoshare.lib.DeniedParseHtmlPermission
import page.ooooo.geoshare.lib.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.Failed
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.GeoUriBuilder
import page.ooooo.geoshare.lib.GeoUriCoords
import page.ooooo.geoshare.lib.GeoUriParams
import page.ooooo.geoshare.lib.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.GrantedParseHtmlPermission
import page.ooooo.geoshare.lib.GrantedUnshortenPermission
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.IntentParser
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Noop
import page.ooooo.geoshare.lib.ReceivedIntent
import page.ooooo.geoshare.lib.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.ShareStateContext
import page.ooooo.geoshare.lib.Succeeded
import page.ooooo.geoshare.lib.UnshortenedUrl
import java.net.URL

class ShareStateTest {

    private lateinit var fakeLog: ILog
    private lateinit var fakeUriQuote: FakeUriQuote
    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter
    private lateinit var mockIntentParser: IntentParser
    private lateinit var mockNetworkTools: NetworkTools
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository

    @Before
    fun before() = runTest {
        fakeLog = FakeLog()
        fakeUriQuote = FakeUriQuote()
        googleMapsUrlConverter = GoogleMapsUrlConverter(fakeLog, fakeUriQuote)

        mockIntentParser = Mockito.mock(IntentParser::class.java)
        Mockito.`when`(mockIntentParser.getIntentGeoUri(any<Intent>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockIntentParser.getIntentUrl(any<Intent>()))
            .thenThrow(NotImplementedError::class.java)

        mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>()))
            .thenThrow(NotImplementedError::class.java)

        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @Test
    fun receivedIntent_intentContainsGeoUri_returnsSucceededUnchanged() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn("geo:1,2?q=fromIntent")
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = ReceivedIntent(shareStateContext, intent)
            assertEquals(
                Succeeded("geo:1,2?q=fromIntent", unchanged = true),
                shareState.transition(),
            )
        }

    @Test
    fun receivedIntent_intentDoesNotContainUrl_returnsNoop() = runTest {
        val intent = Intent()
        val mockIntentParser = Mockito.mock(IntentParser::class.java)
        Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
            .thenReturn(null)
        Mockito.`when`(mockIntentParser.getIntentUrl(intent)).thenReturn(null)
        val shareStateContext = ShareStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
        )
        val shareState = ReceivedIntent(shareStateContext, intent)
        assertTrue(shareState.transition() is Noop)
    }

    @Test
    fun receivedIntent_intentDoesNotContainShortUrl_returnsUnshortenedUrl() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentParser.getIntentUrl(intent))
                .thenReturn(URL("https://maps.google.com/foo"))
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = ReceivedIntent(shareStateContext, intent)
            assertEquals(
                UnshortenedUrl(
                    shareStateContext,
                    URL("https://maps.google.com/foo"),
                    Permission.ASK,
                ),
                shareState.transition(),
            )
        }

    @Test
    fun receivedIntent_intentContainsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentParser.getIntentUrl(intent))
                .thenReturn(URL("https://maps.app.goo.gl/foo"))
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ALWAYS)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = ReceivedIntent(
                shareStateContext,
                intent,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    shareStateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                shareState.transition(),
            )
        }

    @Test
    fun receivedIntent_intentContainsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentParser.getIntentUrl(intent))
                .thenReturn(URL("https://maps.app.goo.gl/foo"))
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ASK)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = ReceivedIntent(
                shareStateContext,
                intent,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    shareStateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                shareState.transition(),
            )
        }

    @Test
    fun receivedIntent_intentContainsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentParser.getIntentUrl(intent))
                .thenReturn(URL("https://maps.app.goo.gl/foo"))
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.NEVER)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = ReceivedIntent(
                shareStateContext,
                intent,
            )
            assertTrue(shareState.transition() is DeniedUnshortenPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val shareStateContext = ShareStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
        )
        val shareState = RequestedUnshortenPermission(
            shareStateContext,
            URL("https://maps.app.goo.gl/foo"),
        )
        assertNull(shareState.transition())
    }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedUnshortenPermission(
                shareStateContext,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(shareStateContext, url),
                shareState.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectToGooglePermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedUnshortenPermission(
                shareStateContext,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(shareStateContext, url),
                shareState.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectToGooglePermission,
                Permission.ALWAYS
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedUnshortenPermission(
                shareStateContext,
                url,
            )
            assertTrue(shareState.deny(false) is DeniedUnshortenPermission)
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectToGooglePermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedUnshortenPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedUnshortenPermission(
                shareStateContext,
                url,
            )
            assertTrue(shareState.deny(true) is DeniedUnshortenPermission)
            verify(mockUserPreferencesRepository).setValue(
                connectToGooglePermission,
                Permission.NEVER
            )
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderFails_returnsUnshortenedUrl() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenReturn(null)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = GrantedUnshortenPermission(
                shareStateContext,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertTrue(shareState.transition() is Failed)
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderSucceeds_returnsUnshortenedUrl() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenReturn(URL("https://maps.google.com/foo"))
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = GrantedUnshortenPermission(
                shareStateContext,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                UnshortenedUrl(
                    shareStateContext,
                    URL("https://maps.google.com/foo"),
                    Permission.ALWAYS,
                ),
                shareState.transition(),
            )
        }

    @Test
    fun deniedUnshortenPermission_returnsFailed() = runTest {
        val shareState = DeniedUnshortenPermission()
        assertTrue(shareState.transition() is Failed)
    }

    @Test
    fun unshortenedUrl_parsingUrlFails_returnsFailed() = runTest {
        val url = URL("https://maps.google.com/foo")
        val mockGoogleMapsUrlConverter =
            Mockito.mock(GoogleMapsUrlConverter::class.java)
        Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
            .thenReturn(null)
        val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
        Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        Mockito.`when`(mockNetworkTools.getText(any<URL>()))
            .thenThrow(NotImplementedError::class.java)
        val shareStateContext = ShareStateContext(
            mockGoogleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
        )
        val shareState = UnshortenedUrl(
            shareStateContext,
            url,
            Permission.ALWAYS,
        )
        assertTrue(shareState.transition() is Failed)
    }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsAlways_returnsGrantedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(geoUriBuilderFromUrl)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val shareStateContext = ShareStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = UnshortenedUrl(
                shareStateContext,
                url,
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    shareStateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                shareState.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsAsk_returnsRequestedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(geoUriBuilderFromUrl)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val shareStateContext = ShareStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = UnshortenedUrl(
                shareStateContext,
                url,
                Permission.ASK,
            )
            assertEquals(
                RequestedParseHtmlPermission(
                    shareStateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                shareState.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsNever_returnsDeniedParseHtmlPermission() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(geoUriBuilderFromUrl)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val shareStateContext = ShareStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState =
                UnshortenedUrl(shareStateContext, url, Permission.NEVER)
            assertTrue(shareState.transition() is DeniedParseHtmlPermission)
        }

    @Test
    fun unshortenUrl_parsingUrlReturnsNonZeroCoordinates_returnsSucceedWithGeoUriFromUrl() =
        runTest {}

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val shareStateContext = ShareStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
        )
        val shareState = RequestedParseHtmlPermission(
            shareStateContext,
            URL("https://maps.app.goo.gl/foo"),
            "geo:1,2?q=fromUrl",
        )
        assertNull(shareState.transition())
    }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriFromUrl = geoUriBuilderFromUrl.toString()
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    shareStateContext,
                    url,
                    geoUriFromUrl,
                ),
                shareState.grant(false),
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectToGooglePermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_grantWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriFromUrl = geoUriBuilderFromUrl.toString()
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    shareStateContext,
                    url,
                    geoUriFromUrl,
                ),
                shareState.grant(true),
            )
            verify(mockUserPreferencesRepository).setValue(
                connectToGooglePermission,
                Permission.ALWAYS
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskFalse_doesNotSavePreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriFromUrl = geoUriBuilderFromUrl.toString()
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                DeniedParseHtmlPermission(geoUriFromUrl),
                shareState.deny(false)
            )
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(connectToGooglePermission),
                any<Permission>(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_denyWithDoNotAskTrue_savesPreferenceAndReturnsGrantedUnshortenPermission() =
        runTest {
            val url = URL("https://maps.app.goo.gl/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriFromUrl = geoUriBuilderFromUrl.toString()
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.setValue(
                    eq(connectToGooglePermission),
                    any<Permission>(),
                )
            ).thenReturn(Unit)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
            )
            val shareState = RequestedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                DeniedParseHtmlPermission(geoUriFromUrl),
                shareState.deny(true)
            )
            verify(mockUserPreferencesRepository).setValue(
                connectToGooglePermission,
                Permission.NEVER
            )
        }

    @Test
    fun grantedParseHtmlPermission_downloadingHtmlFails_returnsFailed() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(null)
            val shareStateContext = ShareStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = GrantedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertTrue(shareState.transition() is Failed)
        }

    @Test
    fun grantedParseHtmlPermission_parsingHtmlSucceeds_returnsSucceededWithGeoUriFromHtml() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val geoUriBuilderFromHtml = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromHtml.coords = GeoUriCoords("1", "2")
            geoUriBuilderFromHtml.params =
                GeoUriParams(q = "fromHtml", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(geoUriBuilderFromHtml)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val shareStateContext = ShareStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = GrantedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                Succeeded(geoUriBuilderFromHtml.toString()),
                shareState.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parsingHtmlFails_returnsSucceededWithGeoUriFromHtml() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(null)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val shareStateContext = ShareStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
            )
            val shareState = GrantedParseHtmlPermission(
                shareStateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                Succeeded(geoUriBuilderFromUrl.toString()),
                shareState.transition(),
            )
        }

    @Test
    fun deniedParseHtmlPermission_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val shareState = DeniedParseHtmlPermission(
                geoUriBuilderFromUrl.toString()
            )
            assertEquals(
                Succeeded(geoUriBuilderFromUrl.toString()),
                shareState.transition(),
            )
        }

    @Test
    fun succeeded_returnsNull() = runTest {
        val shareState = Succeeded("geo:1,2")
        assertNull(shareState.transition())
    }

    @Test
    fun failed_returnsNull() = runTest {
        val shareState = Failed("Fake message")
        assertNull(shareState.transition())
    }

    @Test
    fun noop_returnsNull() = runTest {
        val shareState = Noop()
        assertNull(shareState.transition())
    }
}
