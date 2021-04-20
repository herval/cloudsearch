package cloudsearch

import cloudsearch.util.pid
import junit.framework.TestCase

/**
 * Created by herval on 12/3/17.
 */
class PidTest : TestCase() {

    fun testPid() {
        assertNotNull(pid())
    }

}