package io.lunarlogic.aircasting.screens.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.ShareHelper
import io.lunarlogic.aircasting.models.Session

class ShareSessionBottomSheet(
    private val mListener: ShareSessionBottomSheet.Listener,
    val session: Session
): BottomSheetDialogFragment() {
    interface Listener{
        fun onShareLinkPressed()
        fun onShareFilePressed()
        fun onCancelPressed()
    }

    var emailInput: EditText? = null
    var radioGroup: RadioGroup? = null
    var chosenSensor: String = "P1"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.share_session_bottom_sheet, container, false)

        emailInput = view?.findViewById(R.id.email_input)
        radioGroup = view?.findViewById(R.id.stream_choose_radio_group)

        // TODO: handling clicks on RadioButtons in radioGroup
        radioGroup?.setOnCheckedChangeListener(object: RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when(checkedId){
                    R.id.radio1 -> chosenSensor = "PM1"  // TODO: check in old app the sensor names!!
                    R.id.radio2 -> chosenSensor = "PM2.5"
                    R.id.radio3 -> chosenSensor = "RH"
                    R.id.radio4 -> chosenSensor = "F"
                }
            }
        })

        val shareLinkButton = view?.findViewById<Button>(R.id.share_link_button)
        shareLinkButton?.setOnClickListener {
            mListener.onShareLinkPressed()
        }

        val shareFileButton = view?.findViewById<Button>(R.id.share_file_button)
        shareFileButton?.setOnClickListener {
            mListener.onShareFilePressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        return view
    }

    fun shareFilePressed(): String{
        return emailInput?.text.toString().trim()
    }

    fun shareLinkPressed(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ShareHelper.shareLink(activity, session, chosenSensor))
            putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.share_title))
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share link")
        context?.startActivity(chooser)
    }

}
