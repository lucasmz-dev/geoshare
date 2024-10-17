package page.ooooo.sharetogeo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

open class BaseShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        val data = intent?.data
        if (action == Intent.ACTION_VIEW && data != null) {
            val context = this
            lifecycleScope.launch {
                val geoUri = getGeoUri(data.toString())
                if (geoUri != null) {
                    Log.i(null, "Converted $data to $geoUri")
                    showToast(context, "Opened geo URL")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
                } else {
                    showToast(context, "Failed to create geo URL")
                }
                finish()
            }
        } else {
            finish()
        }
    }

    private fun showToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    open suspend fun getGeoUri(uriString: String): String? {
        throw NotImplementedError()
    }
}
