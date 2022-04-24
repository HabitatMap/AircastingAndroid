package pl.llp.aircasting.ui.view.screens.new_session.choose_location

import pl.llp.aircasting.ui.view.screens.common.BaseController

class ChooseLocationController (
    viewMvc: ChooseLocationViewMvcImpl?
) : BaseController<ChooseLocationViewMvcImpl>(viewMvc) {

    fun registerListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()

        mViewMvc?.onDestroy()
    }
}
