package page.ooooo.geoshare

import android.net.Uri
import java.net.URLDecoder
import java.net.URLEncoder

interface UriQuote {
    fun encode(s: String): String
    fun decode(s: String): String
}

class DefaultUriQuote : UriQuote {
    override fun encode(s: String) = Uri.encode(s)
    override fun decode(s: String) = Uri.decode(s)
}

class FakeUriQuote : UriQuote {
    override fun encode(s: String): String =
        URLEncoder.encode(s, "utf-8").replace("+", "%20")

    override fun decode(s: String): String =
        URLDecoder.decode(s, "utf-8")
}
