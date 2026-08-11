package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.activities.AppCompatBaseActivity
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityStickersViewBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPackData
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.StickersPackAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_STICKERS_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_STICKERS_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_STICKERS_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_STICKERS_CATEGORY_LIST

class StickersViewActivity : AppCompatBaseActivity() {
    private lateinit var binding: ActivityStickersViewBinding
    val stickerViewModel: StickerViewModel by lazy { StickerViewModel() }
    lateinit var stickersPackAdapter: StickersPackAdapter

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isDataFetching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickersViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(0, insets.top, 0, 0)

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = 0
            }
            WindowInsetsCompat.CONSUMED
        }
        hideSystemUI()

        setupRecyclerView()

        // 1. Setup Observers ONCE here instead of every time we fetch data
        setupObservers()

        setupNetworkObserver()

        CustomFirebaseEvents.activitiesFragmentEvent(this, "urdu_stickers_categ_list_scr")

        // Initial check for data/internet
        if (NetworkCheck.isNetworkAvailable(this)) {
            fetchData()
        } else {
            noInternet()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@StickersViewActivity, NavigationActivity::class.java)
                startActivity(intent)
            }
        })

        binding.ivBackPress.blockingClickListener {
                val intent = Intent(this@StickersViewActivity, NavigationActivity::class.java)
                startActivity(intent)
        }
    }

    // ==========================================
    // ADDED: Centralized Observers (Prevents multiple triggers)
    // ==========================================
    private fun setupObservers() {
        // Observe Sticker Data
        stickerViewModel.stickerPackData.observe(this) { stickerPackData ->
            isDataFetching = false
            if (stickerPackData != null) {
                Log.e("DownloadWork", "stickerPackData != null")
                populateAdapter(stickerPackData)
            } else {
                Log.e("DownloadWork", "stickerPackData == null")
                noInternet()
            }
        }

        // Observe Download Progress
        stickerViewModel.downloadProgress.observe(this) { (packName, progress) ->
            Log.i("DownloadWork", "$packName: $progress")
            stickersPackAdapter.updateProgress(packName, progress)
        }
    }

    private fun setupNetworkObserver() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    if (stickersPackAdapter.itemCount == 0 && !isDataFetching) {
                        // Internet is back! FetchData will handle hiding the error and showing the shimmer.
                        fetchData()
                    } else if (stickersPackAdapter.itemCount > 0) {
                        // Internet is back and we already have data
                        binding.noInternet.visibility = View.GONE
                        binding.rvStickers.visibility = View.VISIBLE
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                runOnUiThread {
                    if (stickersPackAdapter.itemCount == 0) {
                        noInternet()
                    }
                }
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::connectivityManager.isInitialized) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onResume() {
        super.onResume()

        if (::stickersPackAdapter.isInitialized) {
            stickersPackAdapter.notifyDataSetChanged()
        }
        if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                SHOW_NATIVE_SINDHI_STICKERS_CATEGORY_LIST,
                "ON"
            ).equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                SHOW_BANNER_SINDHI_STICKERS_CATEGORY_LIST,
                "ON"
            ).equals("ON", true)
        ) {
            binding.shimmerLayoutBanner.startShimmer()
            binding.shimmerLayoutBanner.visibility = View.VISIBLE
            binding.adViewContainer.visibility = View.VISIBLE
            loadAdmobCollapsibleBannerAd()
        } else {
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
        }
    }

    private fun loadAdmobNativeAd() {
        val pref =getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_NATIVE_STICKERS_SETTINGS,resources.getString(R.string.NATIVE_DEFAULT))
        }
        else{
            resources.getString(R.string.NATIVE_DEFAULT)
        }
        NewNativeAdClass.checkAdRequestAdmob(
            mContext = this,
            fragmentName = "StickersViewActivity",
            adId = adId!!,//this.resources.getString(R.string.admob_native),
            isMedia = false,
            isMediaOnLeft = false,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = false,
            onFailed = {
                binding.nativeAdContainerAd.visibility = View.GONE
                binding.separator.visibility=View.GONE
            },
            onAddLoaded = {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.separator.visibility=View.VISIBLE
            })
    }

    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("StickersViewActivity")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["StickersViewActivity"]
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

            val parent = collapsibleAdView?.parent as? ViewGroup
            parent?.removeView(collapsibleAdView)

            binding.adViewContainer.addView(collapsibleAdView)
        } else {
            loadCollapsibleBanner()
        }
    }

        private fun loadCollapsibleBanner() {
        val adView = AdView(this)
        adView.setAdSize(adSize)
        val pref =getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_BANNER_STICKERS_SETTINGS,resources.getString(R.string.BANNER_DEFAULT))
        }
        else{
            resources.getString(R.string.BANNER_DEFAULT)
        }
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
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["StickersViewActivity"] = adView
                }
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.separator.visibility=View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.separator.visibility=View.GONE
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
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    private fun setupRecyclerView() {
        stickersPackAdapter = StickersPackAdapter(
            context = this,
            stickerPackData = StickerDataCache.stickerPackData,
            onStickerDetailsClick = { position ->
                if (NetworkCheck.isNetworkAvailable(this)) {
                    val clickedPack = StickerDataCache.stickerPackData?.sticker_packs?.get(position)
                    val isPremium = clickedPack?.premium.equals("yes", ignoreCase = true)

                    CustomFirebaseEvents.activitiesFragmentEvent(this, "detail_sticker_scr")

                    startActivity(
                        Intent(this, StickersDetailsActivity::class.java)
                            .putExtra("From", intent.getStringExtra("From"))
                            .putExtra("MoveTo", intent.getStringExtra("MoveTo"))
                            .putExtra("selectedIndexNumber", position)
                            .putExtra("isPremium", isPremium)
                    )
                    finish()
                } else {
                    Toast.makeText(this, "No Internet!", Toast.LENGTH_LONG).show()
                }
            }
        )
        binding.rvStickers.apply {
            layoutManager = GridLayoutManager(this@StickersViewActivity, 2)
            adapter = stickersPackAdapter
        }
    }

    private fun fetchData() {
        // 1. Start checking local storage
        stickerViewModel.loadStickerPacks(this)

        if (NetworkCheck.isNetworkAvailable(this)) {
            stickerViewModel.fetchStickers()
        }

        // Observe API Data
        stickerViewModel.stickerPackData.observe(this) { stickerPackData ->
            if (stickerPackData != null) {
                populateAdapter(stickerPackData)
            } else {
                noInternet()
            }
        }

        // ✅ ADD THIS: When local storage check finishes, refresh the UI!
        stickerViewModel.stickerPackList.observe(this) {
            if (::stickersPackAdapter.isInitialized) {
                stickersPackAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun populateAdapter(stickerPackData: StickerPackData) {
        binding.shimmerLayoutStickers.stopShimmer()
        binding.shimmerLayoutStickers.visibility = View.GONE
        binding.noInternet.visibility = View.GONE
        binding.rvStickers.visibility = View.VISIBLE
        stickersPackAdapter.updateData(stickerPackData)
    }

    private fun noInternet() {
        // 3. Reset the boolean here so the app knows we are no longer loading!
        isDataFetching = false

        binding.shimmerLayoutStickers.stopShimmer()
        binding.shimmerLayoutStickers.visibility = View.GONE

        if (stickersPackAdapter.itemCount == 0) {
            binding.noInternet.visibility = View.VISIBLE
            binding.rvStickers.visibility = View.GONE
        } else {
            binding.noInternet.visibility = View.GONE
            binding.rvStickers.visibility = View.VISIBLE
        }
    }
}