package pl.llp.aircasting.ui.view.screens.session_view.graph

import pl.llp.aircasting.util.DateConverter
import java.util.*

object GraphDateStringFactory {
    fun get(date: Date, isExternal: Boolean = false): String {
        return DateConverter.get()
            ?.toTimeStringForDisplay(date, TimeZone.getDefault()) ?: ""
    }
}
