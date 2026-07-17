package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentKeyboardTestBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADS_BANNER_THEMES_TEST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG

class KeyboardTestFragment : Fragment() {
    private var _binding: FragmentKeyboardTestBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private var isPremiumUser = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyboardTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Purchase Status
        updatePurchaseStatus()

        // 2. Setup Keyboard Input
        setupKeyboardInput()

        // 3. Setup Back Press Logic
        setupBackPressHandler()

        // 4. Initial Ad Check
        if (!isPremiumUser) {
            setupBannerAd()
        }
    }

    private fun updatePurchaseStatus() {
        val prefs = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
        isPremiumUser = prefs.getBoolean(IS_PURCHASED, false)
    }

    private fun setupKeyboardInput() {
        binding.etTestKeyboard.apply {
            isFocusableInTouchMode = true
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!performCustomNavigation()) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        updatePurchaseStatus()

        // Setup Toolbar UI
        setupToolbar()

        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "KeyboardTestFragment")

        // Manage Ads Visibility
        if (isPremiumUser) {
            binding.nativeAdContainerAd1.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayout.visibility = View.GONE
            binding.shimmerLayout1.visibility = View.GONE
        } else {
            setupNativeAd()
            setupBannerAd()
        }
    }

    private fun setupToolbar() {
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        ivClose?.visibility = View.INVISIBLE

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        txtSindhiKeyboard?.apply {
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null
            )
            text = resources.getString(R.string.label_test_theme)

            // ✅ ADD THIS: Set the gap between the drawable and the text
            // Convert your desired dp value (e.g., 12dp) to pixels
            val gapInDp = 12
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            compoundDrawablePadding = gapInPx

            val startDrawable = compoundDrawables[0]
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        if (!performCustomNavigation()) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    private fun setupNativeAd() {
        val prefs = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val isNativeEnabled = prefs.getString(Preferences.ADS_NATIVE_THEMES_APPLIED_TEST, "ON").equals("ON", true)

        if (isNetworkAvailable(requireContext()) && isNativeEnabled) {
            val adId = if (!BuildConfig.DEBUG) {
                prefs.getString(NATIVE_THEMES, resources.getString(R.string.admob_native))
            } else {
                resources.getString(R.string.admob_native)
            }

            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                adId = adId ?: "",
                fragmentName = "keybordtest",
                isMedia = true,
                isMediaOnLeft = true,
                adContainer = binding.nativeAdContainerAd1,
                isMediumAd = true,
                onFailed = {
                    binding.nativeAdContainerAd1.visibility = View.GONE
                },
                onAddLoaded = {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                }
            )
        } else {
            binding.nativeAdContainerAd1.visibility = View.GONE
        }
    }

    private fun setupBannerAd() {
        val prefs = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val isBannerEnabled = prefs.getString(ADS_BANNER_THEMES_TEST, "ON").equals("ON", true)

        if (isNetworkAvailable(requireContext()) && isBannerEnabled) {

            // Logic for Collapsible Banner from HashMap
            if (NativeMaster.collapsibleBannerAdMobHashMap?.containsKey("kettest") == true) {
                val collapsibleAdView = NativeMaster.collapsibleBannerAdMobHashMap!!["kettest"]

                Handler(Looper.getMainLooper()).postDelayed({
                    if (_binding != null) { // Check binding before accessing views
                        binding.shimmerLayout1.stopShimmer()
                        binding.shimmerLayout1.visibility = View.GONE
                        binding.adViewContainer.removeView(binding.shimmerLayout1)

                        (collapsibleAdView?.parent as? ViewGroup)?.removeView(collapsibleAdView)
                        binding.adViewContainer.addView(collapsibleAdView)
                    }
                }, 500)
            } else {
                loadNewBannerAd()
            }
        } else {
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayout1.stopShimmer()
            binding.shimmerLayout1.visibility = View.GONE
        }
    }

    private fun loadNewBannerAd() {
        val prefs = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            prefs.getString(ADMOB_BANNER_THEMES, resources.getString(R.string.admob_banner_inside))
        } else {
            resources.getString(R.string.admob_banner_inside)
        }

        val adView = AdView(requireActivity())
        adView.setAdSize(calculateAdSize())
        adView.adUnitId = adId ?: ""

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (_binding != null) {
                    binding.adViewContainer.removeAllViews()
                    binding.adViewContainer.addView(adView)
                    NativeMaster.collapsibleBannerAdMobHashMap?.put("kettest", adView)
                    Log.d("KeyboardTest", "Banner onAdLoaded")
                }
            }

            override fun onAdOpened() { Log.d("KeyboardTest", "Banner onAdOpened") }
            override fun onAdClicked() {}
            override fun onAdClosed() {}
        }

        adView.loadAd(AdRequest.Builder().build())
    }

    private fun calculateAdSize(): AdSize {
        val display = requireActivity().windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density
        var adWidthPixels = binding.adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)
    }

    private fun performCustomNavigation(): Boolean {
        // Safely attempt to find controller
        try {
            if (isAdded) {
                navController = findNavController()
            }
        } catch (e: Exception) {
            return false
        }

        navController?.let { nc ->
            if (nc.currentDestination?.id == R.id.themes_test_fragment) {
                val action = KeyboardTestFragmentDirections.actionThemesTestFragmentToNavHome()
                nc.navigate(action)
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to prevent memory leaks
    }
}