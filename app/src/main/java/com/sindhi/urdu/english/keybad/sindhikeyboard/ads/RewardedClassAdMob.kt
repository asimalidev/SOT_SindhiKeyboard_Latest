package com.sindhi.urdu.english.keybad.sindhikeyboard.ads

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.manual.mediation.library.sotadlib.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import android.app.Activity
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.manual.mediation.library.sotadlib.utils.AdLoadingDialog
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_REWARDED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG

@SuppressLint("StaticFieldLeak")
object RewardedClassAdMob : CoroutineScope by MainScope() {
    var logTag = "AdsFactoryRewarded"

    // Tracks if the ad is currently covering the screen
    var isRewardedAdVisible = false
    var mContextAdmob: Context? = null

    /**
     * Call this function to load and show the ad immediately.
     */
    fun loadAndShowRewardedAd(
        context: Context?,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit
    ) {
        if (context == null || !NetworkCheck.isNetworkAvailable(context)) {
            onAdDismissed()
            return
        }

        mContextAdmob = context
        showWaitDialog()


        val adId = if (BuildConfig.DEBUG) {
            mContextAdmob!!.getString(R.string.AD_ID_SOCCER_REWARDED)
        } else {
           mContextAdmob!!.getSharedPreferences(REMOTE_CONFIG,Context.MODE_PRIVATE)
                .getString(AD_ID_REWARDED,mContextAdmob!!.getString(R.string.AD_ID_REWARDED))
                ?:mContextAdmob!!.getString(R.string.AD_ID_REWARDED)
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(mContextAdmob!!, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                dismissWaitDialog()
                showAdNow(ad, onUserEarnedReward, onAdDismissed)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("onRewardedAdFailedToLoad", "onRewardedAdFailedToLoad :$error")
                dismissWaitDialog()
                isRewardedAdVisible = false
                onAdDismissed()
            }
        })
    }

    private fun showAdNow(
        ad: RewardedAd,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val activity = mContextAdmob?.findActivity()
        if (activity == null) {
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isRewardedAdVisible = false
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isRewardedAdVisible = false
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isRewardedAdVisible = true
            }
        }

        ad.show(activity) { rewardItem ->
            onUserEarnedReward(rewardItem)
        }
    }

    private fun showWaitDialog() {
        mContextAdmob?.findActivity()?.let { activity ->
            if (!activity.isFinishing) {
                val view = activity.layoutInflater.inflate(
                    com.manual.mediation.library.sotadlib.R.layout.dialog_adloading,
                    null,
                    false
                )
                AdLoadingDialog.setContentView(activity, view = view, isCancelable = false).showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextAdmob?.findActivity()?.let { activity ->
            AdLoadingDialog.dismissDialog(activity)
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}