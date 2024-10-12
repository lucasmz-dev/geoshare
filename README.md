# Share to Geo

## Testing

```shell
adb -s emulator-5554 shell am start -W -a android.intent.action.VIEW \
    -d 'https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu\&g_ep=678910"' \
    page.ooooo.sharetogeo
```
