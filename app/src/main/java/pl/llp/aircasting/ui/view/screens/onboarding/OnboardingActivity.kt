package pl.llp.aircasting.ui.view.screens.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.common.ViewEdge
import pl.llp.aircasting.ui.view.common.updateInsets

class OnboardingActivity: BaseActivity() {
    private var controller: OnboardingController? = null

    companion object {
        fun start(contextActivity: AppCompatActivity?) {
            contextActivity?.let{
                val intent = Intent(it, OnboardingActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        val view = OnboardingViewMvcImpl(layoutInflater, null)
        controller = OnboardingController(this, view, supportFragmentManager, settings)

        controller?.onCreate()

        setContentView(view.rootView)
    }

    override fun applyEdgeToEdgeInsets(rootView: View) {
        rootView.updateInsets(ViewEdge.LEFT, ViewEdge.RIGHT, ViewEdge.BOTTOM)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        controller?.onBackPressed()
    }
}
