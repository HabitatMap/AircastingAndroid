package pl.llp.aircasting.ui.view.screens.search

import androidx.fragment.app.activityViewModels
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.SearchFollowBottomSheetBinding
import pl.llp.aircasting.ui.view.screens.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel

class SearchFixedBottomSheet : BottomSheet() {
    private val viewModel: SearchFollowViewModel by activityViewModels()
    private var binding: SearchFollowBottomSheetBinding? = null
    // TODO: Get data from ViewModel about the selected session
    override fun layoutId(): Int {
        return R.layout.search_follow_bottom_sheet
    }

    override fun setup() {
        super.setup()

        binding = contentView?.let { SearchFollowBottomSheetBinding.bind(it) }
        binding?.viewModel = viewModel
    }
}