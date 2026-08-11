package com.sindhi.urdu.english.keybad.sindhikeyboard.utils // Adjust package if needed

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mbridge.msdk.out.BannerAdListener
import com.mbridge.msdk.out.BannerSize
import com.mbridge.msdk.out.MBBannerView
import com.mbridge.msdk.out.MBridgeIds
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_KEYPAD
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

class KeyboardAdManager(
    private val serviceScope: CoroutineScope
) {
    private val logTag = "KeyboardAdManager"
    private val _admobAdView = MutableStateFlow<View?>(null)
    val admobAdView: StateFlow<View?> = _admobAdView.asStateFlow()
    private var currentNativeAd: NativeAd? = null
    private val isAdMobLoading = AtomicBoolean(false)
    private var lastAdMobLoadTime = 0L
    private var lastAdMobFailTime = 0L

    // --- Mintegral State ---
    private val _mintegralAdView = MutableStateFlow<View?>(null)
    val mintegralAdView: StateFlow<View?> = _mintegralAdView.asStateFlow()
    private var currentMintegralBanner: MBBannerView? = null
    private val isMintegralLoading = AtomicBoolean(false)
    private var lastMintegralLoadTime = 0L
    private var lastMintegralFailTime = 0L
    private var wasAdMobEnabled = false
    private var wasMintegralEnabled = false

    private var loadJob: Job? = null
    private val mutex = Mutex()

    fun startAdLoops(context: Context) {
        val appContext = context.applicationContext
        serviceScope.launch {
            mutex.withLock {
                if (loadJob?.isActive == true) return@launch
                loadJob = serviceScope.launch(Dispatchers.IO) {
                    while (isActive) {
                        // Check for config changes before loading
                        checkConfigChanges()

                        checkAndLoadAdMob(appContext)
                        delay(2000) // Stagger loads
                        checkAndLoadMintegral(appContext)
                        delay(30 * 1000L) // Check every 30s
                    }
                }
            }
        }
    }

    private fun checkConfigChanges() {
        val isAdMobEnabled = DataBaseCopyOperationsKt.getRemoteConfigAdmob() == 1
        val isMintegralEnabled = DataBaseCopyOperationsKt.getRemoteConfigMintegral() == 1

        // If AdMob just got enabled, reset its timers to load NOW
        if (isAdMobEnabled && !wasAdMobEnabled) {
            lastAdMobLoadTime = 0L
            lastAdMobFailTime = 0L
            _admobAdView.value = null // Clear old view if any
        }
        if (isMintegralEnabled && !wasMintegralEnabled) {
            lastMintegralLoadTime = 0L
            lastMintegralFailTime = 0L
            _mintegralAdView.value = null // Clear old view if any
        }

        wasAdMobEnabled = isAdMobEnabled
        wasMintegralEnabled = isMintegralEnabled
    }

    private suspend fun checkAndLoadAdMob(context: Context) {
        if (isAdMobLoading.get()) return

        // Perform all checks on IO thread
        val isAdMobEnabled = DataBaseCopyOperationsKt.getRemoteConfigAdmob() == 1
        val isVisibile = DataBaseCopyOperationsKt.getRemoteConfigVisibility() == 1
        val noIAP = DataBaseCopyOperationsKt.getInAppPurchases() == 0
        val hasNetwork = NetworkCheck.isNetworkAvailable(context)

        if (!isAdMobEnabled || !isVisibile || !noIAP || !hasNetwork) {
            _admobAdView.value = null
            return
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceLastLoad = currentTime - lastAdMobLoadTime
        val timeSinceLastFail = currentTime - lastAdMobFailTime

        // 30m success refresh, 45m failure retry
        val shouldLoad = (lastAdMobLoadTime == 0L && lastAdMobFailTime == 0L) ||
                (lastAdMobLoadTime > 0 && timeSinceLastLoad >= 30 * 60 * 1000L) ||
                (lastAdMobFailTime > 0 && timeSinceLastFail >= 45 * 60 * 1000L)

        if (shouldLoad) {
            if (isAdMobLoading.compareAndSet(false, true)) {
                withContext(Dispatchers.Main) { loadAdMobMainThread(context) }
            }
        }
    }

    private fun loadAdMobMainThread(context: Context) {
        try {
            val defBanner = "ca-app-pub-3747520410546258/3314026333"
            val pref = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
            val adId = if (!BuildConfig.DEBUG) pref?.getString(AD_ID_NATIVE_KEYPAD, defBanner) ?: defBanner else context.getString(R.string.admob_native_home)

            CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobRequestNewAd")

            AdLoader.Builder(context, adId)
                .forNativeAd { nativeAd ->
                    currentNativeAd?.destroy()
                    currentNativeAd = nativeAd
                    lastAdMobLoadTime = System.currentTimeMillis()
                    lastAdMobFailTime = 0L
                    isAdMobLoading.set(false)

                    // Inflate view on Main thread
                    val adContainer = LayoutInflater.from(context).inflate(R.layout.card_view_ad_keypad_admob, null) as CardView
                    val adView = adContainer.findViewById<NativeAdView>(R.id.nativead)
                    val shimmer = adContainer.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
                    populateNativeAdmob(nativeAd, shimmer, adView)

                    _admobAdView.value = adContainer
                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobOnAdLoaded")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(e: LoadAdError) {
                        Log.e(logTag, "AdMob Failed: $e")
                        lastAdMobFailTime = System.currentTimeMillis()
                        isAdMobLoading.set(false)
                        _admobAdView.value = null
                        CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobOnAdFailedToLoad")
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
                .build()
                .loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e(logTag, "AdMob init failed", e)
            isAdMobLoading.set(false)
        }
    }

    private fun populateNativeAdmob(nativeAd: NativeAd, shimmer: ShimmerFrameLayout, adView: NativeAdView) {
        shimmer.stopShimmer()
        shimmer.isVisible = false
        adView.isVisible = true

        adView.headlineView = adView.findViewById(R.id.adHeadline)
        adView.bodyView = adView.findViewById(R.id.adBody)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.adAppIcon)

        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)

        adView.findViewById<View>(R.id.adIconCard).isVisible = nativeAd.icon != null
        adView.setNativeAd(nativeAd)
    }

    private suspend fun checkAndLoadMintegral(context: Context) {
        if (isMintegralLoading.get()) return

        val isMintegralEnabled = DataBaseCopyOperationsKt.getRemoteConfigMintegral() == 1
        val isVisibile = DataBaseCopyOperationsKt.getRemoteConfigVisibility() == 1
        val noIAP = DataBaseCopyOperationsKt.getInAppPurchases() == 0
        val hasNetwork = NetworkCheck.isNetworkAvailable(context)

        if (!isMintegralEnabled || !isVisibile || !noIAP || !hasNetwork) {
            _mintegralAdView.value = null
            return
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceLastLoad = currentTime - lastMintegralLoadTime
        val timeSinceLastFail = currentTime - lastMintegralFailTime

        // 15m success refresh, 25m failure retry
        val shouldLoad = (lastMintegralLoadTime == 0L && lastMintegralFailTime == 0L) ||
                (lastMintegralLoadTime > 0 && timeSinceLastLoad >= 15 * 60 * 1000L) ||
                (lastMintegralFailTime > 0 && timeSinceLastFail >= 25 * 60 * 1000L)

        if (shouldLoad) {
            if (isMintegralLoading.compareAndSet(false, true)) {
                withContext(Dispatchers.Main) { loadMintegralMainThread(context) }
            }
        }
    }

    private fun loadMintegralMainThread(context: Context) {
        try {
            currentMintegralBanner?.release()
            currentMintegralBanner = null

            // Inflate on Main thread
            val adContainer = LayoutInflater.from(context).inflate(R.layout.card_view_ad_keypad_mintegral, null) as CardView
            val mbBannerView = adContainer.findViewById<MBBannerView>(R.id.mbBannerView)

            mbBannerView.init(BannerSize(BannerSize.DEV_SET_TYPE, 320, 50),
                context.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[0],
                context.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[1])

            mbBannerView.setRefreshTime(0)

            mbBannerView.setBannerAdListener(object : BannerAdListener {
                override fun onLoadSuccessed(ids: MBridgeIds?) {
                    currentMintegralBanner = mbBannerView
                    lastMintegralLoadTime = System.currentTimeMillis()
                    lastMintegralFailTime = 0L
                    isMintegralLoading.set(false)

                    (mbBannerView.parent as? ViewGroup)?.removeView(mbBannerView)
                    adContainer.removeAllViews()
                    adContainer.addView(mbBannerView)

                    _mintegralAdView.value = adContainer
                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "MintegralOnLoadSuccess")
                }

                override fun onLoadFailed(ids: MBridgeIds?, msg: String?) {
                    Log.e(logTag, "Mintegral Failed: $msg")
                    lastMintegralFailTime = System.currentTimeMillis()
                    isMintegralLoading.set(false)
                    _mintegralAdView.value = null
                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "MintegralOnAdFailedToLoad")
                }

                override fun onLogImpression(p0: MBridgeIds?) {}
                override fun onClick(p0: MBridgeIds?) {}
                override fun onLeaveApp(p0: MBridgeIds?) {}
                override fun showFullScreen(p0: MBridgeIds?) {}
                override fun closeFullScreen(p0: MBridgeIds?) {}
                override fun onCloseBanner(p0: MBridgeIds?) {}
            })

            mbBannerView.load()
        } catch (e: Exception) {
            Log.e(logTag, "Mintegral init failed", e)
            isMintegralLoading.set(false)
        }
    }

    fun destroy() {
        loadJob?.cancel()
        currentNativeAd?.destroy()
        currentMintegralBanner?.release()
    }
}




//class KeyboardAdManager(
//    private val serviceScope: CoroutineScope
//) {
//    private val logTag = "KeyboardAdManager"
//
//    // --- AdMob State ---
//    private val _admobAdView = MutableStateFlow<View?>(null)
//    val admobAdView: StateFlow<View?> = _admobAdView.asStateFlow()
//    private var currentNativeAd: NativeAd? = null
//    private val isAdMobLoading = AtomicBoolean(false)
//    private var lastAdMobLoadTime = 0L
//    private var lastAdMobFailTime = 0L
//
//    // --- Mintegral State ---
//    private val _mintegralAdView = MutableStateFlow<View?>(null)
//    val mintegralAdView: StateFlow<View?> = _mintegralAdView.asStateFlow()
//    private var currentMintegralBanner: MBBannerView? = null
//    private val isMintegralLoading = AtomicBoolean(false)
//    private var lastMintegralLoadTime = 0L
//    private var lastMintegralFailTime = 0L
//
//    private var loadJob: Job? = null
//    private val mutex = Mutex()
//
//    fun startAdLoops(context: Context) {
//        val appContext = context.applicationContext
//        serviceScope.launch {
//            mutex.withLock {
//                if (loadJob?.isActive == true) return@launch
//                loadJob = serviceScope.launch(Dispatchers.IO) {
//                    while (isActive) {
//                        checkAndLoadAdMob(appContext)
//                        delay(2000) // Stagger loads
//                        checkAndLoadMintegral(appContext)
//                        delay(30 * 1000L) // Check every 30s
//                    }
//                }
//            }
//        }
//    }
//
//    private suspend fun checkAndLoadAdMob(context: Context) {
//        if (isAdMobLoading.get()) return
//
//        // Perform all checks on IO thread
//        val isAdMobEnabled = DataBaseCopyOperationsKt.getRemoteConfigAdmob() == 1
//        val isVisibile = DataBaseCopyOperationsKt.getRemoteConfigVisibility() == 1
//        val noIAP = DataBaseCopyOperationsKt.getInAppPurchases() == 0
//        val hasNetwork = NetworkCheck.isNetworkAvailable(context)
//
//        if (!isAdMobEnabled || !isVisibile || !noIAP || !hasNetwork) {
//            _admobAdView.value = null
//            return
//        }
//
//        val currentTime = System.currentTimeMillis()
//        val timeSinceLastLoad = currentTime - lastAdMobLoadTime
//        val timeSinceLastFail = currentTime - lastAdMobFailTime
//
//        // 30m success refresh, 45m failure retry
//        val shouldLoad = (lastAdMobLoadTime == 0L && lastAdMobFailTime == 0L) ||
//                (lastAdMobLoadTime > 0 && timeSinceLastLoad >= 30 * 60 * 1000L) ||
//                (lastAdMobFailTime > 0 && timeSinceLastFail >= 45 * 60 * 1000L)
//
//        if (shouldLoad) {
//            if (isAdMobLoading.compareAndSet(false, true)) {
//                withContext(Dispatchers.Main) { loadAdMobMainThread(context) }
//            }
//        }
//    }
//
//    private fun loadAdMobMainThread(context: Context) {
//        try {
//            val defBanner = "ca-app-pub-3747520410546258/3314026333"
//            val pref = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
//            val adId = if (!BuildConfig.DEBUG) pref?.getString(NATIVE_KEYPAD, defBanner) ?: defBanner else context.getString(R.string.admob_native_home)
//
//            CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobRequestNewAd")
//
//            AdLoader.Builder(context, adId)
//                .forNativeAd { nativeAd ->
//                    currentNativeAd?.destroy()
//                    currentNativeAd = nativeAd
//                    lastAdMobLoadTime = System.currentTimeMillis()
//                    lastAdMobFailTime = 0L
//                    isAdMobLoading.set(false)
//
//                    // Inflate view on Main thread
//                    val adContainer = LayoutInflater.from(context).inflate(R.layout.card_view_ad_keypad_admob, null) as CardView
//                    val adView = adContainer.findViewById<NativeAdView>(R.id.nativead)
//                    val shimmer = adContainer.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
//                    populateNativeAdmob(nativeAd, shimmer, adView)
//
//                    _admobAdView.value = adContainer
//                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobOnAdLoaded")
//                }
//                .withAdListener(object : AdListener() {
//                    override fun onAdFailedToLoad(e: LoadAdError) {
//                        Log.e(logTag, "AdMob Failed: $e")
//                        lastAdMobFailTime = System.currentTimeMillis()
//                        isAdMobLoading.set(false)
//                        _admobAdView.value = null
//                        CustomFirebaseEvents.nativeKeypadAdEvent(context, "AdmobOnAdFailedToLoad")
//                    }
//                })
//                .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
//                .build()
//                .loadAd(AdRequest.Builder().build())
//        } catch (e: Exception) {
//            Log.e(logTag, "AdMob init failed", e)
//            isAdMobLoading.set(false)
//        }
//    }
//
//    private fun populateNativeAdmob(nativeAd: NativeAd, shimmer: ShimmerFrameLayout, adView: NativeAdView) {
//        shimmer.stopShimmer()
//        shimmer.isVisible = false
//        adView.isVisible = true
//
//        adView.headlineView = adView.findViewById(R.id.adHeadline)
//        adView.bodyView = adView.findViewById(R.id.adBody)
//        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
//        adView.iconView = adView.findViewById(R.id.adAppIcon)
//
//        (adView.headlineView as TextView).text = nativeAd.headline
//        (adView.bodyView as TextView).text = nativeAd.body
//        (adView.callToActionView as Button).text = nativeAd.callToAction
//        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
//
//        adView.findViewById<View>(R.id.adIconCard).isVisible = nativeAd.icon != null
//        adView.setNativeAd(nativeAd)
//    }
//
//    private suspend fun checkAndLoadMintegral(context: Context) {
//        if (isMintegralLoading.get()) return
//
//        val isMintegralEnabled = DataBaseCopyOperationsKt.getRemoteConfigMintegral() == 1
//        val isVisibile = DataBaseCopyOperationsKt.getRemoteConfigVisibility() == 1
//        val noIAP = DataBaseCopyOperationsKt.getInAppPurchases() == 0
//        val hasNetwork = NetworkCheck.isNetworkAvailable(context)
//
//        if (!isMintegralEnabled || !isVisibile || !noIAP || !hasNetwork) {
//            _mintegralAdView.value = null
//            return
//        }
//
//        val currentTime = System.currentTimeMillis()
//        val timeSinceLastLoad = currentTime - lastMintegralLoadTime
//        val timeSinceLastFail = currentTime - lastMintegralFailTime
//
//        // 15m success refresh, 25m failure retry
//        val shouldLoad = (lastMintegralLoadTime == 0L && lastMintegralFailTime == 0L) ||
//                (lastMintegralLoadTime > 0 && timeSinceLastLoad >= 15 * 60 * 1000L) ||
//                (lastMintegralFailTime > 0 && timeSinceLastFail >= 25 * 60 * 1000L)
//
//        if (shouldLoad) {
//            if (isMintegralLoading.compareAndSet(false, true)) {
//                withContext(Dispatchers.Main) { loadMintegralMainThread(context) }
//            }
//        }
//    }
//
//    private fun loadMintegralMainThread(context: Context) {
//        try {
//            currentMintegralBanner?.release()
//            currentMintegralBanner = null
//
//            // Inflate on Main thread
//            val adContainer = LayoutInflater.from(context).inflate(R.layout.card_view_ad_keypad_mintegral, null) as CardView
//            val mbBannerView = adContainer.findViewById<MBBannerView>(R.id.mbBannerView)
//
//            mbBannerView.init(BannerSize(BannerSize.DEV_SET_TYPE, 320, 50),
//                context.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[0],
//                context.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[1])
//
//            mbBannerView.setRefreshTime(0)
//
//            mbBannerView.setBannerAdListener(object : BannerAdListener {
//                override fun onLoadSuccessed(ids: MBridgeIds?) {
//                    currentMintegralBanner = mbBannerView
//                    lastMintegralLoadTime = System.currentTimeMillis()
//                    lastMintegralFailTime = 0L
//                    isMintegralLoading.set(false)
//
//                    (mbBannerView.parent as? ViewGroup)?.removeView(mbBannerView)
//                    adContainer.removeAllViews()
//                    adContainer.addView(mbBannerView)
//
//                    _mintegralAdView.value = adContainer
//                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "MintegralOnLoadSuccess")
//                }
//
//                override fun onLoadFailed(ids: MBridgeIds?, msg: String?) {
//                    Log.e(logTag, "Mintegral Failed: $msg")
//                    lastMintegralFailTime = System.currentTimeMillis()
//                    isMintegralLoading.set(false)
//                    _mintegralAdView.value = null
//                    CustomFirebaseEvents.nativeKeypadAdEvent(context, "MintegralOnAdFailedToLoad")
//                }
//
//                override fun onLogImpression(p0: MBridgeIds?) {}
//                override fun onClick(p0: MBridgeIds?) {}
//                override fun onLeaveApp(p0: MBridgeIds?) {}
//                override fun showFullScreen(p0: MBridgeIds?) {}
//                override fun closeFullScreen(p0: MBridgeIds?) {}
//                override fun onCloseBanner(p0: MBridgeIds?) {}
//            })
//
//            mbBannerView.load()
//        } catch (e: Exception) {
//            Log.e(logTag, "Mintegral init failed", e)
//            isMintegralLoading.set(false)
//        }
//    }
//
//    fun destroy() {
//        loadJob?.cancel()
//        currentNativeAd?.destroy()
//        currentMintegralBanner?.release()
//    }
//}
