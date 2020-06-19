@file:Suppress("UNCHECKED_CAST")
package unreal.core

import java.io.File
import java.lang.NullPointerException
import java.nio.ByteBuffer
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal typealias ConditionalBlock = () -> Boolean
internal typealias ByteReadBlock<T> = ByteBuffer.() -> T

abstract class ByteSchema {

    internal data class DelegateBackend<T>(private val conditionalBlock: ConditionalBlock,
                                           private val byteReadBlock: ByteReadBlock<T>) {
        internal var value: T? = null

        fun read(buffer: ByteBuffer) {
            if (!conditionalBlock()) {
                value = null
                return
            }
            value = byteReadBlock(buffer)
        }

    }

    abstract class SchemaDelegate<T> internal constructor(internal val backend: DelegateBackend<T>)

    class NullableSchemaDelegate<T> internal constructor(backend: DelegateBackend<T>) : SchemaDelegate<T>(backend) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = backend.value
    }

    class NonNullableSchemaDelegate<T> internal constructor(backend: DelegateBackend<T>): SchemaDelegate<T>(backend) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = backend.value ?: throw NullPointerException()
    }

    private val definitions: MutableList<DelegateBackend<*>> = mutableListOf()

    fun <T> bytes(conditionalBlock: ConditionalBlock, byteReadBlock: ByteReadBlock<T>): NullableSchemaDelegate<T> = NullableSchemaDelegate(DelegateBackend(conditionalBlock, byteReadBlock)).apply { definitions.add(this.backend) }

    fun <T> staticBytes(conditionalBlock: ConditionalBlock, byteReadBlock: ByteReadBlock<T>): NonNullableSchemaDelegate<T> = NonNullableSchemaDelegate(DelegateBackend(conditionalBlock, byteReadBlock)).apply { definitions.add(this.backend) }

    fun <T> bytes(byteReadBlock: ByteReadBlock<T>): NonNullableSchemaDelegate<T> = staticBytes({ true }, byteReadBlock)

    open fun read(buffer: ByteBuffer) = definitions.forEach { it.read(buffer) }

    override fun toString(): String {
        val properties = mutableListOf<String>()
        this::class.memberProperties.forEach { it ->
            val property = it as KProperty1<ByteSchema, Any?>
            properties.add("${property.name}=${property.get(this)}")
        }
        return "${this::class.simpleName}(${properties.joinToString()})"
    }

}

val int16: ByteReadBlock<Short> = { short }
val int32: ByteReadBlock<Int> = { int }
val int64: ByteReadBlock<Long> = { long }
val boolean: ByteReadBlock<Boolean> = { boolean }
val string: ByteReadBlock<String> = { string }
val guid: ByteReadBlock<String> = { guid() }

class TestSchema: ByteSchema() {
    val fileMagic: Int by bytes(int32)
    val fileVersion: Int by staticBytes({ fileMagic == 0x1CA2E27F }, int32)
    val lengthInMs: Int by bytes(int32)
}

fun main() {
    /*
    println(TestSchema().apply {
        read(ByteBuffer(File("season12.replay")))
    })
     */
    println(UReplay().apply {
        read(ByteBuffer(File("season12.replay")))
    })
}