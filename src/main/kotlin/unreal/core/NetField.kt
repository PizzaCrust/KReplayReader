package unreal.core

class ExportData internal constructor(uReplay: UReplay): ByteSchema() {
    private val numLayoutCmdExports by bytes(int32)
    val exportTickets: List<ExportTicket> by bytes {
        val list = mutableListOf<ExportTicket>()
        for (i in 0 until numLayoutCmdExports) {
            list.add(ExportTicket(uReplay).apply {
                read(this@bytes)
            })
        }
        list
    }
    val exportGuidGroup: ExportGuidGroup by bytes {
        ExportGuidGroup().apply {
            read(this@bytes)
        }
    }
}

class ExportTicket internal constructor(uReplay: UReplay): ByteSchema() {

    val pathNameIndex by bytes(intPacked)
    val isExported
        get() = pathNameIndex == 1
    val pathName: String? by bytes({ isExported }, string)
    val numExports: Int? by bytes({ isExported }, intPacked)
    val export: Export by bytes {
        Export(uReplay).apply {
            read(this@bytes)
        }
    }

}

class Export internal constructor(uReplay: UReplay): ByteSchema() {
    val isExported by bytes(boolean)
    private val exportedRequirement by staticBytes({isExported}) {Unit}
    val handle by bytes({isExported}, intPacked)
    val compatibleChecksum by bytes({isExported}, int32)
    private val name1 by bytes({ uReplay.header.engineNetworkVersion < EngineNetworkVersionHistory.HISTORY_NETEXPORT_SERIALIZATION}, string)
    val type by bytes({name1 != null}, string)
    private val name2 by bytes({ name1 == null && uReplay.header.engineNetworkVersion < EngineNetworkVersionHistory.HISTORY_NETEXPORT_SERIALIZE_FIX}, string)
    private val name3 by bytes({ name1 == null && name2 == null}, string)
    val name: String
        get() = (name3 ?: name2 ?: name1) ?: throw UnsupportedOperationException("No value")
}

class ExportGuid internal constructor(): ByteSchema() {
    val size by bytes(int32)
    val data by bytes { slice(size) }
}

class ExportGuidGroup internal constructor(): ByteSchema() {
    val numGuids by bytes(intPacked)
    val guids: List<ExportGuid> by bytes {
        val exports = mutableListOf<ExportGuid>()
        for (i in 0 until numGuids) {
            exports.add(ExportGuid().apply {
                read(this@bytes)
            })
        }
        exports
    }
}