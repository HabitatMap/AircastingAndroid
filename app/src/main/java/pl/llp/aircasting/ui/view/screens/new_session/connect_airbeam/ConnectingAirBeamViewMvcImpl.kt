package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.util.AnimatedLoader
import pl.llp.aircasting.ui.view.common.BaseViewMvc

class ConnectingAirBeamViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(),
    ConnectingAirBeamViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_connecting_airbeam, parent, false)
        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }
}
