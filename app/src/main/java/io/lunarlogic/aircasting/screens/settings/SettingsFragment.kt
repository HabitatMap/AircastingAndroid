package io.lunarlogic.aircasting.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AppBar

class SettingsFragment : Fragment() {

    private var controller : SettingsController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = SettingsViewMvcImpl(inflater, container, childFragmentManager)
        controller = SettingsController(context, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }
}