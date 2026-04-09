package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.PurchaseInfo
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentHomeBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.isHuaweiDevice
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.StickersViewActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.INTERSTITIAL_STICKER_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.ViewDialog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate

class HomeFragment : Fragment(), IBillingHandler {
    private var binding: FragmentHomeBinding? = null
    var navController: NavController? = null
    var isPurchased: Boolean? = null
    var bp: BillingProcessor? = null
    var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        val ivPurchase = requireActivity().findViewById<LottieAnimationView>(R.id.ivPurchase)

        if (isPurchased == true) {
            ivPurchase.visibility = View.GONE
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchased!!
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(COLLAPSIBLE_HOME, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeFragment")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"]
                binding?.shimmerLayoutBanner?.stopShimmer()
                binding?.shimmerLayoutBanner?.visibility = View.GONE
                binding?.adViewContainer?.removeView(binding?.shimmerLayoutBanner)
                binding?.separator?.visibility = View.VISIBLE

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding?.adViewContainer?.addView(collapsibleAdView)
            } else {
                loadBanner()
            }
        } else {
            binding?.adViewContainer?.visibility = View.GONE
            binding?.shimmerLayoutBanner?.stopShimmer()
            binding?.shimmerLayoutBanner?.visibility = View.GONE
        }


        val toolbarContainer = requireActivity().findViewById<View>(R.id.inclToolBar)
        toolbarContainer?.visibility = View.VISIBLE


        if (!requireActivity().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
                .getBoolean(IS_PURCHASED, false)
            && !isHuaweiDevice()
        ) {

            if (ivPurchase != null) {
                ivPurchase.visibility = View.VISIBLE
                ivPurchase.blockingClickListener {
                    if (isAdded && navController != null) {
                        CustomFirebaseEvents.activitiesFragmentEvent(
                            requireActivity(),
                            "home_scr_tap_removeads"
                        )
                        val action =
                            HomeFragmentDirections.actionNavHomeToNavSubscriptionFragment("Home")
                        navController?.safeNavigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                }
            }
        }


        requireActivity().findViewById<ConstraintLayout>(R.id.clEditing)
            .let { it?.visibility = View.INVISIBLE }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchased!! && requireActivity().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            )
                .getString(ADS_NATIVE_HOME, "ON").equals("ON", true)
        ) {

            val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
            val adId = if (!BuildConfig.DEBUG) {
                pref.getString(NATIVE_HOME, "ca-app-pub-3747520410546258/7968970100")
            } else {
                resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_1)
            }
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                fragmentName = "HomeFragment",
                adId = adId!!,
                isMedia = true,
                isMediaOnLeft = true,
                adContainer = binding?.nativeAdContainerAd,
                isMediumAd = true,
                onFailed = {
                    binding?.nativeAdContainerAd?.visibility = View.GONE
                },
                onAddLoaded = {
                    binding?.shimmerLayout?.stopShimmer()
                    binding?.shimmerLayout?.visibility = View.GONE
                })
        } else {
            binding?.nativeAdContainerAd?.visibility = View.GONE
        }

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            txtSindhiKeyboard.text = resources.getString(R.string.title_home)
            txtSindhiKeyboard.setOnTouchListener(null)
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val ivSettings = requireActivity().findViewById<ImageView>(R.id.ivSettings)
        if (ivSettings != null) {
            ivSettings.visibility = View.VISIBLE
            ivSettings.blockingClickListener {
                CustomFirebaseEvents.activitiesFragmentEvent(
                    requireActivity(),
                    "home_scr_tap_settings"
                )
                settingClick()
            }
        }
    }

    private fun loadBanner() {
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(BANNER_INSIDE, "ca-app-pub-3747520410546258/1697692330")
        } else {
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        if (adId != null) {
            adView.adUnitId = adId
        }
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding?.separator?.visibility = View.VISIBLE
                binding?.adViewContainer?.removeAllViews()
                binding?.adViewContainer?.addView(adView)
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                binding?.shimmerLayout?.stopShimmer()
                binding?.shimmerLayout?.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding?.shimmerLayout?.stopShimmer()
                binding?.shimmerLayout?.visibility = View.GONE
                binding?.separator?.visibility = View.GONE
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
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding?.adViewContainer?.width?.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels!! / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireActivity(),
                adWidth
            )
        }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        binding?.clEditor?.blockingClickListener {
            if (isAdded && navController != null) {
                val action = HomeFragmentDirections.actionNavHomeToNavEditors()
                // Use safeNavigate to ensure the action exists for the current destination
                navController?.safeNavigate(action)
            }
        }

        bundle.putString("HomeFragment", "HomeFragment")
        ApplicationClass.firebaseAnalyticsEventsLog.logEvent("event_home", bundle)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isAdded && navController != null) {
                        val action = HomeFragmentDirections.actionNavHomeToExitScreenFragment()
                        navController?.safeNavigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                }
            })




        binding!!.clTextTranslation.blockingClickListener {
            if (navController != null) {
                if (NetworkCheck.isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                        "RemoteConfig",
                        Context.MODE_PRIVATE
                    ).getString(Preferences.INTERSTITIAL_TRANSLATION_ENTER, "ON").equals("ON", true)
                ) {
                    if (isPurchased!!) {
                        onBtnTranslationClick()
                    } else {
                        showInterstitialAll(transition = "translation")
                    }
                } else {
                    onBtnTranslationClick()
                }
            } else {
                isNavControllerAdded()
            }
        }


        binding!!.clTextReverse.blockingClickListener {
            Toast.makeText(
                requireActivity(),
                getString(R.string.label_coming_soon),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding!!.clDictionary.blockingClickListener {
            Toast.makeText(
                requireActivity(),
                getString(R.string.label_coming_soon),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding!!.clThemes.blockingClickListener {
            if (navController != null) {
                if (NetworkCheck.isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                        "RemoteConfig",
                        Context.MODE_PRIVATE
                    ).getString(Preferences.INTERSTITIAL_THEME_ENTER, "ON").equals("ON", true)
                ) {
                    if (isPurchased!!) {
                        onBtnThemeClick()
                    } else {
                        showInterstitialAll(transition = "themes")
                    }
                } else {
                    onBtnThemeClick()
                }
            } else {
                isNavControllerAdded()
            }
        }

        binding!!.clStickers.blockingClickListener {
            CustomFirebaseEvents.activitiesFragmentEvent(
                requireActivity(),
                "home_scr_tap_sindhi_stickers"
            )
            if (NetworkCheck.isNetworkAvailable(requireActivity())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(INTERSTITIAL_STICKER_ENTER, "ON").equals("ON", true)
            ) {
                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                    context = requireActivity(),
                    nameFragment = "StickersViewActivity",
                    onAdClosedCallBackAdmob = {
                        if (isAdded && navController != null) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(
                                    Intent(
                                        requireActivity(),
                                        StickersViewActivity::class.java
                                    ).putExtra("From", "FromHome")
                                )
                            }, 300)
                        }
                    },
                    onAdShowedCallBackAdmob = {
                    }
                )
            } else {
                startActivity(
                    Intent(
                        requireActivity(),
                        StickersViewActivity::class.java
                    ).putExtra("From", "FromHome")
                )
            }
        }


        binding!!.clDailyStatus.blockingClickListener {
            if (navController != null) {
                if (NetworkCheck.isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                        "RemoteConfig", Context.MODE_PRIVATE
                    ).getString(Preferences.INTERSTITIAL_SINDHI_STATUS_ENTER, "ON")
                        .equals("ON", true)
                ) {
                    if (isPurchased!!) {
                        onBtnDailyStatus()
                    } else {
                        showInterstitialAll(transition = "dailystatus")
                    }
                } else {
                    onBtnDailyStatus()
                }
            } else {
                isNavControllerAdded()
            }
        }

        binding!!.clDisable.blockingClickListener {
            onBtnDisableClick()
        }
    }

    override fun onDestroy() {
        if (bp != null) {
            bp!!.release()
        }

        requireActivity().findViewById<LottieAnimationView>(R.id.ivPurchase)
            .let { it?.visibility = View.GONE }

        requireActivity().findViewById<ImageView>(R.id.ivSettings)
            .let { it?.visibility = View.GONE }
        super.onDestroy()
    }

    private fun onBtnThemeClick() {
        navController?.safeNavigate(HomeFragmentDirections.actionNavHomeToThemesFragment("FromHome"))
    }

    private fun onBtnTranslationClick() {
        navController?.safeNavigate(HomeFragmentDirections.actionNavHomeToTranslationFragment())
    }

    private fun onBtnDailyStatus() {
        navController?.safeNavigate(HomeFragmentDirections.actionNavHomeToNavSindhiStatus())
    }

    fun onBtnSettingClick() {
        navController?.safeNavigate(HomeFragmentDirections.actionNavHomeToSettingsFragment())
    }

    private fun onBtnDisableClick() {
        bundle = Bundle()
        bundle.putString("HomeFragment_DisableKeyboard", "HomeFragment_DisableKeyboard")
        ApplicationClass.firebaseAnalyticsEventsLog.logEvent("event_home_disable_keyboard", bundle)
        try {
            val mgr =
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.showInputMethodPicker()
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    private fun onRemoveAdsClick() {
        bp?.purchase(requireActivity(), getString(R.string.product_id))
    }

    private fun showRestartDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Purchase Successful")
            .setMessage("Restart is Required")
            .setIcon(R.drawable.icon)
            .setPositiveButton("Restart") { dialog: DialogInterface?, which: Int ->
                startActivity(Intent(requireContext(), FOFStartActivity::class.java))
            }
        builder.create()
        builder.show()
    }

    override fun onPause() {
        super.onPause()

        requireActivity().findViewById<ImageView>(R.id.ivSettings)
            .let { it?.visibility = View.GONE }

        requireActivity().findViewById<LottieAnimationView>(R.id.ivPurchase)
            .let { it?.visibility = View.GONE }
    }

    fun showInterstitialAll(transition: String) {
        InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
            requireActivity(),
            "HomeFragment",
            onAdClosedCallBackAdmob = {},
            onAdShowedCallBackAdmob = {})
        Handler(Looper.getMainLooper()).postDelayed({
            when (transition) {
                "themes" -> {
                    onBtnThemeClick()
                }

                "translation" -> {
                    onBtnTranslationClick()
                }

                "settings" -> {
                    onBtnSettingClick()
                }

                "dailystatus" -> {
                    onBtnDailyStatus()
                }
            }
        }, 1200)
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
            .putBoolean(PURCHASE, true).apply()
        showRestartDialog()
    }

    override fun onPurchaseHistoryRestored() {
        Log.d("something happened", "history restored")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.d("something happened", error?.message.toString())
    }

    override fun onBillingInitialized() {
        Log.d("something happened", "billing initialized")
    }
}

private fun HomeFragment.settingClick() {
    if (navController != null) {
        if (NetworkCheck.isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(Preferences.INTERSTITIAL_SETTINGS_ENTER, "ON").equals("ON", true)
        ) {
            if (isPurchased!!) {
                onBtnSettingClick()
            } else {
                showInterstitialAll(transition = "settings")
            }
        } else {
            onBtnSettingClick()
        }
    } else {
        isNavControllerAdded()
    }
}
