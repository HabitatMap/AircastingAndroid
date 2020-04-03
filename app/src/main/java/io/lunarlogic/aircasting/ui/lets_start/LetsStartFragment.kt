package io.lunarlogic.aircasting.ui.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.lunarlogic.aircasting.R

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
        val textView: TextView = root.findViewById(R.id.text_lets_start)
        letsStartViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}