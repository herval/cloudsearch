package cloudsearch.util

import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*


fun base64(str: String): String {
    return String(Base64.getEncoder().encode(str.toByteArray()))
}

private val messageDigest = MessageDigest.getInstance("MD5")

fun md5(str: String): String {
    messageDigest.reset()
    messageDigest.update(str.toByteArray(Charset.forName("UTF8")))
    val resultByte = messageDigest.digest()
    return String(Hex.encodeHex(resultByte))
}