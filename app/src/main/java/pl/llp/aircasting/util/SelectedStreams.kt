package pl.llp.aircasting.util

import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.model.SensorName
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.util.events.StreamSelectedEvent
import java.util.concurrent.ConcurrentHashMap

object SelectedStreams {
    private val selectedStreams: MutableMap<String, SensorName> = ConcurrentHashMap()

    fun save(presenter: SessionPresenter?) {
        val uuid = presenter?.session?.uuid ?: return
        val detailedType = presenter.selectedStream?.detailedType ?: return
        val sensorName = SensorName.fromString(detailedType) ?: return

        selectedStreams[uuid] = sensorName
        EventBus.getDefault().postSticky(StreamSelectedEvent(presenter.session))
    }

    fun get(uuid: String?): String? {
        if (uuid == null) return null

        return selectedStreams[uuid]?.detailedType
    }
}