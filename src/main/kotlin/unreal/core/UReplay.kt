package unreal.core

import java.nio.ByteBuffer

/**
 * Represents an unreal engine replay.
 * This parses common elements within an unreal engine replay. For fortnite based parsing,
 * view FNChunks, where Fortnite specific elements are parsed and can be accessed via
 * extensions.
 */
class UReplay: ByteSchema() {

    val fileMagic: Int by bytes(int32)

    class Meta internal constructor(uReplay: UReplay): ByteSchema() {

        val fileVersion: Int by staticBytes({ uReplay.fileMagic == 0x1CA2E27F }, int32)
        val lengthInMs: Int by bytes(int32)
        val networkVersion: Int by bytes(int32)
        val changelist: Int by bytes(int32)
        val friendlyName: String by bytes(string)
        val isLive: Boolean by bytes(boolean)
        val timestamp: Long? by bytes({ fileVersion >= ReplayVersionHistory.HISTORY_RECORDED_TIMESTAMP }, int64)
        val isCompressed: Boolean? by bytes({ fileVersion >= ReplayVersionHistory.HISTORY_COMPRESSION }, boolean)
        val isEncrypted: Boolean? by bytes({ fileVersion >= ReplayVersionHistory.HISTORY_ENCRYPTION }, boolean)
        val encryptionKeySize: Int? by bytes({ isEncrypted ?: false }, int32)
        val encryptionKey: ByteBuffer? by bytes({ isEncrypted ?: false }) { slice(encryptionKeySize!!) }

    }

    lateinit var meta: Meta

    class Chunk internal constructor(@IgnoreSchema val uReplay: UReplay): ByteSchema() {

        val type: ReplayChunkType by bytes {
            val type = int
            ReplayChunkType.values().firstOrNull { it.ordinal == type } ?: ReplayChunkType.UNKNOWN
        }
        val size: Int by bytes(int32)
        val data: ByteBuffer by bytes { slice(size) }

    }

    lateinit var chunks: List<Chunk>

    val header: HeaderChunk
        get() = chunks.firstOrNull { it.type == ReplayChunkType.HEADER}?.asHeader ?: throw UnsupportedOperationException()

    val events: List<EventChunk>
        get() = chunks.filter { it.type == ReplayChunkType.EVENT }.map { it.asEvent }

    val data: List<DataChunk>
        get() = chunks.filter { it.type == ReplayChunkType.REPLAY_DATA }.map { it.asData }

    override fun read(buffer: ByteBuffer, rewind: Boolean) {
        super.read(buffer, false)
        meta = Meta(this).apply {
            read(buffer)
        }
        val chunks = mutableListOf<Chunk>()
        while (buffer.hasRemaining()) {
            chunks.add(Chunk(this).apply {
                read(buffer)
            })
        }
        this.chunks = chunks
        if (rewind) buffer.rewind()
    }

}