package com.sindhi.urdu.english.keybad.sindhikeyboard.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.manual.mediation.library.sotadlib.utils.AdLoadingDialog
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED


class ResumeAd(globalClass: ApplicationClass? = null) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    companion object {
        var isShowingDialog = false
        var isShowingAd = false
        var isAdLoadInProgress = false
        var isLoadTimedOut = false
        var lastRequestTime: Long = 0 // Prevents rapid-fire requests
    }

    private var currentActivity: Activity? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var myApplicationClass: ApplicationClass? = globalClass

    init {
        myApplicationClass?.let {
            it.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val activity = currentActivity ?: return
        val className = activity.localClassName ?: ""

        Log.d("ResumeAd", "App Foregrounded in activity: $className")

        // 1. Interstitial Check
        if (InterstitialClassAdMob.isInterstitialAdVisible) {
            Log.d("ResumeAd", "Skipping Resume Ad: Interstitial is active.")
            return
        }

        // 2. GLOBAL STATE CHECK: Don't trigger if ANY instance is already loading/showing
        if (isShowingAd || isShowingDialog || isAdLoadInProgress) {
            Log.d("ResumeAd", "Skipping: Resume Ad already showing/loading in another instance.")
            return
        }

        // 3. DEBOUNCE CHECK: Prevent multiple calls within 3 seconds
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < 3000) {
            Log.d("ResumeAd", "Skipping: Another ad was requested less than 3 seconds ago.")
            return
        }

        // 4. EXCLUSION LIST
        val excludedActivities = listOf(
            "ui.activities.FOFStartActivity",
            "ui.activities.KeyboardSelectionActivity",
            "com.manual.mediation.library.sotadlib.activities.LanguageScreenOne",
            "com.manual.mediation.library.sotadlib.activities.WalkThroughConfigActivity",
            "com.manual.mediation.library.sotadlib.activities.LanguageScreenDup",
            "com.manual.mediation.library.sotadlib.activities.WelcomeScreenOne",
            "com.manual.mediation.library.sotadlib.activities.WelcomeScreenDup",
            "com.applovin.adview.AppLovinFullscreenActivity",
            "com.applovin.adview.AppLovinInterstitialActivity",
            "com.facebook.ads.AudienceNetworkActivity",
            "com.facebook.ads.internal.ipc.RemoteANActivity",
            "com.bytedance.sdk.openadsdk",
            "com.mbridge.msdk",
            "com.google.android.gms.ads.AdActivity"
        )

        if (excludedActivities.any { className.contains(it) }) {
            Log.d("ResumeAd", "Skipping: Activity $className is in exclusion list.")
            return
        }

        // 5. REMOTE CONFIG & NETWORK
        val prefs = activity.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val isPremium = prefs?.getBoolean(IS_PURCHASED, false) ?: false
        val isResumeEnabled = prefs?.getBoolean("RESUME_OVERALL", true) ?: true

        if (!isPremium && isResumeEnabled && NetworkCheck.isNetworkAvailable(
                activity
            )
        ) {
            Log.d("ResumeAd", "All checks passed. Requesting Resume Ad.")
            lastRequestTime = System.currentTimeMillis() // Update the debounce timer
            loadAndShowAd()
        }
    }

    private fun loadAndShowAd() {
        if (myApplicationClass != null && !NetworkCheck.isNetworkAvailable(myApplicationClass) && currentActivity?.localClassName.equals(
                "ui.activities.SplashActivity"
            )
        ) {
            return
        }

        isAdLoadInProgress = true
        isLoadTimedOut = false
        showWaitDialog()

        timeoutRunnable = Runnable {
            Log.d("ResumeAd", "Ad load timed out. Dismissing dialog.")
            isLoadTimedOut = true
            isAdLoadInProgress = false
            dismissWaitDialog()
        }
        handler.postDelayed(timeoutRunnable!!, 5000)

        val loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                handler.removeCallbacks(timeoutRunnable!!)
                isAdLoadInProgress = false

                if (isLoadTimedOut) {
                    Log.d("ResumeAd", "Ad loaded late (after 5s timeout). Discarding ad.")
                    return
                }

                showAdNow(ad)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                handler.removeCallbacks(timeoutRunnable!!)
                isAdLoadInProgress = false
                dismissWaitDialog()
            }
        }

        val request: AdRequest = AdRequest.Builder().build()
        val pref = currentActivity?.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val adId = pref?.getString(
            "RESUME_OVER_ALL",
            currentActivity?.resources?.getString(R.string.RESUME_OVER_ALL)
        )

        myApplicationClass?.applicationContext?.let {
            AppOpenAd.load(it, adId!!, request, loadCallback)
        }
    }

    private fun showAdNow(ad: AppOpenAd) {
        val fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                isShowingDialog = false
                dismissWaitDialog()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                isShowingAd = false
                isShowingDialog = false
                dismissWaitDialog()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                dismissWaitDialog()
            }
        }

        ad.fullScreenContentCallback = fullScreenContentCallback

        currentActivity?.let { activity ->
            if (!activity.isFinishing) {
                ad.show(activity)
            } else {
                dismissWaitDialog()
                isShowingAd = false // Reset just in case
            }
        }
    }


    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {
        dismissWaitDialog()
    }

    private fun showWaitDialog() {
        if (isShowingDialog) return

        currentActivity?.let { act ->
            if (!act.isFinishing) {
                isShowingDialog = true
                val view = act.layoutInflater.inflate(
                    com.manual.mediation.library.sotadlib.R.layout.dialog_adloading,
                    null,
                    false
                )
                AdLoadingDialog.setContentView(act, view = view, isCancelable = false)
                    .showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        isShowingDialog = false
        currentActivity?.let { act ->
            if (!act.isFinishing && !act.isDestroyed) {
                try {
                    AdLoadingDialog.dismissDialog(act)
                } catch (e: Exception) {
                    Log.e("ResumeAd", "Failed to dismiss dialog: ${e.message}")
                }
            }
        }
    }
}