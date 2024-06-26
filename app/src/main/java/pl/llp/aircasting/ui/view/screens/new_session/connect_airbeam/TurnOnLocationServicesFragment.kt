package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TurnOnLocationServicesFragment(
    private val useDetailedExplanation: Boolean,
    private val areMapsDisabled: Boolean
): Fragment() {
    private var controller: TurnOnLocationServicesController? = null
    var listener: TurnOnLocationServicesViewMvc.Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            TurnOnLocationServicesViewMvcImpl(
                layoutInflater,
                null,
                useDetailedExplanation,
                areMapsDisabled
            )
        controller =
            TurnOnLocationServicesController(
                context,
                view
            )

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener?.let { controller?.unregisterListener(it) }
    }
}
