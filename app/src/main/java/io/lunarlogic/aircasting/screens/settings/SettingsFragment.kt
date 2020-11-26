package io.lunarlogic.aircasting.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.lunarlogic.aircasting.R

class SettingsFragment : Fragment() {

    private var controller : SettingsController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Todo: here should be added code analogic to Dashboard fragment I guess, childFragmentManager <??>
        val view = SettingsViewMvcImpl(inflater, container, childFragmentManager)
        controller = SettingsController(view)

        controller?.onCreate()

        return view.rootView
    }
}