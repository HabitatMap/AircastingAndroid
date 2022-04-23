package pl.llp.aircasting.lib

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.PowerManager
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.prominent_app_bar.topAppBar
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseActivity

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
    if (isSDKLessThanM()) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

fun setupAppBar(activity: BaseActivity, toolbar: Toolbar) {
    activity.setSupportActionBar(toolbar)
    adjustMenuVisibility(activity)
    toolbar.setNavigationOnClickListener {
        activity.onBackPressed()
    }
}

fun adjustMenuVisibility(
    activity: Activity,
    isFollowingTab: Boolean = false,
    followingSessionsNumber: Int = 0
) {
    val visibility =
        if (isFollowingTab && followingSessionsNumber >= 2) View.VISIBLE else View.INVISIBLE
    activity.topAppBar?.findViewById<ImageView>(R.id.reorder_sessions_button)?.visibility =
        visibility
}

fun isValidEmail(target: String): Boolean {
    return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches())
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    val name = context.applicationContext.packageName
    if (isSDKGreaterOrEqualToM()) {
        return pwrm.isIgnoringBatteryOptimizations(name)
    }
    return true
}
