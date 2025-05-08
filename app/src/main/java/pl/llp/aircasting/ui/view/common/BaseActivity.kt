package pl.llp.aircasting.ui.view.common

import android.content.Context
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
import androidx.core.view.isVisible
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
        initConnectivityBanner()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        initConnectivityBanner()
    }

    private fun initConnectivityBanner() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        connectivityBanner =
            layoutInflater.inflate(R.layout.layout_connectivity_banner, rootView, false)
        connectivityBanner?.visibility = View.GONE

        rootView.addView(connectivityBanner)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

}

fun <T : ViewDataBinding> BaseActivity.setContentViewWithDataBinding(
    @LayoutRes layoutResId: Int
): T {
    val binding: T = DataBindingUtil.inflate(layoutInflater, layoutResId, null, false)
    setContentView(binding.root)
    return binding
}