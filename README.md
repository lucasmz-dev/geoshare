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
   apksigner verify --print-certs page.ooooo.geoshare.apk
   ```

   Expected output:

   ```
   Signer #1 certificate DN: CN=Jakub Valenta, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=DE
   Signer #1 certificate SHA-256 digest: 1b27b17a9df05321a93a47df31ed0d6645ebe55d0e89908157d71c1032d17c10
   Signer #1 certificate SHA-1 digest: f847c6935fa376a568a56ca458896b9236e22b6c
   Signer #1 certificate MD5 digest: 6bcaa6bd5288a6443754b85bf6700374
   ```

3. Install the APK on your phone using adb:

   ```shell
   adb -d install page.ooooo.geoshare.apk
   ```

## Development

Open this repo in Android Studio to run, build and test the app.

### Generating a signed release APK

```shell
make sign keystore_path=/path/to/your/keystore.jks
```

### Installing the release APK on your phone

```shell
make install
```

### Testing various Google Maps URLs

```shell
adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
    -e android.intent.extra.TEXT 'https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu\&g_ep=678910"' \
    page.ooooo.geoshare
```

```shell
adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
    -e android.intent.extra.TEXT 'https://maps.app.goo.gl/eukZjpeYrrvX3tDw6?g_st=ac' \
    page.ooooo.geoshare
```

## Contributing

__Feel free to remix this project__ under the terms of the GNU General Public
License version 3 or later. See [COPYING](./COPYING) and [NOTICE](./NOTICE).
