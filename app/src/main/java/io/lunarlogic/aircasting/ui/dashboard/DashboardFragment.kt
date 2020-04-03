package io.lunarlogic.aircasting.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.R
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dashboard_record_new_session_button.setOnClickListener {
            activity?.let{
                val selectDeviceIntent = Intent(it, SelectDeviceActivity::class.java)
                it.startActivity(selectDeviceIntent)
            }
        }
    }
}