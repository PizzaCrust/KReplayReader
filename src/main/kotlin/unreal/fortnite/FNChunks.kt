package unreal.fortnite

import unreal.core.*
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer
import kotlin.reflect.full.createInstance

class Elimination internal constructor(uReplay: UReplay): ByteSchema() {

    @IgnoreSchema private val branch: BranchData = uReplay.header.branch ?: throw UnsupportedOperationException("Branch data doesn't exist")

    private val newSkip: Unit? by bytes({
        uReplay.header.engineNetworkVersion >= EngineNetworkVersionHistory.HISTORY_FAST_ARRAY_DELTA_STRUCT
                && branch.major >= 9
    }) {
        slice(85)
        Unit
    }

    private fun ByteBuffer.parsePlayer(): String {
        val indicator = read(1)[0]
        return when (indicator) {
            0x03.toByte() -> "Bot"
            0x10.toByte() -> string
            else -> {
                val size = read(1)[0]
                guid(size.toInt())
            }
        }
    }

    private val newVictimId: String? by bytes({ newSkip != null }) { parsePlayer() }
    private val newKillerId: String? by bytes({ newSkip != null }) { parsePlayer() }

    private val skip41: Unit? by bytes({ branch.major <= 4 && branch.minor < 2 }) {
        slice(12)
        Unit
    }

    private val skip42: Unit? by bytes({ branch.major == 4 && branch.minor <= 2}) {
        slice(40)
        Unit
    }

    private val skip43: Unit? by bytes({ newSkip == null && skip41 == null && skip42 == null}) {
        slice(45)
        Unit
    }

    private val oldVictimId: String? by bytes({ newVictimId == null }, string)
    private val oldKillerId: String? by bytes({ newVictimId == null }, string)

    val victimId: String
        get() = (newVictimId ?: oldVictimId) ?: throw UnsupportedOperationException("No value")
    val killerId: String
        get() = (newKillerId ?: oldKillerId) ?: throw UnsupportedOperationException("No value")
    val gunType by bytes(byte)
    val knocked by bytes(boolean)

}

val EventChunk.type: ReplayEventType
    get() = ReplayEventType.from(this.group)

val EventChunk.asElim: Elimination
    get() = Elimination(uReplay).apply {
        assert(this@asElim.type == ReplayEventType.PLAYER_ELIMINATION)
        read(this@asElim.data, true)
    }

val UReplay.eliminations: List<Elimination>
    get() = this.events.filter { it.type == ReplayEventType.PLAYER_ELIMINATION }.map { it.asElim }

class MatchStats internal constructor(): ByteSchema() {

    private val unknown: Int by bytes(int32)
    val accuracy: Float by bytes(float)
    val assists: Int by bytes(int32)
    val eliminations: Int by bytes(int32)
    val weaponDamage: Int by bytes(int32)
    val otherDamage: Int by bytes(int32)
    val revives: Int by bytes(int32)
    val damageTaken: Int by bytes(int32)
    val damageToStructures: Int by bytes(int32)
    val materialsGathered: Int by bytes(int32)
    val materialsUsed: Int by bytes(int32)
    val totalTravelled: Int by bytes(int32)

}

inline fun <reified T: ByteSchema> ByteBuffer.read(rewind: Boolean = true,
                                                   block: () -> Unit = {}): T {
    block()
    return T::class.createInstance().apply {
        this.read(this@read, rewind)
    }
}

val EventChunk.asMatchStats: MatchStats
    get() = data.read()

val UReplay.matchStats: MatchStats
    get() = events.first { ReplayEventType.fromId(it.id) == ReplayEventType.MATCH_STATS }.asMatchStats

class TeamStats internal constructor(): ByteSchema() {

    private val unknown: Int by bytes(int32)
    val position: Int by bytes(int32)
    val totalPlayers: Int by bytes(int32)

}

val EventChunk.asTeamStats: TeamStats
    get() = data.read()

val UReplay.teamStats: TeamStats
    get() = events.first { ReplayEventType.fromId(it.id) == ReplayEventType.TEAM_STATS }.asTeamStats
