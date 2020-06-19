package unreal.core

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun ByteBuffer(vararg array: Byte, order: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteBuffer = ByteBuffer.wrap(array).order(order)

fun ByteBuffer(file: File, order: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteBuffer = ByteBuffer(*file.readBytes(), order = order)

val ByteBuffer.boolean: Boolean
    get() = int != 0

fun ByteBuffer.read(amount: Int) = ByteArray(amount).apply {
    get(this)
}

val ByteBuffer.string: String
    get() {
        var length = int
        val isUnicode = length < 0
        if (isUnicode) length = -length
        if (length < 0) throw UnsupportedOperationException("Archive is corrupted")
        val data = (if (isUnicode) read(length * 2) else read(length))
        return String(data).trim { it <= ' ' }.replace(("\u0000").toRegex(), "")
    }

fun ByteBuffer.guid(size: Int = 16) = read(size).joinToString { "%02x".format(it) }

fun ByteBuffer.decrypt(key: ByteArray,
            size: Int): ByteBuffer {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
    return ByteBuffer(*cipher.doFinal(read(size)))
}

fun ByteBuffer.slice(limit: Int): ByteBuffer = this.slice().apply { limit(limit) }