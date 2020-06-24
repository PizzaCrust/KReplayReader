package unreal.core

import java.nio.ByteBuffer

data class BranchData internal constructor(val raw: String, val major: Int, val minor: Int) {
    companion object {
        internal fun fromRawBranch(raw: String): BranchData {
            val releaseVersion = raw.split("-")[1]
            val versioning = releaseVersion.split("\\.".toRegex())
            return BranchData(raw, versioning[0].toInt(), versioning[1].toInt())
        }
    }
}

class HeaderChunk internal constructor(): ByteSchema() {
    val networkMagic: Int by bytes(int32)
    val networkVersion: Int by staticBytes({ networkMagic == 0x2CF5A13D }, int32)
    val networkChecksum: Int by staticBytes({ networkVersion >= NetworkVersionHistory.HISTORY_EXTRA_VERSION }, int32)
    val engineNetworkVersion: Int by bytes(int32)
    val gameNetworkProtocolVersion: Int by bytes(int32)
    val id: String? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_HEADER_GUID }, guid)
    val major: Short? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_SAVE_FULL_ENGINE_VERSION }, int16)
    val minor: Short? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_SAVE_FULL_ENGINE_VERSION }, int16)
    val patch: Short? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_SAVE_FULL_ENGINE_VERSION }, int16)
    private val changelist1: Int? by bytes({ networkVersion >= NetworkVersionHistory
            .HISTORY_SAVE_FULL_ENGINE_VERSION }, int32)
    val branch: BranchData? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_SAVE_FULL_ENGINE_VERSION }) {
        BranchData.fromRawBranch(string)
    }
    private val changelist2: Int? by bytes({ changelist1 == null }, int32)
    val changelist: Int?
        get() = changelist1 ?: changelist2
    val levelNamesAndTimesSize: Int by bytes(int32)
    val skippedLevelData: Unit by bytes {
        slice(levelNamesAndTimesSize)
        Unit
    }
    val flags: Int? by bytes({ networkVersion >= NetworkVersionHistory.HISTORY_HEADER_FLAGS }, int32)
}

val UReplay.Chunk.asHeader: HeaderChunk
    get() = HeaderChunk().apply {
        assert(this@asHeader.type == ReplayChunkType.HEADER)
        this.read(this@asHeader.data, true)
    }

class EventChunk internal constructor(uReplay: UReplay): ByteSchema() {
    val id: String by bytes(string)
    val group: String by bytes(string)
    val metadata: String by bytes(string)
    val startTime: Int by bytes(int32)
    val endTime: Int by bytes(int32)
    val sizeInBytes: Int by bytes(int32)
    val data: ByteBuffer by bytes {
        val encryptedBuffer = slice(sizeInBytes)
        if (uReplay.meta.isEncrypted == true) {
            encryptedBuffer.decrypt(uReplay.meta.encryptionKey!!.arrayLimit(), sizeInBytes)
        } else {
            encryptedBuffer
        }
    }
}

val UReplay.Chunk.asEvent: EventChunk
    get() = EventChunk(uReplay).apply {
        assert(this@asEvent.type == ReplayChunkType.EVENT)
        this.read(this@asEvent.data, true)
    }