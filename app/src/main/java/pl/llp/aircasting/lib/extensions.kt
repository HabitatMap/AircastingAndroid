package pl.llp.aircasting.lib

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.prominent_app_bar.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}

fun Context.areLocationServicesOn(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun styleGoogleMap(map: GoogleMap, context: Context) {
    map.setMapStyle(
        MapStyleOptions.loadRawResourceStyle(
            context, R.raw.map_style
        )
    )
}

fun labelFormat(value: Float?): String {
    return "%d".format(value?.toInt())
}

fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

fun adjustMenuVisibility(
    activity: Activity,
    isFollowingTab: Boolean,
    followingSessionsNumber: Int = 0
) {
    val visibility =
        if (isFollowingTab && followingSessionsNumber >= 2) View.VISIBLE else View.INVISIBLE
    activity.topAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility =
        visibility
}

fun isValidEmail(target: String): Boolean {
    return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches())
}

fun isWorkEverScheduledBefore(context: Context, tag: String): Boolean {
    val instance = WorkManager.getInstance(context)
    val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosForUniqueWork(tag)
    var workScheduled = false
    statuses.get()?.let {
        for (workStatus in it) {
            workScheduled = (
                    workStatus.state == WorkInfo.State.ENQUEUED
                            || workStatus.state == WorkInfo.State.RUNNING
                            || workStatus.state == WorkInfo.State.BLOCKED
                            || workStatus.state.isFinished
                    )
        }
    }
    return workScheduled
}
