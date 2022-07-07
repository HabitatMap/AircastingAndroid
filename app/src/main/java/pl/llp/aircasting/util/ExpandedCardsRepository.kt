package pl.llp.aircasting.util

class ExpandedCardsRepository private constructor(
    private val settings: Settings
) {
    private val uuids: MutableSet<String> = settings.getExpandedSessionsUUIDs()

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
        uuids.add(uuid)
        settings.saveExpandedSessionsUUIDs(uuids)
    }

    fun remove(uuid: String) {
        uuids.remove(uuid)
        settings.saveExpandedSessionsUUIDs(uuids)
    }

    fun clear() {
        uuids.clear()
    }

    fun contains(uuid: String) = uuids.contains(uuid)
}