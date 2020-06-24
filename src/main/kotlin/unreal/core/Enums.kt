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


enum class EngineNetworkVersionHistory {
    HISTORY_INITIAL,
    HISTORY_REPLAY_BACKWARDS_COMPAT,
    HISTORY_MAX_ACTOR_CHANNELS_CUSTOMIZATION,
    HISTORY_REPCMD_CHECKSUM_REMOVE_PRINTF,
    HISTORY_NEW_ACTOR_OVERRIDE_LEVEL,
    HISTORY_CHANNEL_NAMES,
    HISTORY_CHANNEL_CLOSE_REASON,
    HISTORY_ACKS_INCLUDED_IN_HEADER,
    HISTORY_NETEXPORT_SERIALIZATION,
    HISTORY_NETEXPORT_SERIALIZE_FIX,
    HISTORY_FAST_ARRAY_DELTA_STRUCT,
    HISTORY_FIX_ENUM_SERIALIZATION,
    HISTORY_OPTIONALLY_QUANTIZE_SPAWN_INFO,
    HISTORY_JITTER_IN_HEADER;

    val value = ordinal + 1

    operator fun compareTo(i: Int): Int = this.value.compareTo(i)
}

operator fun Int.compareTo(envs: EngineNetworkVersionHistory) = compareTo(envs.value)

enum class ReplayEventType(val value: String) {
    PLAYER_ELIMINATION("playerElim"),
    MATCH_STATS("AthenaMatchStats"),
    TEAM_STATS("AthenaMatchTeamStats"),
    ENCRYPTION_KEY("PlayerStateEncryptionKey"),
    CHARACTER_SAMPLE("CharacterSampleMeta"),
    ZONE_UPDATE("ZoneUpdate"),
    BATTLE_BUS("BattleBusFlight"),
    UNKNOWN("Unknown");

    companion object {
        fun from(str: String) = values().firstOrNull { it.value == str } ?: UNKNOWN
    }
}
