package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentExitScreenBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.selectedTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.exitScreenThemes.ExitScreenThemes
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.system.exitProcess

class ExitScreenFragment : Fragment() {

    lateinit var binding: FragmentExitScreenBinding
    var navController: NavController? = null
    var ivClose: ImageView? = null
    var isPurchase = false
    var doubleBackToExitPressedOnce = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExitScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()

        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            ExitScreenThemes(onApplyThemeClick = { theme ->
                selectedTheme = theme
                if (navController != null) {
                    InterstitialClassAdMob.checkAndLoadAdMobInterstitial(
                        requireActivity(),
                        "AfterExitLoad"
                    )
                    selectedTheme = theme
                    if (ivClose != null) {
                        ivClose?.visibility = View.INVISIBLE
                    }
                    try {
                        val action =
                            ExitScreenFragmentDirections.actionExitScreenFragmentToThemesApplyFragment(
                                "ExitScreenFragment"
                            )
                        navController?.navigate(action)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                } else {
                    isNavControllerAdded()
                }
            })
        }

        CoroutineScope(Dispatchers.IO).launch {
            CustomFirebaseEvents.activitiesFragmentEvent(
                requireActivity(),
                "ExitScreenFragment"
            )
        }

        if (isPurchase==true) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(
                        Preferences.ADS_NATIVE_EXIT, "ON"
                    ).equals("ON", true)
            ) {
                binding.g1ExitScreen.setGuidelinePercent(0.55f)
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(NATIVE_OVER_ALL, "ca-app-pub-3747520410546258/1702944653")
                } else {
                    resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,//getString(R.string.admob_native),
                    fragmentName = "OverallAtPoetryExit",
                    isMedia = true,
                    isMediaOnLeft = true,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = true,
                    onFailed = {
                        binding.nativeAdContainerAd.visibility = View.GONE
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    },
                    onAddLoaded = {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    })
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.nativeAdContainerAd.visibility = View.GONE
                binding.g1ExitScreen.setGuidelinePercent(0.9f)
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }

        binding.txtTapHereToExit.blockingClickListener {
            exitCode()
        }

        binding.btnExploreMore.blockingClickListener {
            InterstitialClassAdMob.checkAndLoadAdMobInterstitial(requireActivity(), "AfterExitLoad")
            if (ivClose != null) {
                ivClose?.visibility = View.INVISIBLE
            }
            onThemeClicked("FromExploreFeature")
        }

        binding.btnSeeMore.blockingClickListener {
            if (ivClose != null) {
                ivClose?.visibility = View.INVISIBLE
            }
            onThemeClicked("SeeMore")
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        exitCode()
                        Toast.makeText(requireContext(), "Exit Code Goes Here", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please Click again to EXIT !",
                            Toast.LENGTH_SHORT
                        ).show()
                        doubleBackToExitPressedOnce = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000)
                    }
                }
            })
    }

    private fun onThemeClicked(from: String) {
        if (navController != null) {
            val action = ExitScreenFragmentDirections.actionExitScreenFragmentToThemesFragment(from)
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun exitCode() {
        requireActivity().finishAffinity()
        exitProcess(0)
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        ivClose = requireActivity().findViewById(R.id.ivClose)
        if (ivClose != null) {
            ivClose?.visibility = View.VISIBLE

            ivClose?.blockingClickListener {
                // Fix: Use 'activity' instead of 'requireActivity()' to avoid crash if detached
                val currentActivity = activity

                if (currentActivity != null && isAdded) {
                    if (navController != null) {
                        // Use currentActivity safely here
                        InterstitialClassAdMob.checkAndLoadAdMobInterstitial(
                            currentActivity,
                            "AfterExitLoad"
                        )

                        // Ensure we are still on the correct destination before navigating
                        if (navController?.currentDestination?.id == R.id.exitScreen_fragment) {
                            val action =
                                ExitScreenFragmentDirections.actionKeypressFragmentToNavHome()
                            navController?.navigate(action)
                        }
                    } else {
                        isNavControllerAdded()
                    }
                }
            }
        }

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.text = resources.getString(R.string.label_explore_feature)
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        val gapInDp = 12 // Change this to make the gap bigger or smaller
        val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
        txtSindhiKeyboard.compoundDrawablePadding = gapInPx
    }

}