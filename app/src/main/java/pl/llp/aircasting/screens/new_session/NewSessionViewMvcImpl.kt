package pl.llp.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseViewMvc

class NewSessionViewMvcImpl(inflater: LayoutInflater, parent: ViewGroup?) : BaseViewMvc(), NewSessionViewMvc {
    init {
        this.rootView = inflater.inflate(R.layout.activity_new_session, parent, false)
    }
}