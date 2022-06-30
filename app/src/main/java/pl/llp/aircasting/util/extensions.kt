package pl.llp.aircasting.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.PowerManager
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.prominent_app_bar.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseActivity

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
        if (isFollowingTab && followingSessionsNumber >= 2) View.VISIBLE else View.GONE
    activity.topAppBar?.apply {
        findViewById<ImageView>(R.id.reorderButton)?.visibility = visibility
        findViewById<ImageView>(R.id.search_follow_icon)?.visibility =
            if (isFollowingTab) View.VISIBLE else View.INVISIBLE
    }
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

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

val Context.isConnected: Boolean
    get() {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return when {
            isSDKGreaterOrEqualToM() -> {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
            else -> {
                // For older APIs
                val nwInfo = connectivityManager.activeNetworkInfo ?: return false
                nwInfo.isConnected
            }
        }
    }

fun MarkerOptions.icon(context: Context, @DrawableRes vectorDrawable: Int): MarkerOptions {
    this.icon(ContextCompat.getDrawable(context, vectorDrawable)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    })
    return this
}

fun getBitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorDrawableResourceId: Int
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
    val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun GoogleMap.drawMarkerOnMap(
    mContext: Context,
    options: MarkerOptions,
    lat: Double,
    lng: Double,
    uuid: String?
): Marker? {
    return addMarker(
        options
            .position(LatLng(lat, lng))
            .anchor(0.5f, 0.5f)
            .snippet(uuid)
            .icon(mContext, R.drawable.map_dot_with_circle_inside)
    )
}

fun initializePlacesApi(appContext: Context) {
    if (!Places.isInitialized()) Places.initialize(
        appContext,
        BuildConfig.PLACES_API_KEY
    )
}

fun View.setMargins(
    left: Int = this.marginLeft,
    top: Int = this.marginTop,
    right: Int = this.marginRight,
    bottom: Int = this.marginBottom,
) {
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMargins(left, top, right, bottom)
    }
}

fun EditText.setStyle(mHint: String, mHintColor: Int){
    this.apply {
        hint = mHint
        textSize = 15.0f
        setHintTextColor(ContextCompat.getColor(this.context, mHintColor))
    }
}
