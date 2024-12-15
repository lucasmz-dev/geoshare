package page.ooooo.geoshare

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
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
import page.ooooo.geoshare.lib.ConversionFailed
import page.ooooo.geoshare.lib.ConversionStateContext
import page.ooooo.geoshare.lib.ConversionSucceeded
import page.ooooo.geoshare.lib.DeniedParseHtmlPermission
import page.ooooo.geoshare.lib.DeniedUnshortenPermission
import page.ooooo.geoshare.lib.DismissedSharePermission
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.GeoUriBuilder
import page.ooooo.geoshare.lib.GeoUriCoords
import page.ooooo.geoshare.lib.GeoUriParams
import page.ooooo.geoshare.lib.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.GrantedParseHtmlPermission
import page.ooooo.geoshare.lib.GrantedSharePermission
import page.ooooo.geoshare.lib.GrantedUnshortenPermission
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Initial
import page.ooooo.geoshare.lib.IntentParser
import page.ooooo.geoshare.lib.NetworkTools
import page.ooooo.geoshare.lib.Noop
import page.ooooo.geoshare.lib.ReceivedGeoUri
import page.ooooo.geoshare.lib.ReceivedIntent
import page.ooooo.geoshare.lib.ReceivedUrl
import page.ooooo.geoshare.lib.RequestedParseHtmlPermission
import page.ooooo.geoshare.lib.RequestedSharePermission
import page.ooooo.geoshare.lib.RequestedUnshortenPermission
import page.ooooo.geoshare.lib.UnshortenedUrl
import page.ooooo.geoshare.lib.XiaomiTools
import java.net.URL

class ConversionStateTest {

    private lateinit var fakeActivity: Activity
    private lateinit var fakeLog: ILog
    private lateinit var fakeUriQuote: FakeUriQuote
    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter
    private lateinit var mockIntentParser: IntentParser
    private lateinit var mockNetworkTools: NetworkTools
    private lateinit var mockXiaomiTools: XiaomiTools
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

        fakeActivity = Activity()
        mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
        Mockito.`when`(
            mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                any<Activity>()
            )
        ).thenThrow(NotImplementedError::class.java)
        Mockito.`when`(
            mockXiaomiTools.showPermissionsEditor(
                any<Activity>(),
                any<ManagedActivityResultLauncher<Intent, ActivityResult>>(),
                any<(String) -> Unit>(),
            )
        ).thenThrow(NotImplementedError::class.java)

        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @Test
    fun initial_returnsNull() =
        runTest {
            val state = Initial()
            assertNull(state.transition())
        }

    @Test
    fun receivedIntent_intentContainsGeoUri_returnsSucceededUnchanged() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn("geo:1,2?q=fromIntent")
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedIntent(stateContext, intent)
            assertEquals(
                ConversionSucceeded("geo:1,2?q=fromIntent", unchanged = true),
                state.transition(),
            )
        }

    @Test
    fun receivedIntent_intentDoesNotContainUrl_returnsNoop() = runTest {
        val intent = Intent()
        val mockIntentParser = Mockito.mock(IntentParser::class.java)
        Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
            .thenReturn(null)
        Mockito.`when`(mockIntentParser.getIntentUrl(intent)).thenReturn(null)
        val stateContext = ConversionStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
        )
        val state = ReceivedIntent(stateContext, intent)
        assertTrue(state.transition() is Noop)
    }

    @Test
    fun receivedIntent_intentContainsUrl_returnsReceivedUrlWithPermissionNull() =
        runTest {
            val intent = Intent()
            val mockIntentParser = Mockito.mock(IntentParser::class.java)
            Mockito.`when`(mockIntentParser.getIntentGeoUri(intent))
                .thenReturn(null)
            Mockito.`when`(mockIntentParser.getIntentUrl(intent))
                .thenReturn(URL("https://maps.google.com/foo"))
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedIntent(stateContext, intent)
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://maps.google.com/foo"),
                    null
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrl_returnsUnshortenedUrlAndPassesPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val permission = Permission.NEVER
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.google.com/foo"),
                permission,
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext,
                    URL("https://maps.google.com/foo"),
                    permission,
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            var stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.ASK,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            var stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                Permission.NEVER,
            )
            assertTrue(state.transition() is DeniedUnshortenPermission)
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ALWAYS)
            var stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertEquals(
                GrantedUnshortenPermission(
                    stateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ASK)
            var stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertEquals(
                RequestedUnshortenPermission(
                    stateContext,
                    URL("https://maps.app.goo.gl/foo"),
                ),
                state.transition(),
            )
        }

    @Test
    fun receivedUrl_urlIsShortUrlAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedUrl(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
                null,
            )
            assertTrue(state.transition() is DeniedUnshortenPermission)
        }

    @Test
    fun requestedUnshortenPermission_transition_returnsNull() = runTest {
        val stateContext = ConversionStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
        )
        val state = RequestedUnshortenPermission(
            stateContext,
            URL("https://maps.app.goo.gl/foo"),
        )
        assertNull(state.transition())
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, url),
                state.grant(false),
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                url,
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, url),
                state.grant(true),
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                url,
            )
            assertTrue(state.deny(false) is DeniedUnshortenPermission)
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedUnshortenPermission(
                stateContext,
                url,
            )
            assertTrue(state.deny(true) is DeniedUnshortenPermission)
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertTrue(state.transition() is ConversionFailed)
        }

    @Test
    fun grantedUnshortenPermission_gettingLocationHeaderSucceeds_returnsUnshortenedUrl() =
        runTest {
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenReturn(URL("https://maps.google.com/foo"))
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedUnshortenPermission(
                stateContext,
                URL("https://maps.app.goo.gl/foo"),
            )
            assertEquals(
                UnshortenedUrl(
                    stateContext,
                    URL("https://maps.google.com/foo"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun deniedUnshortenPermission_returnsFailed() = runTest {
        val state = DeniedUnshortenPermission()
        assertTrue(state.transition() is ConversionFailed)
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
        val stateContext = ConversionStateContext(
            mockGoogleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
        )
        val state = UnshortenedUrl(
            stateContext,
            url,
            Permission.ALWAYS,
        )
        assertTrue(state.transition() is ConversionFailed)
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
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(
                stateContext,
                url,
                Permission.ALWAYS,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
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
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(
                stateContext,
                url,
                Permission.ASK,
            )
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
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
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(
                stateContext,
                url,
                Permission.NEVER,
            )
            assertTrue(state.transition() is DeniedParseHtmlPermission)
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedParseHtmlPermission() =
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
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ALWAYS)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(stateContext, url, null)
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedParseHtmlPermission() =
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
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.ASK)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(stateContext, url, null)
            assertEquals(
                RequestedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriBuilderFromUrl.toString(),
                ),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsZeroCoordinatesAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedParseHtmlPermission() =
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
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenReturn(Permission.NEVER)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(stateContext, url, null)
            assertTrue(state.transition() is DeniedParseHtmlPermission)
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLat_returnsSucceedWithGeoUriFromUrl() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("1", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(geoUriBuilderFromUrl)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(stateContext, url, null)
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun unshortenedUrl_parsingUrlReturnsNonZeroLon_returnsSucceedWithGeoUriFromUrl() =
        runTest {
            val url = URL("https://maps.google.com/foo")
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "1")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseUrl(url))
                .thenReturn(geoUriBuilderFromUrl)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            val mockUserPreferencesRepository = Mockito.mock(
                FakeUserPreferencesRepository::class.java
            )
            Mockito.`when`(
                mockUserPreferencesRepository.getValue(
                    connectToGooglePermission
                )
            ).thenThrow(NotImplementedError::class.java)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = UnshortenedUrl(stateContext, url, null)
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun requestedParseHtmlPermission_transition_returnsNull() = runTest {
        val stateContext = ConversionStateContext(
            googleMapsUrlConverter,
            mockIntentParser,
            mockNetworkTools,
            fakeUserPreferencesRepository,
            mockXiaomiTools,
        )
        val state = RequestedParseHtmlPermission(
            stateContext,
            URL("https://maps.app.goo.gl/foo"),
            "geo:1,2?q=fromUrl",
        )
        assertNull(state.transition())
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriFromUrl,
                ),
                state.grant(false),
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                GrantedParseHtmlPermission(
                    stateContext,
                    url,
                    geoUriFromUrl,
                ),
                state.grant(true),
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                DeniedParseHtmlPermission(geoUriFromUrl),
                state.deny(false)
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                mockUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedParseHtmlPermission(
                stateContext,
                url,
                geoUriFromUrl,
            )
            assertEquals(
                DeniedParseHtmlPermission(geoUriFromUrl),
                state.deny(true)
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
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertTrue(state.transition() is ConversionFailed)
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
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromHtml.toString()),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parsingHtmlFailsAndParsingGoogleSearchHtmlFails_returnsSucceededWithGeoUriFromHtml() =
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
            Mockito.`when`(mockGoogleMapsUrlConverter.parseGoogleSearchHtml(html))
                .thenReturn(null)
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun grantedParseHtmlPermission_parsingHtmlFailsAndParsingGoogleSearchHtmlSucceeds_returnsReceivedUrlWithGoogleMapsUrlAndPermissionAlways() =
        runTest {
            val url = URL("https://g.co/kgs/foo")
            val html = "<html></html>"
            val mockGoogleMapsUrlConverter =
                Mockito.mock(GoogleMapsUrlConverter::class.java)
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseHtml(html))
                .thenReturn(null)
            Mockito.`when`(mockGoogleMapsUrlConverter.parseGoogleSearchHtml(html))
                .thenReturn(URL("https://maps.google.com/foo"))
            val mockNetworkTools = Mockito.mock(NetworkTools::class.java)
            Mockito.`when`(mockNetworkTools.requestLocationHeader(any<URL>()))
                .thenThrow(NotImplementedError::class.java)
            Mockito.`when`(mockNetworkTools.getText(url)).thenReturn(html)
            val stateContext = ConversionStateContext(
                mockGoogleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = GrantedParseHtmlPermission(
                stateContext,
                url,
                geoUriBuilderFromUrl.toString(),
            )
            assertEquals(
                ReceivedUrl(
                    stateContext,
                    URL("https://maps.google.com/foo"),
                    Permission.ALWAYS,
                ),
                state.transition(),
            )
        }

    @Test
    fun deniedParseHtmlPermission_returnsSucceededWithGeoUriFromUrl() =
        runTest {
            val geoUriBuilderFromUrl = GeoUriBuilder(fakeUriQuote)
            geoUriBuilderFromUrl.coords = GeoUriCoords("0", "0")
            geoUriBuilderFromUrl.params =
                GeoUriParams(q = "fromUrl", uriQuote = fakeUriQuote)
            val state = DeniedParseHtmlPermission(
                geoUriBuilderFromUrl.toString()
            )
            assertEquals(
                ConversionSucceeded(geoUriBuilderFromUrl.toString()),
                state.transition(),
            )
        }

    @Test
    fun conversionSucceeded_returnsNull() = runTest {
        val state = ConversionSucceeded("geo:1,2")
        assertNull(state.transition())
    }

    @Test
    fun conversionFailed_returnsNull() = runTest {
        val state = ConversionFailed("Fake message")
        assertNull(state.transition())
    }

    @Test
    fun receivedGeoUri_backgroundStartActivityPermissionIsGranted_returnsGrantedSharePermission() =
        runTest {
            val geoUri = "geo:1,1"
            val unchanged = false
            val fakeActivity = Activity()
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    fakeActivity
                )
            ).thenReturn(true)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedGeoUri(
                stateContext,
                fakeActivity,
                geoUri,
                unchanged,
            )
            assertEquals(
                GrantedSharePermission(geoUri, unchanged),
                state.transition(),
            )
        }

    @Test
    fun receivedGeoUri_backgroundStartActivityPermissionIsNotGranted_returnsRequestedSharePermission() =
        runTest {
            val geoUri = "geo:1,1"
            val unchanged = false
            val fakeActivity = Activity()
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    fakeActivity
                )
            ).thenReturn(false)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = ReceivedGeoUri(
                stateContext,
                fakeActivity,
                geoUri,
                unchanged,
            )
            assertEquals(
                RequestedSharePermission(
                    stateContext,
                    fakeActivity,
                    geoUri,
                    unchanged,
                ),
                state.transition(),
            )
        }

    @Test
    fun requestedSharePermission_backgroundStartActivityPermissionIsGranted_returnsGrantedSharePermission() =
        runTest {
            val geoUri = "geo:1,1"
            val unchanged = false
            val fakeActivity = Activity()
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    fakeActivity
                )
            ).thenReturn(true)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedSharePermission(
                stateContext,
                fakeActivity,
                geoUri,
                unchanged,
            )
            assertEquals(
                GrantedSharePermission(geoUri, unchanged),
                state.transition(),
            )
        }

    @Test
    fun requestedSharePermission_backgroundStartActivityPermissionIsNotGranted_returnsNull() =
        runTest {
            val geoUri = "geo:1,1"
            val unchanged = false
            val fakeActivity = Activity()
            val mockXiaomiTools = Mockito.mock(XiaomiTools::class.java)
            Mockito.`when`(
                mockXiaomiTools.isBackgroundStartActivityPermissionGranted(
                    fakeActivity
                )
            ).thenReturn(false)
            val stateContext = ConversionStateContext(
                googleMapsUrlConverter,
                mockIntentParser,
                mockNetworkTools,
                fakeUserPreferencesRepository,
                mockXiaomiTools,
            )
            val state = RequestedSharePermission(
                stateContext,
                fakeActivity,
                geoUri,
                unchanged,
            )
            assertNull(state.transition())
        }

    @Test
    fun grantedSharePermission_returnsNull() =
        runTest {
            val geoUri = "geo:1,1"
            val unchanged = false
            val state = GrantedSharePermission(geoUri, unchanged)
            assertNull(state.transition())
        }

    @Test
    fun dismissedSharePermission_returnsNull() =
        runTest {
            val state = DismissedSharePermission()
            assertNull(state.transition())
        }

    @Test
    fun noop_returnsNull() = runTest {
        val state = Noop()
        assertNull(state.transition())
    }
}
