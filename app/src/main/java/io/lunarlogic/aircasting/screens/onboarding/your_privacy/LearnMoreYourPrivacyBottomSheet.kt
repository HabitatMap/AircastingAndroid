package io.lunarlogic.aircasting.screens.onboarding.your_privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import kotlinx.android.synthetic.main.learn_more_onboarding_your_privacy.view.*

class LearnMoreYourPrivacyBottomSheet: BottomSheetDialogFragment() {

    private val TAG = "LearnMorePage4BottomSheet"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.learn_more_onboarding_your_privacy, container, false)

        val textView = view?.findViewById<TextView>(R.id.learn_more_onboarding_your_privacy_description)
        view.learn_more_onboarding_your_privacy_description.text = buildDescription()

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            dismiss()
        }

        return view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    fun buildDescription(): String {
        return getString(R.string.onboarding_bottomsheet_page4_description1) + "\n\n" + getString(R.string.onboarding_bottomsheet_page4_description2)
    }

}
