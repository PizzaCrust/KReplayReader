package unreal.core

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE

fun ByteBuffer.read(amnt: Int) = ByteArray(amnt).apply {
    get(this)
}

/**
 * Represents a chunk of bytes.
 */
class ByteChunk(bytes: ByteArray) {

    val buffer: ByteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    val int: Int
        get() = buffer.int

    val long: Long
        get() = buffer.long

    val boolean: Boolean
        get() = int != 0

    val short: Short
        get() = buffer.short

    val string: String
        get() {
            var length = int
            val isUnicode = length < 0
            if (isUnicode) length = -length
            if (length < 0) throw UnsupportedOperationException("Archive is corrupted")
            val data = (if (isUnicode) buffer.read(length * 2) else buffer.read(length))
            return String(data).trim { it <= ' ' }.replace(("\u0000").toRegex(), "")
        }

    fun guid(size: Int = 16) = buffer.read(size).joinToString { "%02x".format(it) }

    fun decrypt(key: ByteArray,
                size: Int): ByteChunk {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(DECRYPT_MODE, javax.crypto.spec.SecretKeySpec(key, "AES"))
        return ByteChunk(cipher.doFinal(buffer.read(size)))
    }

}