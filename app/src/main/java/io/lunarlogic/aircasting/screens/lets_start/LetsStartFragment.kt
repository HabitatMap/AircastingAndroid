package io.lunarlogic.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity

class LetsStartFragment : Fragment() {

    private lateinit var letsStartViewModel: LetsStartViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        letsStartViewModel =
            ViewModelProviders.of(this).get(LetsStartViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lets_start, container, false)
        val button: Button = root.findViewById(R.id.start_recording_button)
        button?.setOnClickListener {
            NewSessionActivity.start(context)
        }

        return root
    }
}