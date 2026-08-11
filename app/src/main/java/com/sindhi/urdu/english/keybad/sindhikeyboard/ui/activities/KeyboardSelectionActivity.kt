package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.activities.AppCompatBaseActivity
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityKeyboardSelectionBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_NATIVE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.isKeyboardEnabled
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.isKeyboardSelected
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_SLKB_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_SLKB_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SELECT_KEYBOARD

class KeyboardSelectionActivity : AppCompatBaseActivity() {
    lateinit var binding: ActivityKeyboardSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(0, insets.top, 0, 0)

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = 0 // 2. Remove the top margin!
            }
            WindowInsetsCompat.CONSUMED
        }
        hideSystemUI()

        CustomFirebaseEvents.logEvent(this, eventName = "keyboard_setup_scr")
        CustomFirebaseEvents.activitiesFragmentEvent(this, "SelectKeyboardActivity")

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS),
            0
        )

        binding.btnEnableStep1.setOnClickListener {
            it.clearAnimation()
            if (!isKeyboardEnabled(this)) {
                try {
                    CustomFirebaseEvents.logEvent(this, eventName = "enable_scr_tap")
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    startActivity(intent)
                } catch (ignored: ActivityNotFoundException) {
                    Toast.makeText(this, "Unable to open keyboard settings.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.btnSelectStep2.setOnClickListener {
            it.clearAnimation() // Stops the animation immediately on click
            try {
                CustomFirebaseEvents.logEvent(this, eventName = "setkeyboard_scr_tap")
                val mgr = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                mgr.showInputMethodPicker()
            } catch (ignored: ActivityNotFoundException) {
            }
        }

        // Ads Logic
        if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, MODE_PRIVATE).getBoolean(
                Preferences.IS_PURCHASED, false
            )
            && getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                SHOW_NATIVE_SELECT_KEYBOARD,
                "ON"
            ).equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, MODE_PRIVATE).getBoolean(
                Preferences.IS_PURCHASED,
                false
            )
            && getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                SHOW_BANNER_SELECT_KEYBOARD,
                "ON"
            ).equals("ON", true)
        ) {
            binding.shimmerLayoutBanner.startShimmer()
            binding.shimmerLayoutBanner.visibility = View.VISIBLE
            binding.adViewContainer.visibility = View.VISIBLE
            loadAdmobCollapsibleBannerAd()
        } else {
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
        }

        // Initial UI State Check
        updateUIState()
    }

    private fun startPulseAnimation(view: View) {
        val scaleDownX = android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.06f)
        val scaleDownY = android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.06f)

        scaleDownX.duration = 300
        scaleDownY.duration = 300

        scaleDownX.repeatCount = android.animation.ObjectAnimator.INFINITE
        scaleDownY.repeatCount = android.animation.ObjectAnimator.INFINITE

        scaleDownX.repeatMode = android.animation.ObjectAnimator.REVERSE
        scaleDownY.repeatMode = android.animation.ObjectAnimator.REVERSE

        val animatorSet = android.animation.AnimatorSet()
        animatorSet.playTogether(scaleDownX, scaleDownY)
        animatorSet.start()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            updateUIState()
        }
    }

    private fun updateUIState() {
        if (isKeyboardEnabled(this)) {
            if (isKeyboardSelected(this)) {
                // Both steps complete! Move to next screen
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!NetworkCheck.isNetworkAvailable(this)) {
                        startActivity(
                            Intent(
                                this@KeyboardSelectionActivity,
                                NavigationActivity::class.java
                            )
                        )
                    } else {
                        val moveTo = intent.getStringExtra("MoveTo")
                        val intent = Intent(this, SubscriptionActivity::class.java).apply {
                            putExtra("fromName", "SelectKeyboard")
                            putExtra("MoveTo", moveTo)
                        }
                        startActivity(intent)
                        finish()
                    }
                }, 0)
            } else {
                // Step 1 done, move to Step 2
                binding.layoutStep1.visibility = View.GONE
                binding.layoutStep2.visibility = View.VISIBLE

                // FIX: Trigger animation for Step 2 button
                startPulseAnimation(binding.btnSelectStep2)
            }
        } else {
            // Step 1 not done yet
            binding.layoutStep1.visibility = View.VISIBLE
            binding.layoutStep2.visibility = View.GONE

            // FIX: Trigger animation for Step 1 button
            startPulseAnimation(binding.btnEnableStep1)
        }
    }


    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("KeyboardSelectionFragment")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["KeyboardSelectionFragment"]
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.INVISIBLE
            binding.adViewContainer.removeView(binding.shimmerLayoutBanner)
            val parent = collapsibleAdView?.parent as? ViewGroup
            parent?.removeView(collapsibleAdView)
            binding.adViewContainer.addView(collapsibleAdView)
        } else {
            loadCollapsibleBanner()
        }
    }

    private fun loadAdmobNativeAd() {
        val pref = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_NATIVE_SLKB_HOME, resources.getString(R.string.NATIVE_DEFAULT))
        } else {
            resources.getString(R.string.NATIVE_DEFAULT)
        }



        NewNativeAdClass.checkAdRequestAdmob(
            mContext = this@KeyboardSelectionActivity,
            adId = adId!!,
            fragmentName = "KeyboardSelectionFragment",
            isMedia = true,
            isMediaOnLeft = true,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = false,
            onFailed = {
                binding.nativeAdContainerAd.visibility = View.GONE
            },
            onAddLoaded = {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.INVISIBLE
            })
    }

    private fun loadCollapsibleBanner() {
        val pref = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_BANNER_SLKB_HOME, resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }
        val adView = AdView(this)
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.INVISIBLE
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                        OVERALL_BANNER_RELOADING,
                        "SAVE"
                    ).equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["KeyboardSelectionFragment"] =
                        adView
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.INVISIBLE
            }

            override fun onAdClosed() {
                val layoutParams =
                    binding.adViewContainer.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                binding.adViewContainer.layoutParams = layoutParams
            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onDestroy() {
        binding.adViewContainer.removeAllViews()
        super.onDestroy()
    }
}