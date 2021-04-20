package cloudsearch.util

fun pid(): Int {
    val runtime = java.lang.management.ManagementFactory.getRuntimeMXBean()
    val jvm = runtime.javaClass.getDeclaredField("jvm")
    jvm.isAccessible = true
    val mgmt = jvm.get(runtime) //as sun.management.VMManagement
    val pidMethod = mgmt.javaClass.getDeclaredMethod("getProcessId")
    pidMethod.isAccessible = true

    return pidMethod.invoke(mgmt) as Int
}