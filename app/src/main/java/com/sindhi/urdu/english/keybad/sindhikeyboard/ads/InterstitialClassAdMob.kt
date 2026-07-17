package com.sindhi.urdu.english.keybad.sindhikeyboard.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.manual.mediation.library.sotadlib.utils.AdLoadingDialog
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.ForegroundCheckTask
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.INTER_OVER_ALL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


@SuppressLint("StaticFieldLeak")
object InterstitialClassAdMob : CoroutineScope by MainScope() {
    var logTag = "AdsFactoryInterstitial"
    private var adShowingDelayTime = 1000
    var isInterstitialAdVisible = false
    var isFirstTimeSessionRequest = true
    var mContextAdmob: Context? = null
    var isAdmobAdLoaded = false
    var isAdmobAdRequestSent = false
    var admobInterstitialAd: InterstitialAd? = null
    private var isShowDialog = true

    var onAdClosedCallBackAdmob: (() -> Unit)? = null
    var onAdLoadedCallBackAdmob: (() -> Unit)? = null

    var oddEvenCount = 0
    fun checkAndLoadAdMobInterstitial(context: Context?, nameFragment: String, onAdLoadedCallAdmob: (() -> Unit)? = null) {
        mContextAdmob = context

        if (onAdLoadedCallAdmob != null) {
            onAdLoadedCallBackAdmob = onAdLoadedCallAdmob
        }

        if (NetworkCheck.isNetworkAvailable(mContextAdmob)) {
            if (admobInterstitialAd == null && !isAdmobAdRequestSent) {
                if (ForegroundCheckTask().execute(mContextAdmob).get()) {
                    loadAdmobInterstitial(nameFragment)
                }
            }
        }
    }


    fun resetSessionState() {
        isFirstTimeSessionRequest = true
        isAdmobAdLoaded = false
        isAdmobAdRequestSent = false
        admobInterstitialAd = null
        isInterstitialAdVisible = false
    }

    fun showIfAvailableOrLoadAdMobInterstitial(
        context: Context?,
        nameFragment: String,
        onAdClosedCallBackAdmob: () -> Unit,
        onAdShowedCallBackAdmob: () -> Unit
    ) {
        Log.d("AdDebug", "========== SHOW OR LOAD TRIGGERED ==========")
        Log.d("AdDebug", "Current State -> isAdmobAdLoaded: $isAdmobAdLoaded, isFirstTime: $isFirstTimeSessionRequest")

        mContextAdmob = context
        isShowDialog = true
        this.onAdClosedCallBackAdmob = onAdClosedCallBackAdmob

        if (isAdmobAdLoaded) {
            Log.d("AdDebug", "🟢 Ad is ALREADY LOADED. Showing immediately.")
            // If it somehow loaded instantly, show it.
            showAdmobInterstitial(onAdShowedCallBackAdmob, nameFragment)
        } else {
            // AD IS NOT LOADED YET. Check if it's the first time.
            if (isFirstTimeSessionRequest) {
                Log.d("AdDebug", "🟡 FIRST TIME: Requesting ad in background, skipping Wait Dialog, navigating away instantly.")

                // Set flag to false so the SECOND time, it shows the wait dialog
                isFirstTimeSessionRequest = false

                // Trigger the load in the background (pass null for the callback so it doesn't try to show)
                checkAndLoadAdMobInterstitial(context, nameFragment, null)

                // Trigger the close callback instantly so the user navigates away without waiting
                this.onAdClosedCallBackAdmob?.invoke()

            } else {
                Log.d("AdDebug", "🔴 SECOND TIME ONWARD: Triggering Load-on-Demand and showing Wait Dialog.")
                showWaitDialog()

                checkAndLoadAdMobInterstitial(context, nameFragment) {
                    Log.d("AdDebug", "🟢 Load-on-Demand SUCCESS. Dismissing dialog and showing ad.")
                    dismissWaitDialog()
                    showAdmobInterstitial(onAdShowedCallBackAdmob, nameFragment)
                }

                // Safety timeout: If ad doesn't load in 5 seconds, exit
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isAdmobAdLoaded && !isInterstitialAdVisible) {
                        Log.d("AdDebug", "⏰ TIMEOUT REACHED (5 seconds). Ad didn't load in time. Closing.")
                        dismissWaitDialog()
                        this.onAdClosedCallBackAdmob?.invoke()
                    }
                }, 5000)
            }
        }
    }

    private fun loadAdmobInterstitial(nameFragment: String) {
        val pref = mContextAdmob?.getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref?.getString(INTER_OVER_ALL, "ca-app-pub-3747520410546258/9322591981")
        } else {
            mContextAdmob?.resources?.getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
        }

        if (admobInterstitialAd == null && !isAdmobAdRequestSent) {
            isAdmobAdRequestSent = true
            Log.d("AdDebug", "📡 Sending Ad Request to AdMob server now...")

            val adRequestInterstitial = AdRequest.Builder().build()
            InterstitialAd.load(mContextAdmob!!, adId!!, adRequestInterstitial, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("AdDebug", "✅ AdMob Interstitial LOADED successfully from server.")
                    admobInterstitialAd = interstitialAd
                    isAdmobAdLoaded = true

                    onAdLoadedCallBackAdmob?.let {
                        Log.d("AdDebug", "Invoking onAdLoadedCallBackAdmob.")
                        it.invoke()
                        onAdLoadedCallBackAdmob = null
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("AdDebug", "❌ AdMob Interstitial FAILED to load: ${loadAdError.message}")
                    if (mContextAdmob != null && nameFragment != "") {
                        CustomFirebaseEvents.interstitialAdEvent(mContextAdmob!!, nameFragment)
                    }

                    admobInterstitialAd = null
                    isAdmobAdLoaded = false
                    isAdmobAdRequestSent = false
                }
            })
        } else {
            Log.d("AdDebug", "Skipping load request. isAdmobAdRequestSent: $isAdmobAdRequestSent, admobInterstitialAd is null: ${admobInterstitialAd == null}")
        }
    }


    private fun showAdmobInterstitial(onAdShowedCallBackAdmob: () -> Unit, nameFragment: String) {
        showWaitDialog()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                dismissWaitDialog()
                if (admobInterstitialAd != null) {
                    admobInterstitialAd!!.show(mContextAdmob as Activity)
                    isInterstitialAdVisible = true
                    admobInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                onAdClosedCallBackAdmob?.invoke()
                                Log.e(logTag, "Admob Interstitial Closed.")
                                admobInterstitialAd = null
                                isInterstitialAdVisible = false
                                isAdmobAdLoaded = false
                                isAdmobAdRequestSent = false
                                if (!nameFragment.equals("ExitScreen")) {
                                    checkAndLoadAdMobInterstitial(context = mContextAdmob,"")
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                onAdClosedCallBackAdmob?.invoke()
                                Log.e(logTag,"Admob Interstitial Failed to Show Full Screen Content.\n" + adError.message)
                                if (BuildConfig.DEBUG) {
                                    Toast.makeText(mContextAdmob,"Interstitial :: AdMob :: Loaded But Failed to Show Full Screen",Toast.LENGTH_LONG).show()
                                }
                                admobInterstitialAd = null
                                isInterstitialAdVisible = false
                                isAdmobAdLoaded = false
                                isAdmobAdRequestSent = false
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                onAdShowedCallBackAdmob.invoke()
                            }
                        }
                } else {
                    onAdClosedCallBackAdmob?.invoke()
                }
            }, adShowingDelayTime.toLong())
        } catch (e: Exception) {
            dismissWaitDialog()
        }
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            mContextAdmob?.let {
                val view = (it as Activity).layoutInflater.inflate(com.manual.mediation.library.sotadlib.R.layout.dialog_adloading, null, false)
                isShowDialog = true
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false).showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextAdmob?.let {
            AdLoadingDialog.dismissDialog((it as Activity))
        }
    }
}