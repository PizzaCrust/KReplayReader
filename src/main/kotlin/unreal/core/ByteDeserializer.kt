package unreal.core

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Indicates a class can be deserialize from a ByteChunk.
 * Supports the types declared in ByteChunk
 */
@Target(AnnotationTarget.CLASS)
annotation class ChunkSerializable

/**
 * Indicates to the deserializer that the property should be read after an amount of bytes.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Skip(val bytes: Int)

/**
 * Represents the object to represent a GUID inside a chunk serializable class.
 */
data class SerializableGuid(val guid: String)

@ExperimentalStdlibApi
fun ByteChunk.deserialize(clazz: KClass<*>): Any {
    val metadata = clazz.findAnnotation<ChunkSerializable>() ?: throw UnsupportedOperationException("Attempting to deserialize on a unsupported schema")
    val constructor = clazz.constructors.firstOrNull() ?: throw UnsupportedOperationException("No" +
            " constructors???")
    val values = mutableListOf<Any>()
    constructor.parameters.forEach {
        if (it.hasAnnotation<Skip>()) buffer.read(it.findAnnotation<Skip>()!!.bytes)
        when (it.type.classifier) {
            String::class -> values.add(string)
            Int::class -> values.add(int)
            Short::class -> values.add(short)
            Boolean::class -> values.add(intBoolean)
            Long::class -> values.add(long)
            SerializableGuid::class -> values.add(SerializableGuid(guid()))
            else -> {
                if (!it.type.hasAnnotation<ChunkSerializable>() || it.type.classifier !is KClass<*>) {
                    throw UnsupportedOperationException("Property ${it.name} has an unsupported type of ${it.type}! ")
                } else {
                    values.add(deserialize(it.type.classifier as KClass<*>))
                }
            }
        }
    }
    return constructor.call(*values.toTypedArray())
}

@ExperimentalStdlibApi
inline fun <reified T> ByteChunk.deserialize(): T = deserialize(T::class) as T

@ChunkSerializable data class Test(val fileMagic: Int, val fileVersion: Int, val lengthInMs: Int)
@ExperimentalStdlibApi
fun main() {
    println(ByteChunk(File("season12.replay").readBytes()).deserialize<Test>().apply {
        println(this.fileMagic == 0x1CA2E27F)
    })
}