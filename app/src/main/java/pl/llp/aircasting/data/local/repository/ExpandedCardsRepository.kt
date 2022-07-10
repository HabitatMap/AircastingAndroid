package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.util.Settings

class ExpandedCardsRepository private constructor(
    private val settings: Settings
) {
    companion object {
        private var instance: ExpandedCardsRepository? = null
        fun getInstance(): ExpandedCardsRepository? {
            return instance
        }

        fun setup(settings: Settings) {
            if (instance == null)
                instance = ExpandedCardsRepository(settings)
        }
    }

    fun add(uuid: String) {
        val uuids = settings.getExpandedSessionsUUIDs()
        uuids.add(uuid)
        settings.saveExpandedSessionsUUIDs(uuids)
    }

    fun remove(uuid: String) {
        val uuids = settings.getExpandedSessionsUUIDs()
        uuids.remove(uuid)
        settings.saveExpandedSessionsUUIDs(uuids)
    }

    fun contains(uuid: String): Boolean {
        val uuids = settings.getExpandedSessionsUUIDs()
        return uuids.contains(uuid)
    }
}