package pl.llp.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.AnimatedLoader
import pl.llp.aircasting.screens.common.BaseViewMvc

class ConnectingAirBeamViewMvcImpl : BaseViewMvc, ConnectingAirBeamViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_connecting_airbeam, parent, false)

        startLoader()
    }

    private fun startLoader() {
        val loader = rootView?.findViewById<ImageView>(R.id.loader)
        AnimatedLoader(loader).start()
    }
}
