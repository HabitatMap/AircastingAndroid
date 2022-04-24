package pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import pl.llp.aircasting.data.model.MeasurementStream

class SessionCardMeasurementsTableContainer(context: Context, inflater: LayoutInflater, rootView: View?, selectable: Boolean, displayValues: Boolean)
    : MeasurementsTableContainer(context, inflater, rootView, selectable, displayValues) {
    override fun shouldShowSelectedMeasurement(stream: MeasurementStream): Boolean {
        return stream == mSessionPresenter?.selectedStream && !mCollapsed
    }
}
