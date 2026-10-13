package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityConfirmUninstallBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_UNINSTAL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_UNINSTALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideSystemUIUpdated

class ConfirmUninstallActivity : AppCompatActivity() {
    private  var _binding: ActivityConfirmUninstallBinding? =null
    private val binding get() = _binding
    private  lateinit var pref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityConfirmUninstallBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.hide()
        supportActionBar?.hide()

        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        _binding?.root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                // 1. Use PADDING for the top so the background color extends behind the time bar
                v.setPadding(0, insets.top, 0, 0)

                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                    topMargin = 0 // 2. Remove the top margin!
                }
                WindowInsetsCompat.CONSUMED
            }
        }
        hideSystemUI()
        pref = getSharedPreferences(REMOTE_CONFIG, Context.MODE_PRIVATE)
        loadNativeAd()
        _binding?.imgBack?.blockingClickListener {
            whenBack()
        }

        binding?.btnTryAgain?.blockingClickListener {
            whenBack()
        }

        binding?.btnStillUninstall?.blockingClickListener {
            Intent(this, SurveyActivity::class.java).apply {
                startActivity(this)
            }
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                whenBack()
            }
        })

    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars()) // changed to show()
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    private fun whenBack() {
        val intent = Intent(this, NavigationActivity::class.java).apply {
            // Clears all activities on top of NavigationActivity
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun loadNativeAd(){
        if (isNetworkAvailable(this) && pref.getBoolean(
                NATIVE_UNINSTALL, true) && !pref.getBoolean(IS_PURCHASED,false)
        ) {
            val adId = if (!BuildConfig.DEBUG){pref.getString(AD_ID_NATIVE_UNINSTAL,getString(
                R.string.NATIVE_INSIDE_BIDDING))?:getString(R.string.NATIVE_INSIDE_BIDDING)} else{ getString(R.string.NATIVE_INSIDE_BIDDING)}
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = this,
                fragmentName = "uninstall",
                adId = adId,
                isMedia = true,
                isMediaOnLeft = true,
                adContainer = binding?.frAds,
                isMediumAd = true,
                onFailed = {
                    runOnUiThread{
                        _binding?.let {
                            it.frAds.visibility = View.GONE
                        }

                    }

                },
                onAddLoaded = {
                    runOnUiThread{
                        _binding?.let {
                            it.shimmerLayout.stopShimmer()
                            it.shimmerLayout.visibility = View.GONE
                        }

                    }

                })
        } else {
            binding?.frAds?.visibility = View.GONE
        }
    }
}