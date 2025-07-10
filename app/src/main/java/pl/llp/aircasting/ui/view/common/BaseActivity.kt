package pl.llp.aircasting.ui.view.common

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.isNotConnected
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    private var connectivityBanner: View? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isNetworkCallbackRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as AircastingApplication).apply {
            appComponent.inject(this@BaseActivity)
            userDependentComponent?.inject(this@BaseActivity)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupRootView()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setupRootView()
    }

    private fun setupRootView() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        setupConnectivityBanner(rootView)
        applyEdgeToEdgeInsets(rootView)
        configureStatusBarIcons()
    }

    private fun setupConnectivityBanner(rootView: ViewGroup) {
        connectivityBanner =
            layoutInflater.inflate(R.layout.layout_connectivity_banner, rootView, false)
        connectivityBanner?.visibility = View.GONE

        rootView.addView(connectivityBanner)

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        setupNetworkCallback()
    }

    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    hideBanner()
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    showBanner()
                }
            }
        }
    }

    private fun showBanner() {
        connectivityBanner?.let { banner ->
            if (banner.visibility != View.VISIBLE) {
                banner.visibility = View.VISIBLE
                banner.translationY = -banner.height.toFloat()
                banner.animate().translationY(0f).alpha(1f).duration = 300
            }
        }
    }

    private fun hideBanner() {
        connectivityBanner?.let { banner ->
            if (banner.isVisible) {
                banner.animate().translationY(-banner.height.toFloat()).alpha(0f).apply {
                    duration = 300
                    withEndAction { banner.visibility = View.GONE }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (settings.isKeepScreenOnEnabled()) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        registerNetworkCallback()

        if (isNotConnected) {
            showBanner()
        } else {
            hideBanner()
        }
    }

    private fun registerNetworkCallback() {
        if (!isNetworkCallbackRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
            isNetworkCallbackRegistered = true
        }
    }

    override fun onDestroy() {
        unregisterNetworkCallback()
        super.onDestroy()
    }

    private fun unregisterNetworkCallback() {
        if (isNetworkCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isNetworkCallbackRegistered = false
        }
    }

    protected open fun applyEdgeToEdgeInsets(rootView: View) {
        rootView.updateInsets()
    }

    private fun configureStatusBarIcons() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.isAppearanceLightStatusBars = settings.isDarkThemeEnabled().not()
    }
}

enum class ViewEdge {
    LEFT, RIGHT, TOP, BOTTOM
}

fun View.updateInsets(
    vararg edges: ViewEdge = arrayOf(
        ViewEdge.LEFT,
        ViewEdge.RIGHT,
        ViewEdge.TOP,
        ViewEdge.BOTTOM
    )
) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )

        v.updatePadding(
            top = if (ViewEdge.TOP in edges) insets.top else paddingTop,
            bottom = if (ViewEdge.BOTTOM in edges) insets.bottom else paddingBottom,
            left = if (ViewEdge.LEFT in edges) insets.left else paddingLeft,
            right = if (ViewEdge.RIGHT in edges) insets.right else paddingRight,
        )

        WindowInsetsCompat.CONSUMED
    }
}

fun <T : ViewDataBinding> BaseActivity.setContentViewWithDataBinding(
    @LayoutRes layoutResId: Int
): T {
    val binding: T = DataBindingUtil.inflate(layoutInflater, layoutResId, null, false)
    setContentView(binding.root)
    return binding
}