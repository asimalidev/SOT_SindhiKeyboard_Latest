package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentThemesBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.selectedTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.screens.ThemesScreen
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_THEME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_THEMES_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_THEMES_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate

class ThemesFragment : Fragment() {
    private lateinit var binding: FragmentThemesBinding
    var navController: NavController? = null
    var isPurchase = false
    var bundle = Bundle()
    val argsThemes: ThemesFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()

        binding.ivBackPress.setOnClickListener {
            myBackPressed()
        }

        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            ThemesScreen(context = requireContext(), onApplyThemeClick = { theme ->
                selectedTheme = theme
                try {
                    if (navController != null) {
                        val action =
                            ThemesFragmentDirections.actionThemesFragmentToThemesApplyFragment("ThemesFragment")
                        navController?.safeNavigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            })
        }

        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        if (isNetworkAvailable(requireActivity())
            && !isPurchase
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_BANNER_THEMES_LIST, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("abcdef")) {
                val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["abcdef"]
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
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }

        bundle.putString("ThemesFragment", "ThemesFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_themes", bundle)

        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    myBackPressed()
                }
            })
    }

    private fun loadBanner() {
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_BANNER_THEME,   resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }

        adView.adUnitId = adId!!

        val adRequest = AdRequest.Builder()
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["abcdef"] = adView
                }

                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
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

        if (isPurchase) {
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.separator.visibility = View.GONE
        } else {
            if (isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(SHOW_NATIVE_THEMES_LIST, "ON"
                    ).equals("ON", true)
            ) {
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(AD_ID_NATIVE_THEMES,  resources.getString(R.string.NATIVE_DEFAULT))
                } else {
                    resources.getString(R.string.NATIVE_DEFAULT)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "abcdef",
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onFailed = {
                        binding.nativeAdContainerAd.visibility = View.GONE
                        binding.separator.visibility = View.GONE
                    },
                    onAddLoaded = {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        binding.separator.visibility = View.VISIBLE

                    })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
                binding.separator.visibility = View.GONE
            }
        }
    }

    private fun myBackPressed() {
        argsThemes.let {
            if (navController != null) {
                argsThemes.let {
                    if (navController != null) {
                        when (argsThemes.fromFragment) {
                            "FromHome", "FromExploreFeature" -> {
                                val action =
                                    ThemesFragmentDirections.actionThemesFragmentToNavHome()
                                navController?.safeNavigate(action)
                            }

                            "SeeMore" -> {
                                val action =
                                    ThemesFragmentDirections.actionThemesFragmentToExitScreenFragment()
                                navController?.safeNavigate(action)
                            }

                            else -> {
                                val action =
                                    ThemesFragmentDirections.actionThemesFragmentToNavHome()
                                navController?.safeNavigate(action)
                            }
                        }
                    } else {
                        isNavControllerAdded()
                    }
                }
            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}
