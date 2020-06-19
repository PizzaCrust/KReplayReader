package unreal.core

import java.nio.ByteBuffer

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

    override fun read(buffer: ByteBuffer) {
        super.read(buffer)
        meta = Meta(this).apply {
            read(buffer)
        }
    }

}