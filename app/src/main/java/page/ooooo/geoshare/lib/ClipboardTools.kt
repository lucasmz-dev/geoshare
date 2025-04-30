package page.ooooo.geoshare.lib

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

class ClipboardTools {
    suspend fun setPlainText(
        clipboard: Clipboard,
        label: String,
        text: String,
    ) {
        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))
    }
}
