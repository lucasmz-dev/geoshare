# Geo Share

## Testing

```shell
adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
    -e android.intent.extra.TEXT 'https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu\&g_ep=678910"' \
    page.ooooo.geoshare
adb -s emulator-5554 shell am start -W -a android.intent.action.SEND -t text/plain \
    -e android.intent.extra.TEXT 'https://maps.app.goo.gl/eukZjpeYrrvX3tDw6?g_st=ac' \
    page.ooooo.geoshare
```
