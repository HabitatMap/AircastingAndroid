package pl.llp.aircasting.ui.view.screens.search

import androidx.fragment.app.activityViewModels
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.screens.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.util.DateConverter

class SearchFixedBottomSheet : BottomSheet() {
    private val viewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null

    companion object {
        // I am not sure about this approach, maybe all these things should be in a different place
        private val dateConverter = DateConverter.get()
        private fun formatTime(time: String = ""): String? = DateConverter.fromString(time)
            ?.let { dateConverter?.toTimeStringForDisplay(it) }

        private fun formatDate(date: String = ""): String? = DateConverter.fromString(date)
            ?.let { DateConverter.toDateStringForDisplay(it) }

        private fun formatType(type: String = ""): String {
            val splitByCapitalLetter = type.split(Regex("(?=[A-Z])"))
            return splitByCapitalLetter[1]
        }

        private fun formatSensorName(sensor: String = ""): String {
            val splitByHyphen = sensor.split("-")
            return splitByHyphen[0]
        }
    }

    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }

    override fun setup() {
        super.setup()
        binding = contentView?.let { SearchFollowBottomSheetBinding.bind(it) }
        setObserver()
    }

    private fun setObserver() {
        viewModel.selectedSession.observe(this) {
            binding?.apply {
                title = it.title
                startDate = formatDate(it.startTimeLocal)
                startTime = formatTime(it.startTimeLocal)
                endDate = formatDate(it.endTimeLocal)
                endTime = formatTime(it.endTimeLocal)
                type = formatType(it.type)
                sensorName = formatSensorName(it.streams.sensor.sensorName)
            }
        }
    }
}