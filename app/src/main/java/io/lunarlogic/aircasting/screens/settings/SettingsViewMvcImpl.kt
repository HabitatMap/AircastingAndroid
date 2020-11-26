package io.lunarlogic.aircasting.screens.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SettingsViewMvcImpl : BaseObservableViewMvc<SettingsViewMvc.Listener>, SettingsViewMvc  {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?,
        fragmentManager: FragmentManager?  //todo: is this fragment meneger needed ??
    ) : super(){
        this.rootView = inflater.inflate(R.layout.fragment_settings, parent, false)

        val myAccountButton = rootView?.findViewById<Button>(R.id.myAccount_Button)
        myAccountButton?.setOnClickListener {
            onMyaccountClicked()
        }

    }

    private fun onMyaccountClicked() {
        //Todo: going to my account activity with signout button

    }


}