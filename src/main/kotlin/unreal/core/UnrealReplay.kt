package unreal.core


@ChunkSerializable data class UnrealReplay(val magic: Int, val meta: Meta) {
    @ChunkSerializable data class Meta(val fileVersion: Int,
                                       val lengthInMs: Int,
                                       val networkVersion: Int,
                                       val changeList: Int,
                                       val friendlyName: String,
                                       val isLive: Boolean,
                                       val versionBased: VersionBasedHandling) {
        class VersionBasedHandling: ChunkHandler {
            var timestamp: Long? = null
            var isCompressed: Boolean? = null
            var isEncrypted: Boolean? = null
            var encryptionKey: ByteArray? = null
            override fun handle(values: List<Any>, chunk: ByteChunk) {
                val fileVersion = values[0] as Int
                if (fileVersion >= ReplayVersionHistory.HISTORY_RECORDED_TIMESTAMP) {
                    timestamp = chunk.long
                }
                if (fileVersion >= ReplayVersionHistory.HISTORY_COMPRESSION) {
                    isCompressed = chunk.boolean
                }
                if (fileVersion >= ReplayVersionHistory.HISTORY_ENCRYPTION) {
                    isEncrypted = chunk.boolean
                    if (isEncrypted!!) {
                        encryptionKey = chunk.buffer.read(chunk.int)
                    }
                }
            }
            override fun toString(): String {
                return "VersionBasedHandling(timestamp=$timestamp, isCompressed=$isCompressed, isEncrypted=$isEncrypted, encryptionKey=${encryptionKey?.contentToString()})"
            }
        }
    }
}