package pl.llp.aircasting.ui.view.screens.new_session.session_details

open class SessionDetailsController(
    private var mViewMvc: SessionDetailsViewMvc?
) {

    fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    open fun onCreate() {}

    fun onDestroy() {
        mViewMvc = null
    }
}
