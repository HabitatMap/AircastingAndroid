package pl.llp.aircasting.ui.view.screens.search

import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BottomSheet

class SearchFixedBottomSheet : BottomSheet() {
    // TODO: Get data from ViewModel about the selected session
    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }
}