package unreal.core

import me.fungames.oodle.Oodle
import unreal.fortnite.read
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer

fun UReplay.decompress(compressedChunk: ByteBuffer): ByteBuffer {
    if (this.meta.isCompressed == false) {
        return compressedChunk
    }
    val decompressedSize = compressedChunk.int
    val compressedSize = compressedChunk.int
    val compressedBuffer = compressedChunk.slice(compressedSize)
    return ByteBuffer(*ByteArray(decompressedSize).apply {
        Oodle.decompress(compressedBuffer.arrayLimit(), this)
    })
}

class PlaybackPacket internal constructor(): ByteSchema() {
    val bufferSize by bytes(int32)

    enum class PacketState {
        SUCCESS, END, ERROR
    }

    val state: PacketState
        get() {
            if (bufferSize == 0) {
                return PacketState.END
            } else if (bufferSize > 2048 || bufferSize < 0) {
                return PacketState.ERROR
            }
            return PacketState.SUCCESS
        }

    val data: ByteBuffer by bytes { slice(bufferSize) }
}

class DemoFrame internal constructor(uReplay: UReplay): ByteSchema() {

    @IgnoreSchema private val hasLevelStreamingFixes = ReplayHeaderFlags.HAS_STREAMING_FIXES.hasFlag(uReplay.header.flags ?: 0)
    @IgnoreSchema private val hasGameSpecificFrameData = ReplayHeaderFlags.GAME_SPECIFIC_FRAME_DATA.hasFlag(uReplay.header.flags ?: 0)

    val currentLevelIndex by bytes({ uReplay.header.networkVersion >= NetworkVersionHistory.HISTORY_MULTIPLE_LEVELS }, int32)
    val timeSeconds by bytes(float)
    val exportData by bytes({uReplay.header.networkVersion >= NetworkVersionHistory.HISTORY_LEVEL_STREAMING_FIXES}) {
        ExportData(uReplay).apply {
            read(this@bytes)
        }
    }
    private val levelStreamingFlag by staticBytes({ hasLevelStreamingFixes }) { Unit }
    val numStreamingLevels: Int by bytes(intPacked)
    val levelNames: List<String> by bytes {
        val levelNames = mutableListOf<String>()
        for (i in 0 until numStreamingLevels) {
            levelNames.add(string)
        }
        levelNames
    }
    val externalOffset by bytes(int64)
    private val skipExternalData by bytes {
        while (true) {
            val externalDataNumBits = intPacked
            if (externalDataNumBits == 0) break
            val netGuid = intPacked
            val size = (externalDataNumBits + 7) shr 3
            slice(size)
        }
    }
    private val skipExternalOffset by bytes({ hasGameSpecificFrameData }) {
        val size = long
        if (size > 0) slice(size.toInt())
    }
    private val packets: List<PlaybackPacket> by bytes {
        val packets = mutableListOf<PlaybackPacket>()
        var remaining = true
        while (remaining) {
            val seenLevelIndex = intPacked
            packets.add(read<PlaybackPacket> {  }.apply {
                remaining = when (this.state) {
                    PlaybackPacket.PacketState.SUCCESS -> true
                    else -> false
                }
            })
        }
        packets
    }

}

class DataChunk internal constructor(uReplay: UReplay): ByteSchema() {
    val start: Int? by bytes({ uReplay.meta.fileVersion >= ReplayVersionHistory.HISTORY_STREAM_CHUNK_TIMES }, int32)
    val end: Int? by bytes({ start != null }, int32)
    private val length1: Int? by bytes({ end != null }, int32)
    private val length2: Int? by bytes({ length1 == null }, int32)
    val length: Int
        get() = (length1 ?: length2) ?: throw UnsupportedOperationException("No value")
    private val newMemorySizeInBytes by bytes({ uReplay.meta.fileVersion >= ReplayVersionHistory.HISTORY_ENCRYPTION }, int32)
    val memorySizeInBytes: Int
        get() = (newMemorySizeInBytes ?: length)
    val data: List<DemoFrame> by bytes {
        val decrypted = uReplay.decrypt(this, length)
        val decompressedBuffer = uReplay.decompress(decrypted)
        val demoFrame = mutableListOf<DemoFrame>()
        while (decompressedBuffer.hasRemaining()) {
            demoFrame.add(DemoFrame(uReplay).apply {
                read(this@bytes)
            })
        }
        demoFrame
    }
}

val UReplay.Chunk.asData: DataChunk
    get() {
        assert(this.type == ReplayChunkType.REPLAY_DATA)
        return DataChunk(this.uReplay).apply {
            read(this@asData.data, true)
        }
    }