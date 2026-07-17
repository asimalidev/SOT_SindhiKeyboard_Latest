package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentThemesApplyBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.ApplyThemePreview
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADS_NATIVE_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_THEMES_APPLIED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import kotlinx.coroutines.launch


class ThemesApplyFragment : Fragment() {
    private lateinit var binding: FragmentThemesApplyBinding
    var isPurchased: Boolean? = null
    var navController: NavController? = null
    val argsThemes: ThemesApplyFragmentArgs by navArgs()
    private val defBanner = "ca-app-pub-3747520410546258/3066582155"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentThemesApplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()



        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            ApplicationClass.selectedTheme?.let {
                ApplyThemePreview(
                    ApplicationClass.selectedTheme!!.name,
                    ApplicationClass.selectedTheme!!.backgroundColor,
                    ApplicationClass.selectedTheme!!.backgroundColor2,
                    requireContext()
                )
            }
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(BANNER_THEMES_APPLIED, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeFragment")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"]
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            } else {
                loadBanner()
            }
        } else {

            binding.adViewContainer.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }

        binding.tvApplyTheme.blockingClickListener {
            if (navController != null) {
                Toast.makeText(requireContext(),"Applying Theme...!", Toast.LENGTH_SHORT).show()
                ApplicationClass.selectedTheme?.let { theme ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        MyPreferences(requireContext()).setTheme(theme)
                    }

                    if (NetworkCheck.isNetworkAvailable(context) && !(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean(PURCHASE,false))
                        && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(Preferences.INTERSTITIAL_THEME_APPLIED,"ON").equals("ON",true)) {
                        if (navController != null) {
                            binding.tvApplyTheme.isClickable = false
                            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                                context = context,
                                nameFragment = "ThemesApplyFragment",
                                onAdClosedCallBackAdmob = {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (isAdded) {
                                            Toast.makeText(requireContext(),"Theme Applied...!", Toast.LENGTH_SHORT).show()
                                            val action = ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesTestFragment()
                                            navController?.safeNavigate(action)
                                        }
                                    },300)
                                },
                                onAdShowedCallBackAdmob = { })
                        } else {
                            isNavControllerAdded()
                        }
                    } else {
                        Toast.makeText(requireContext(),"Theme Applied...!", Toast.LENGTH_SHORT).show()
                        val action = ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesTestFragment()
                        navController?.safeNavigate(action)
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                argsThemes.let {
                    if (navController != null) {
                        when (argsThemes.directFromFragment) {
                            "ExitScreenFragment" -> {
                                val action = ThemesApplyFragmentDirections.actionThemesApplyFragmentToNavHome()
                                navController?.safeNavigate(action)
                            }
                            "ThemesFragment" -> {
                                val action = ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesFragment("")
                                navController?.safeNavigate(action)
                            }
                            else -> {}
                        }
                    } else {
                        isNavControllerAdded()
                    }
                }
            }
        })
    }

    private fun loadBanner() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(ADMOB_BANNER_THEMES, defBanner)
        } else {
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        val adView = AdView(requireActivity())
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
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
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

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireActivity(),
                adWidth
            )
        }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.back),null,null,null)
            txtSindhiKeyboard.text = resources.getString(R.string.label_apply_theme)
            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx
            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        requireActivity().onBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "ThemesApplyFragment")

        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true
        if (isPurchased == true) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(Preferences.ADS_NATIVE_THEMES_APPLY,"ON").equals("ON",true)) {
                val pref =requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId  =if (!BuildConfig.DEBUG){
                    pref.getString(ADS_NATIVE_THEMES_APPLY,"ca-app-pub-3747520410546258/6696428641")
                }
                else{
                    resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
                }
                    NewNativeAdClass.checkAdRequestAdmob(
                        mContext = requireActivity(),
                        adId = adId!!,//getString(R.string.admob_native),
                        fragmentName = "ThemesFragment",
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
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

}