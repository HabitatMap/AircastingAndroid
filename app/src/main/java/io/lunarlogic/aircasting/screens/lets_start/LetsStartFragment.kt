package io.lunarlogic.aircasting.screens.lets_start

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class LetsStartFragment : Fragment() {
    private var controller: LetsStartController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LetsStartViewMvcImpl(layoutInflater, null, childFragmentManager)
        controller = LetsStartController(activity, view)
        controller?.onCreate()

        return view.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}
