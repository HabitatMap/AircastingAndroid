package io.lunarlogic.aircasting.screens.sync.refreshing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RefreshingSessionsFragment(): Fragment() {
    private var controller: RefreshingSessionsController? = null
    private var view: RefreshingSessionsViewMvcImpl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = RefreshingSessionsViewMvcImpl(layoutInflater, null)
        controller = RefreshingSessionsController()

        return view?.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view = null
        controller = null
    }
}
