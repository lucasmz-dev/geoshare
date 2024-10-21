package page.ooooo.geoshare

import android.util.Log

interface ILog {
    fun e(tag: String?, msg: String): Int
    fun i(tag: String?, msg: String): Int
    fun w(tag: String?, msg: String): Int
}

class DefaultLog : ILog {
    override fun e(tag: String?, msg: String) = Log.e(tag, msg)
    override fun i(tag: String?, msg: String) = Log.i(tag, msg)
    override fun w(tag: String?, msg: String) = Log.w(tag, msg)
}

class FakeLog : ILog {
    override fun e(tag: String?, msg: String): Int {
        print(msg)
        return 1
    }

    override fun i(tag: String?, msg: String): Int {
        print(msg)
        return 1
    }

    override fun w(tag: String?, msg: String): Int {
        print(msg)
        return 1
    }
}
