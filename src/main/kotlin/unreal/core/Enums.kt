package unreal.core

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

enum class ReplayChunkType {
    HEADER,
    REPLAY_DATA,
    CHECKPOINT,
    EVENT,
    UNKNOWN;
}

enum class NetworkVersionHistory {
    HISTORY_REPLAY_INITIAL,
    HISTORY_SAVE_ABS_TIME_MS,
    HISTORY_INCREASE_BUFFER,
    HISTORY_SAVE_ENGINE_VERSION,
    HISTORY_EXTRA_VERSION,
    HISTORY_MULTIPLE_LEVELS,
    HISTORY_MULTIPLE_LEVELS_TIME_CHANGES,
    HISTORY_DELETED_STARTUP_ACTORS,
    HISTORY_HEADER_FLAGS,
    HISTORY_LEVEL_STREAMING_FIXES,
    HISTORY_SAVE_FULL_ENGINE_VERSION,
    HISTORY_HEADER_GUID,
    HISTORY_CHARACTER_MOVEMENT,
    HISTORY_CHARACTER_MOVEMENT_NOINTERP;

    val value = ordinal + 1

    operator fun compareTo(i: Int): Int = this.value.compareTo(i)

}

operator fun Int.compareTo(networkVersionHistory: NetworkVersionHistory) = compareTo(networkVersionHistory.value)
