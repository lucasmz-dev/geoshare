# Geo Share

An Android app to turn Google Maps URLs into geo: URLs.

## Usage

1. Go to Google Maps or a web browser and share a link with Geo Share:

    ![Screenshot of a Google Maps link being shared with Geo
    Share](./app/src/main/res/drawable-mdpi/share_to_light.png)

2. Geo Share will turn the link into a geo: URL and open it with one of your
   installed apps:

    ![Screenshot of Geo Share sharing a geo:
    link](./app/src/main/res/drawable-mdpi/share_from_light.png)

Geo Share supports many Google Maps URL formats. Still, if you find a URL that
doesn't work, please report an [issue on
GitHub](https://github.com/jakubvalenta/geoshare/issues).

## Installation

### From APK

1. Download the APK from [GitHub
   releases](https://github.com/jakubvalenta/geoshare/releases/download/v1.0.0/page.ooooo.geoshare.apk).

2. Verify the APK signature:

    ```shell
    $ apksigner verify --print-certs page.ooooo.geoshare.apk
    Signer #1 certificate DN: CN=Jakub Valenta
    Signer #1 certificate SHA-256 digest: 7380367daf7c96601b3b15b69bf9a2d051a8b04793009a93ca6873bfcc46b378
    Signer #1 certificate SHA-1 digest: b9666537efd3d2fbaf6dc9f73613252dda87bcb2
    Signer #1 certificate MD5 digest: 3dc401479f9f6788751469d6956046c8
    ```

3. Install the APK on your phone using adb:

    ```shell
    $ adb -d install page.ooooo.geoshare.apk
    ```

## Development

Open this repo in Android Studio to run, build and test the app.

### Running release variant

1. Connect your phone via USB.

2. Generate signed APK.

3. Go to `./app/release` and install the APK on your phone using adb:

    ```shell
    $ adb -d install app-release.apk
    ```

### Manually testing various Google Maps URLs

1. Run the app in emulator.

2. Send an Intent to the app using adb:

    ```shell
    $ adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
          -e android.intent.extra.TEXT 'https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu\&g_ep=678910"' \
          page.ooooo.geoshare
    ```

    ```shell
    $ adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
          -e android.intent.extra.TEXT 'https://maps.app.goo.gl/eukZjpeYrrvX3tDw6?g_st=ac' \
          page.ooooo.geoshare
    ```

## Contributing

__Feel free to remix this project__ under the terms of the GNU General Public
License version 3 or later. See [COPYING](./COPYING) and [NOTICE](./NOTICE).
