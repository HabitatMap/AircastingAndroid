package pl.llp.aircasting.util.extensions

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.local.repository.ExpandedCardsRepository
import java.io.FileInputStream

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}

fun labelFormat(value: Float?): String {
    return "%d".format(value?.toInt())
}

fun isValidEmail(target: String): Boolean {
    return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches())
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

fun EditText.setStyle(mHint: String, mHintColor: Int) {
    this.apply {
        hint = mHint
        textSize = 15.0f
        setHintTextColor(ContextCompat.getColor(this.context, mHintColor))
    }
}

fun View.disableForASecond() {
    this.isEnabled = false

    postDelayed({
        this.isEnabled = true
    }, 1000)
}

fun expandedCards(): ExpandedCardsRepository? = ExpandedCardsRepository.getInstance()

fun GoogleMap.setMapTypeToSatellite() {
    this.mapType = GoogleMap.MAP_TYPE_HYBRID
}

fun ImageView.startAnimation() {
    this.visible()
    drawable.startAnimation()
}

fun ImageView.stopAnimation() {
    this.gone()
    drawable.stopAnimation()
}

fun Drawable.startAnimation() {
    (this as Animatable).start()
}
fun runOnIOThread(block: (scope: CoroutineScope) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        block(this)
    }
}

fun Drawable.stopAnimation() {
    (this as Animatable).stop()
}

fun backToUIThread(scope: CoroutineScope, uiBlock: () -> Unit) {
    scope.launch(Dispatchers.Main) {
        uiBlock()
    }
}

fun encodeToBase64(filepath: Uri?): String? {
    return try {
        val inputStream = FileInputStream(filepath?.path)
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes)
        encodeToString(bytes, DEFAULT)
    } catch (e: Exception) {
        null
    }
}