package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.dictionary

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.data.Language
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentTranslationBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_BANNER_TRANSLATOR_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_CONVERSATION_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_HISTORY_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_TEXT_TRANSLATOR_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_VOICE_TRANSLATOR_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_TRANSLATOR_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate

class TranslationFragment : Fragment() {
    private lateinit var binding: FragmentTranslationBinding
    var navController: NavController? = null
    lateinit var mSharedPreferences: SharedPreferences
    var bundle = Bundle()
    var isPurchase = false


    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed to navigation
                navigateToPhotoFragment()
            } else {
                // Permission denied
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Permanently denied (user denied 2 times / Don't ask again). Send to Settings.
                    openAppSettings()
                } else {
                    // Denied once. Optional: Show a message explaining why it's needed.
                    Toast.makeText(requireContext(), "Camera permission is required for image translation.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTranslationBinding.inflate(inflater, container, false)
        return binding.root
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        if (isNetworkAvailable(requireActivity())
            && !isPurchase
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_BANNER_TRANSLATOR_HOME_SCR, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("TranslatorFragment")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["TranslatorFragment"]
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

        if (isNetworkAvailable(requireActivity())
            && !isPurchase
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_NATIVE_TRANSLATOR_HOME_SCR, "ON").equals("ON", true)
        ) {
            val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
            val adId = if (!BuildConfig.DEBUG) {
                pref.getString(AD_ID_NATIVE_TRANSLATOR, resources.getString(R.string.NATIVE_DEFAULT))
            } else {
                resources.getString(R.string.NATIVE_DEFAULT)
            }
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                fragmentName = "TranslationFragment",
                adId = adId!!,
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

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonConversation)
            .let { it?.visibility = View.INVISIBLE }
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonTTS)
            .let { it?.visibility = View.INVISIBLE }

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = resources.getString(R.string.label_translation)

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        bundle.putString("TranslationFragment", "TranslationFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_translation", bundle)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // FIX: Check if we are actually on the TranslationFragment before navigating
                    if (navController?.currentDestination?.id == R.id.translationFragment) {
                        val action =
                            TranslationFragmentDirections.actionTranslationFragmentToNavHome()
                        navController?.safeNavigate(action)
                    } else {
                        // If we are on a different tab/fragment, let the system handle the back press naturally
                        isEnabled = false
                        requireActivity().onBackPressed()
                        isEnabled = true
                    }
                }
            })

        binding.clTextTranslator.blockingClickListener {
            onCvTextTranslatorClickWithInterstitial()
        }

        binding.clImageTranslation.blockingClickListener {
            if (navController != null) {
                val action = TranslationFragmentDirections.actionTranslationFragmentToPhotoFragment()
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }



        binding.clConversation.blockingClickListener {
            if (navController != null) {
                if (isPurchase!!) {
                    btnClConversation()
                } else {
                    if (isNetworkAvailable(requireContext())
                        && requireActivity().getSharedPreferences(
                            "RemoteConfig",
                            Context.MODE_PRIVATE
                        ).getString(SHOW_INTER_CONVERSATION_ENTER, "ON")
                            .equals("ON", true)
                    ) {
                        InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                            context = context,
                            nameFragment = "TranslationFragment",
                            onAdClosedCallBackAdmob = {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    btnClConversation()
                                }, 300)
                            },
                            onAdShowedCallBackAdmob = { })
                    } else {
                        btnClConversation()
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }

        binding.clSpeechToText.blockingClickListener {
            if (navController != null) {
                if (isPurchase!!) {
                    btnClSpeechToText()
                } else {
                    if (isNetworkAvailable(requireContext())
                        && requireActivity().getSharedPreferences(
                            "RemoteConfig",
                            Context.MODE_PRIVATE
                        ).getString(SHOW_INTER_VOICE_TRANSLATOR_ENTER, "ON")
                            .equals("ON", true)
                    ) {
                        InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                            context = context,
                            nameFragment = "TranslationFragment",
                            onAdClosedCallBackAdmob = {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    btnClSpeechToText()
                                }, 300)
                            },
                            onAdShowedCallBackAdmob = { })
                    } else {
                        btnClSpeechToText()
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }

        binding.clImageTranslation.blockingClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted
                navigateToPhotoFragment()
            } else {
                // Request the permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.clHistroy.blockingClickListener {
            if (navController != null) {
                if (isPurchase!!) {
                    val action =
                        TranslationFragmentDirections.actionTranslationFragmentToHistoryFragment()
                    navController?.safeNavigate(action)
                } else {
                    if (isNetworkAvailable(requireContext())
                        && requireActivity().getSharedPreferences(
                            "RemoteConfig",
                            Context.MODE_PRIVATE
                        ).getString(SHOW_INTER_HISTORY_ENTER, "ON").equals("ON", true)
                    ) {
                        InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                            context = context,
                            nameFragment = "TranslationFragment",
                            onAdClosedCallBackAdmob = {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val action =
                                        TranslationFragmentDirections.actionTranslationFragmentToHistoryFragment()
                                    navController?.safeNavigate(action)
                                }, 300)
                            },
                            onAdShowedCallBackAdmob = { })
                    } else {
                        val action =
                            TranslationFragmentDirections.actionTranslationFragmentToHistoryFragment()
                        navController?.safeNavigate(action)
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }

    }

    private fun onCvTextTranslatorClickWithInterstitial() {
        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(SHOW_INTER_TEXT_TRANSLATOR_ENTER, "ON").equals("ON", true)) {
            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                context = requireActivity(),
                nameFragment = "TranslatorFragment",
                onAdClosedCallBackAdmob = {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onCvTextTranslatorClick()
                    },300)
                },
                onAdShowedCallBackAdmob = {
                }
            )
        } else {
            onCvTextTranslatorClick()
        }
    }

    private fun onCvTextTranslatorClick() {
        val action = TranslationFragmentDirections.actionTranslationToTextTranslation()
        navController?.navigate(action)
    }



    private fun btnClSpeechToText() {
        if (navController != null) {
            val fromCountryName = mSharedPreferences.getString("fromLangSTT", "English (En)")
            val fromCountryFlag = mSharedPreferences.getInt("fromFlagSTT", Language.English.imageId)
            val toCountryName = mSharedPreferences.getString("toLangSTT", "Urdu (اردو)")
            val toCountryFlag = mSharedPreferences.getInt("toFlagSTT", Language.Urdu.imageId)
            val action = TranslationFragmentDirections.actionTranslationFragmentToSpeechFragment(
                fromCountryName.toString(),
                fromCountryFlag,
                toCountryName.toString(),
                toCountryFlag
            )
            navController?.safeNavigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun btnClConversation() {
        if (navController != null) {
            val fromCountryName = mSharedPreferences.getString("fromlangconvo", "English (En)")
            val toCountryName = mSharedPreferences.getString("tolangconvo", "Sindhi (سنڌي)")
            val fromCountryFlag =
                mSharedPreferences.getInt("fromflagconvo", Language.English.imageId)
            val toCountryFlag = mSharedPreferences.getInt("toflagconvo", Language.Urdu.imageId)
            val action =
                TranslationFragmentDirections.actionTranslationFragmentToConversationFragment(
                    fromCountryName.toString(),
                    fromCountryFlag,
                    toCountryName.toString(),
                    toCountryFlag
                )
            navController?.safeNavigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun loadBanner() {
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(
                AD_ID_BANNER_TRANSLATOR,
                resources.getString(R.string.BANNER_DEFAULT)
            )
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }
        adView.adUnitId = adId!!

        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["TranslatorFragment"] = adView
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


    private fun navigateToPhotoFragment() {
        val action = TranslationFragmentDirections.actionTranslationFragmentToPhotoFragment()
        navController?.navigate(action)
    }

    private fun openAppSettings() {
        Toast.makeText(requireContext(), "Please grant camera permission in settings", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }

}