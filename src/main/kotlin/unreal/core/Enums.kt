package unreal.core

/*
object ReplayVersionHistory {
    const val HISTORY_INITAL = 0
    const val HISTORY_FIXEDSIZE_FRIENDLY_NAME = 1
    const val HISTORY_COMPRESSION = 2
    const val HISTORY_RECORDED_TIMESTAMP = 3
    const val HISTORY_STREAM_CHUNK_TIMES =  4
    const val HISTORY_FRIENDLY_NAME_ENCODING = 5
    const val HISTORY_ENCRYPTION = 6
}
 */

enum class ReplayVersionHistory {
    HISTORY_INITIAL,
    HISTORY_FIXEDSIZE_FRIENDLY_NAME,
    HISTORY_COMPRESSION,
    HISTORY_RECORDED_TIMESTAMP,
    HISTORY_STREAM_CHUNK_TIMES,
    HISTORY_FRIENDLY_NAME_ENCODING,
    HISTORY_ENCRYPTION;

    operator fun compareTo(i: Int): Int = this.ordinal.compareTo(i)
}

operator fun Int.compareTo(versionHistory: ReplayVersionHistory) = compareTo(versionHistory.ordinal)