package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSettingsBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.screens.PreferenceScreen
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    lateinit var navController: NavController
    private var isPurchase = false
    var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            PreferenceScreen(
                context = requireContext(),
                requireActivity = requireActivity()
            )
        }

        bundle.putString("SettingsFragment", "SettingsFragment")
        ApplicationClass.firebaseAnalyticsEventsLog.logEvent("event_settings", bundle)

        if (isAdded) {
            navController = findNavController()
        }

        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    navController.let {
                        if (it.currentDestination?.id == R.id.settingsFragment) {
                            it.navigate(SettingsFragmentDirections.actionSettingsFragmentToNavHome())
                        } else {
                            it.popBackStack()
                        }
                    }
                }
            })
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        // Use activity?.let to avoid multiple requireActivity() calls
        activity?.let { act ->
            // 1. Hide all unwanted icons
            act.findViewById<ImageView>(R.id.ivClose)?.visibility = View.INVISIBLE
            act.findViewById<LottieAnimationView>(R.id.ivPurchase)?.visibility = View.INVISIBLE

            // NEW: Explicitly hide the settings icon so it doesn't show up
            act.findViewById<ImageView>(R.id.ivSettings)?.visibility = View.INVISIBLE

            // 2. Setup the Toolbar Text
            val txtSindhiKeyboard = act.findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)

            // FIX: Add an isAdded check and use 'act' context inside the async .post block
            txtSindhiKeyboard?.post {
                if (!isAdded) return@post // Prevents the crash if the fragment is detached

                txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(act, R.drawable.back), // Use 'act' instead of requireContext()
                    null, null, null
                )
                txtSindhiKeyboard.text = act.getString(R.string.label_settings) // Use 'act.getString()'
            }

            txtSindhiKeyboard?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        act.onBackPressedDispatcher.onBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchase
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(ADS_NATIVE_SETTINGS, "ON").equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            binding.separator.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchase
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(COLLAPSIBLE_SETTINGS, "ON").equals("ON", true)
        ) {
            binding.shimmerLayoutBanner.startShimmer()
            binding.shimmerLayoutBanner.visibility = View.VISIBLE
            binding.adViewContainer.visibility = View.VISIBLE
            binding.separator.visibility = View.VISIBLE
            loadAdmobCollapsibleBannerAd()
        } else {
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
            binding.separator.visibility = View.GONE
        }
    }

    private fun loadAdmobNativeAd() {
        val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(NATIVE_OVER_ALL, "ca-app-pub-3747520410546258/1702944653")
        } else {
            resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
        }
        NewNativeAdClass.checkAdRequestAdmob(
            mContext = requireActivity(),
            adId = adId!!,
            fragmentName = "SettingsFragment",
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
    }

    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("SettingsFragment")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["SettingsFragment"]
            Handler().postDelayed({
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.separator.visibility = View.VISIBLE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            }, 500)
        } else {
            loadCollapsibleBanner()
        }
    }

    private fun loadCollapsibleBanner() {
        val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(BANNER_INSIDE, "ca-app-pub-3747520410546258/1697692330")
        } else {
            resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
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
                    NativeMaster.collapsibleBannerAdMobHashMap!!["SettingsFragment"] = adView
                }
                binding.separator.visibility = View.VISIBLE
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.separator.visibility = View.GONE
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
}