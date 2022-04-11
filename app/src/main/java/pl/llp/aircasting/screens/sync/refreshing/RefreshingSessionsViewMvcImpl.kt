package pl.llp.aircasting.screens.sync.refreshing

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.screens.common.BaseViewMvc

class RefreshingSessionsViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(),
    RefreshingSessionsViewMvc {
    init {
        this.rootView = inflater.inflate(R.layout.fragment_refreshing_sessions, parent, false)
        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }
}
