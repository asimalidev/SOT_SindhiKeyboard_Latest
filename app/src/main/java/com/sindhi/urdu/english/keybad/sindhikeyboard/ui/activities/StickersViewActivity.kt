package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_STICKER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_STICKERS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING


class StickersViewActivity : AppCompatBaseActivity() {
    private lateinit var binding: ActivityStickersViewBinding
    val stickerViewModel: StickerViewModel by lazy { StickerViewModel() }
    lateinit var stickersPackAdapter: StickersPackAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickersViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setStatusBarColor(this@StickersViewActivity,resources.getColor(R.color.maroon_500))


        binding.shimmerLayoutStickers.startShimmer()
        setupRecyclerView()
        fetchData()
        CustomFirebaseEvents.activitiesFragmentEvent(this, "urdu_stickers_categ_list_scr")

        if (!NetworkCheck.isNetworkAvailable(this)) {
            noInternet()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (intent.getStringExtra("MoveTo").equals("sindhi_stickers")) {
                    val intent = Intent(this@StickersViewActivity, NavigationActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        })

        binding.ivBackPress.blockingClickListener {
            if (intent.getStringExtra("MoveTo").equals("sindhi_stickers")) {
                val intent = Intent(this@StickersViewActivity, NavigationActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        stickerViewModel.downloadProgress.observe(this) { (packName, progress) ->
            Log.i("DownloadWork", "$packName: $progress")
            stickersPackAdapter.updateProgress(packName, progress)
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window

        // 1. Make the Navigation Bar Transparent
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = color

        // 2. Disable the system "contrast scrim" (the semi-transparent overlay) on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // 3. Handle Edge-to-Edge and Padding
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                val navBarInsets = insets.getInsets(WindowInsets.Type.navigationBars())

                view.setBackgroundColor(color)

                // We KEEP this padding to prevent your views from merging with the bottom bar
                view.setPadding(0, statusBarInsets.top, 0, navBarInsets.bottom)

                insets
            }
        } else {
            // Android 14 and below
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }


    override fun onResume() {
        super.onResume()
        if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED,false)
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(NATIVE_STICKERS,"ON").equals("ON",true)) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(
                Preferences.IS_PURCHASED,false)
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(BANNER_STICKER,"ON").equals("ON",true)) {
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
            pref.getString(NATIVE_OVER_ALL,"ca-app-pub-3747520410546258/1702944653")
        }
        else{
            resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
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
            val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["StickersViewActivity"]
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.adViewContainer.removeView(binding.shimmerLayoutBanner)
            binding.separator.visibility=View.VISIBLE

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
            pref.getString(BANNER_INSIDE,"ca-app-pub-3747520410546258/1697692330")
        }
        else{
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        adView.adUnitId = adId!!//getString(R.string.admob_banner_inside)
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
                    CustomFirebaseEvents.activitiesFragmentEvent(this, "detail_sticker_scr")
                    startActivity(Intent(this, StickersDetailsActivity::class.java)
                        .putExtra("From", intent.getStringExtra("From"))
                        .putExtra("MoveTo", intent.getStringExtra("MoveTo"))
                        .putExtra("selectedIndexNumber", position))
                    finish()
                } else {
                    Toast.makeText(this,"No Internet! Please make sure internet is on", Toast.LENGTH_LONG).show()
                }
            }
        )

        binding.rvStickers.apply {
            layoutManager = GridLayoutManager(this@StickersViewActivity, 2)
            adapter = stickersPackAdapter
        }
    }

    private fun fetchData() {
        if (NetworkCheck.isNetworkAvailable(this)) {
            stickerViewModel.fetchStickers()
        }

        stickerViewModel.stickerPackData.observe(this) { stickerPackData ->
            if (stickerPackData != null) {
                Log.e("DownloadWork", "stickerPackData != null")
                populateAdapter(stickerPackData)
            } else {
                Log.e("DownloadWork", "stickerPackData == null")
                noInternet()
            }
        }
    }

    private fun populateAdapter(stickerPackData: StickerPackData) {
        // stop shimmer once data is loaded
        binding.shimmerLayoutStickers.stopShimmer()
        binding.shimmerLayoutStickers.visibility = View.GONE
        stickersPackAdapter.updateData(stickerPackData)
    }

    private fun noInternet() {
        // Always stop shimmer
        binding.shimmerLayoutStickers.stopShimmer()
        binding.shimmerLayoutStickers.visibility = View.GONE

        // If adapter has no data, show no internet screen
        if (stickersPackAdapter.itemCount == 0) {
            binding.noInternet.visibility = View.VISIBLE
            binding.rvStickers.visibility = View.GONE
        } else {
            // Data already loaded → keep showing RecyclerView
            binding.noInternet.visibility = View.GONE
            binding.rvStickers.visibility = View.VISIBLE
        }
    }
}