package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes

import android.R.attr.theme
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.ApplyThemePreview
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_THEME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_THEMES_APPLIED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate
import kotlinx.coroutines.launch


class ThemesApplyFragment : Fragment() {
    private lateinit var binding: FragmentThemesApplyBinding
    var isPurchased: Boolean? = null
    var navController: NavController? = null
    val argsThemes: ThemesApplyFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemesApplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()

        binding.ivBackPress.setOnClickListener {
            findNavController().popBackStack()
        }

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
                .getString(SHOW_BANNER_THEMES_APPLY, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("Theme")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["Theme"]
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
                Toast.makeText(requireContext(), "Applying Theme...!", Toast.LENGTH_SHORT).show()

                ApplicationClass.selectedTheme?.let { theme ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        MyPreferences(requireContext()).setTheme(theme)
                    }

                    if (NetworkCheck.isNetworkAvailable(context) && !(PreferenceManager.getDefaultSharedPreferences(
                            requireActivity()
                        ).getBoolean(PURCHASE, false)) && requireActivity().getSharedPreferences(
                            "RemoteConfig",
                            Context.MODE_PRIVATE
                        ).getString(SHOW_INTER_THEMES_APPLIED, "ON").equals("ON", true)
                    ) {
                        if (navController != null) {
                            binding.tvApplyTheme.isClickable = false
                            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                                context = context,
                                nameFragment = "ThemesApplyFragment",
                                onAdClosedCallBackAdmob = {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (isAdded) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Theme Applied...!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val action =
                                                ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesTestFragment()
                                            navController?.safeNavigate(action)
                                        }
                                    }, 300)
                                },
                                onAdShowedCallBackAdmob = { })
                        } else {
                            isNavControllerAdded()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Theme Applied...!", Toast.LENGTH_SHORT)
                            .show()
                        val action =
                            ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesTestFragment()
                        navController?.safeNavigate(action)
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }



        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    argsThemes.let {
                        if (navController != null) {
                            when (argsThemes.directFromFragment) {
                                "ExitScreenFragment" -> {
                                    val action =
                                        ThemesApplyFragmentDirections.actionThemesApplyFragmentToNavHome()
                                    navController?.safeNavigate(action)
                                }

                                "ThemesFragment" -> {
                                    val action =
                                        ThemesApplyFragmentDirections.actionThemesApplyFragmentToThemesFragment(
                                            ""
                                        )
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
            pref.getString(AD_ID_BANNER_THEME, resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
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
                    NativeMaster.collapsibleBannerAdMobHashMap!!["Theme"] = adView
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

    override fun onPause() {
        super.onPause()
        (activity as NavigationActivity).showtoolbar()
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        (activity as NavigationActivity).hidetoolbar()
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "ThemesApplyFragment")

        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        if (isPurchased == true) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(SHOW_NATIVE_THEMES_APPLY, "ON").equals("ON", true)
            ) {
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(
                        AD_ID_NATIVE_THEMES,
                        resources.getString(R.string.NATIVE_DEFAULT)
                    )
                } else {
                    resources.getString(R.string.NATIVE_DEFAULT)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "ApplyThemesFragment",
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