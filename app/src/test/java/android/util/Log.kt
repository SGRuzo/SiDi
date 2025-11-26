package android.util

/**
 * Mock implementation of android.util.Log for unit testing.
 * This allows tests to run without requiring the Android framework.
 */
object Log {
    @JvmStatic
    fun d(tag: String?, msg: String): Int {
        println("DEBUG: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun e(tag: String?, msg: String): Int {
        println("ERROR: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun i(tag: String?, msg: String): Int {
        println("INFO: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun v(tag: String?, msg: String): Int {
        println("VERBOSE: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun w(tag: String?, msg: String): Int {
        println("WARN: $tag: $msg")
        return 0
    }
}
