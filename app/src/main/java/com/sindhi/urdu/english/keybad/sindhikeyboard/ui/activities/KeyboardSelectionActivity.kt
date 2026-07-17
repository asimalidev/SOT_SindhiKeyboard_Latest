package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.activities.AppCompatBaseActivity
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityKeyboardSelectionBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.checkIfKeyboardEnabled
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.isInputMethodEnabled
import kotlin.system.exitProcess

class KeyboardSelectionActivity : AppCompatBaseActivity() {

    lateinit var binding: ActivityKeyboardSelectionBinding

    private lateinit var oneBlueDrawable: Drawable
    private lateinit var twoBlueDrawable: Drawable
    private lateinit var oneGreenDrawable: Drawable
    private lateinit var twoGreenDrawable: Drawable

    private lateinit var tickDrawable: Drawable
    lateinit var nextDrawable: Drawable

    var isPurchased: Boolean? = null
    var bundle = Bundle()
    lateinit var shakeAnimation: TranslateAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
                Glide.with(this@KeyboardSelectionActivity)
                    .asBitmap()
                    .load(R.drawable.ic_selection_theme)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.ivActivateKeyboard)


        bundle.putString("SelectKeyboardActivity","SelectKeyboardActivity")
        FirebaseLog.getAnalytics(this).logEvent("event_select_keyboard", bundle)


        supportActionBar?.hide()
      isPurchased = getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS), 0)

        shakeAnimation = TranslateAnimation(0f, 20f, 0f, 0f)
        shakeAnimation.duration = 100
        shakeAnimation.repeatMode = Animation.REVERSE
        shakeAnimation.repeatCount = 10

        oneBlueDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_one_default)!!
        twoBlueDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_two_default)!!
        oneGreenDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_one_blue)!!
        twoGreenDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_two_blue)!!
        tickDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_tick)!!
        nextDrawable = ContextCompat.getDrawable(this@KeyboardSelectionActivity, R.drawable.ic_next)!!

        nextMove()

        binding.btnActivate.blockingClickListener {
            if (!checkIfKeyboardEnabled()) {
                try {
                    startActivityForResult(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0)
                } catch (ignored: ActivityNotFoundException) {
                }
            } else {
                binding.btnSwitch.startAnimation(shakeAnimation)
            }
        }

        binding.btnSwitch.blockingClickListener {
            if (checkIfKeyboardEnabled()) {
                try {
                    val mgr = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    mgr.showInputMethodPicker()
                } catch (ignored: ActivityNotFoundException) { }
            } else {
                binding.btnActivate.startAnimation(shakeAnimation)
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
                exitProcess(0)
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        if (NetworkCheck.isNetworkAvailable(this)
            && !isPurchased!!
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(ADS_NATIVE_SELECT_KEYBOARD,"ON").equals("ON",true)) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(this)
            && !isPurchased!!
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(COLLAPSIBLE_SELECT_KEYBOARD,"ON").equals("ON",true)) {
            binding.shimmerLayoutBanner.startShimmer()
            binding.shimmerLayoutBanner.visibility = View.VISIBLE
            binding.adViewContainer.visibility = View.VISIBLE
            binding.separator.visibility = View.VISIBLE

            loadAdmobCollapsibleBannerAd()
        } else {
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        this.hideSystemUIUpdated()
    }

    private fun loadAdmobNativeAd() {
        val pref =getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
           // pref.getString(NATIVE_HOME,"ca-app-pub-3747520410546258/7968970100")
            pref.getString(NATIVE_HOME, resources.getString(R.string.admob_native_home))
        }
        else {
            resources.getString(R.string.admob_native_home)
        }

            NewNativeAdClass.checkAdRequestAdmob(
            mContext = this@KeyboardSelectionActivity,
            adId = adId!!,
            fragmentName = "KeyboardSelectionFragment",
            isMedia = true,
            isMediaOnLeft = true,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = true,
            onFailed = {
                binding.nativeAdContainerAd.visibility = View.GONE

            },
            onAddLoaded = {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            })
    }

    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("KeyboardSelectionFragment")) {
            val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["KeyboardSelectionFragment"]
            Handler().postDelayed({
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)
                binding.separator.visibility = View.VISIBLE
                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            },500)
        } else {
            loadCollapsibleBanner()
        }
    }

    private fun loadCollapsibleBanner() {
        val pref =getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
          //  pref.getString(BANNER_INSIDE,"ca-app-pub-3747520410546258/1697692330")
            pref.getString(BANNER_INSIDE,resources.getString(R.string.admob_banner_inside))
        }
        else{
            resources.getString(R.string.admob_banner_inside)
        }
        val adView = AdView(this)
        adView.setAdSize(adSize)
        adView.adUnitId =adId!!
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["KeyboardSelectionFragment"] = adView
                }
                binding.separator.visibility = View.VISIBLE
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.separator.visibility = View.GONE
            }

            override fun onAdOpened() {

            }

            override fun onAdClicked() {

            }

            override fun onAdClosed() {

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

    private fun nextMove() {
            if (checkIfKeyboardEnabled() && isInputMethodEnabled()) {
                activateState(true)
                switchState(true)
                Handler().postDelayed({
                    // CHANGED: Assigned intent to a variable and added startActivity()
                    val intent = Intent(this, SubscriptionActivity::class.java).apply {
                        putExtra("fromName", "SelectKeyboard")
                        putExtra("MoveTo", intent.getStringExtra("MoveTo"))
                    }
                    startActivity(intent) // <--- THIS WAS MISSING!
                    finish()
                }, 0)
            } else if (!checkIfKeyboardEnabled() && !isInputMethodEnabled()) {
            binding.btnActivate.startAnimation(shakeAnimation)
            activateState(false)
            switchState(false)
        } else {
            if (checkIfKeyboardEnabled()) {
                activateState(true)
            } else {
                binding.btnActivate.startAnimation(shakeAnimation)
                activateState(false)
            }

            if (isInputMethodEnabled()) {
                switchState(true)
            } else {
                binding.btnSwitch.startAnimation(shakeAnimation)
                switchState(false)
            }
        }
    }

    private fun activateState(state: Boolean) {
        if (state) {
            binding.btnActivate.background = AppCompatResources.getDrawable(this, R.drawable.bg_enabled_keyboard)
            binding.btnActivate.setCompoundDrawablesRelativeWithIntrinsicBounds(oneGreenDrawable,null,tickDrawable,null)
        } else {
            binding.btnActivate.background = AppCompatResources.getDrawable(this, R.drawable.bg_disabled_keyboard)
            binding.btnActivate.setCompoundDrawablesRelativeWithIntrinsicBounds(oneBlueDrawable,null,nextDrawable,null)
        }
    }

    private fun switchState(state: Boolean) {
        if (state) {
            binding.btnSwitch.background = AppCompatResources.getDrawable(this, R.drawable.bg_enabled_keyboard)
            binding.btnSwitch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                twoGreenDrawable,
                null,
                tickDrawable,
                null
            )
        } else {
            binding.btnSwitch.background = AppCompatResources.getDrawable(this, R.drawable.bg_disabled_keyboard)
            binding.btnSwitch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                twoBlueDrawable,
                null,
                nextDrawable,
                null
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.enabledInputMethodList.toString().contains(packageName)) {
                nextMove()
            }
        }
    }
}