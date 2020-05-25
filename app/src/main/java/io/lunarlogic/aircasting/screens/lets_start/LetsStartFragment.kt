package io.lunarlogic.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class LetsStartFragment : Fragment() {
    private lateinit var controller: LetsStartController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LetsStartViewMvcImpl(layoutInflater, null)
        controller = LetsStartController(context, view)
        controller.onCreate()

        return view.rootView
    }
}