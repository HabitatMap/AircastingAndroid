package pl.llp.aircasting.ui.view.screens.sync.unplug_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import pl.llp.aircasting.R

class UnplugAirBeamFragment(
    val onUnplugAirBeamContinueClicked: () -> Unit = {}
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_unplug_airbeam, null, false)
        val button = rootView?.findViewById<Button>(R.id.unplug_airbeam_ready_button)
        button?.setOnClickListener {
            onUnplugAirBeamContinueClicked()
        }
        return rootView
    }
}