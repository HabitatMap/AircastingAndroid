package pl.llp.aircasting.ui.view.screens.login

import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.extensions.inVisible
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation
import pl.llp.aircasting.util.extensions.visible

interface LogOutInBackgroundInfoDisplayer {
    val infoView: TextView?
    val button: Button?
    fun onLogOutInProgress() {
        infoView?.apply {
            visible()
            compoundDrawables[0]?.startAnimation()
        }
        button?.isEnabled = false
    }

    fun onLogOutFinished() {
        infoView?.apply {
            inVisible()
            compoundDrawables[0]?.stopAnimation()
        }
        button?.isEnabled = true
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(logout: LogoutEvent) = Handler(Looper.getMainLooper()).post {
        if (logout.inProgress)
            onLogOutInProgress()
        else
            onLogOutFinished()
    }
}