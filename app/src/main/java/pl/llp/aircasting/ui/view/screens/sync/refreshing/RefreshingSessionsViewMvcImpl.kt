package pl.llp.aircasting.ui.view.screens.sync.refreshing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseViewMvc
import pl.llp.aircasting.util.extensions.startAnimation

class RefreshingSessionsViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(),
    RefreshingSessionsViewMvc {
    init {
        this.rootView = inflater.inflate(R.layout.fragment_refreshing_sessions, parent, false)
        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        loader?.startAnimation()
    }
}
