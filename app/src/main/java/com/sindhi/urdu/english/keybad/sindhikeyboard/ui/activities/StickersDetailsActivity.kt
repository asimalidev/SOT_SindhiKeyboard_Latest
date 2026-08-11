package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
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
import com.sindhi.urdu.english.keybad.databinding.ActivityStickersDetailsBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.RewardedClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.downloadInProgressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.progressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPack
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPackData
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.StickersDetailsAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_STICKERS_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_STICKERS_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_STICKERS_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_STICKERS_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_REWARDED_SINDHI_STICKERS

class StickersDetailsActivity : AppCompatBaseActivity() {
    private lateinit var binding: ActivityStickersDetailsBinding
    val stickerViewModel: StickerViewModel by lazy { StickerViewModel() }
    var selectedIndexNumber = 0
    private var loadingAnimator: ValueAnimator? = null
    private var isAnimating = false
    private var isPurchase = false
    private var isPremium = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickersDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isPurchase =
            getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(
                Preferences.IS_PURCHASED,
                false
            )

        binding.txtPackName.isSelected = true
        isPremium = intent.getBooleanExtra("isPremium", false)
        applyButtonState()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        supportActionBar?.hide()
        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 1. Use PADDING for the top so the background color extends behind the time bar
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
        binding.shimmerLayoutStickerDetails.startShimmer()
        selectedIndexNumber = intent.getIntExtra("selectedIndexNumber", 0)

        if (!NetworkCheck.isNetworkAvailable(this)) {
            Toast.makeText(this, "No Internet!", Toast.LENGTH_LONG).show()
            noInternet()
        }

        fetchData()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.clStickerShow.visibility == View.GONE) {
                    startActivity(
                        Intent(this@StickersDetailsActivity, StickersViewActivity::class.java)
                            .putExtra("From", intent.getStringExtra("From"))
                            .putExtra("MoveTo", intent.getStringExtra("MoveTo"))
                    )
                    finish()
                } else {
                    binding.clStickerShow.visibility = View.GONE
                }
            }
        })

        if (StickerDataCache.stickerPackData != null) {
            binding.txtPackName.text =
                StickerDataCache.stickerPackData!!.sticker_packs[selectedIndexNumber].name
            binding.txtSize.text =
                "Size: ${StickerDataCache.stickerPackData!!.sticker_packs[selectedIndexNumber].stickers_list_size}"

            updateProgressViews()
        }

        binding.txtAddSticker.blockingClickListener {
            val stickerPackData = StickerDataCache.stickerPackData
            if (stickerPackData != null) {
                val currentPack = stickerPackData.sticker_packs[selectedIndexNumber]

                if (isPremium && !isPurchase) {
                    showPremiumUnlockDialog(currentPack, selectedIndexNumber)
                } else {
                    downloadInProgressMap[currentPack.name] = true
                    startLoadingAnimation()
                    stickerViewModel.downloadStickerPack(currentPack, this)
                }
            }
        }

        stickerViewModel.downloadProgress.observe(this) { (packName, progress) ->
            Log.i("DownloadWorkDetails", "$packName : $progress")
            progressMap[packName] = progress

            runOnUiThread {
                binding.txtAddSticker.text = progress

                if (progress.equals("Download Complete", true) || progress.equals(
                        "Completed",
                        true
                    )
                ) {
                    stopLoadingAnimation()
                }
                updateProgressViews()
            }
        }

        binding.ivBackPress.blockingClickListener {
            onBackPressed()
        }

        binding.clStickerShow.blockingClickListener {
            if (binding.clStickerShow.visibility == View.VISIBLE) {
                binding.clStickerShow.visibility = View.GONE
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars()) // changed to show()
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    // Helper method to set the correct colors and animation visibility
    private fun applyButtonState() {
        if (isPremium && !isPurchase) {
            binding.txtAddSticker.setBackgroundResource(R.drawable.bg_btn_premium)
            binding.lottieAnimation.visibility = View.VISIBLE
        } else {
            binding.txtAddSticker.setBackgroundResource(R.drawable.bg_btn_blue_gradient)
            binding.lottieAnimation.visibility = View.GONE
        }
    }

    private fun startLoadingAnimation() {
        if (isAnimating) return

        isAnimating = true
        binding.txtAddSticker.text = "Downloading..."
        binding.txtAddSticker.isEnabled = false

        val drawable = binding.txtAddSticker.background.mutate() as GradientDrawable

        // Define your new colors here
        val startColor = 0xFFFF3B5E.toInt()
        val endColor = 0xFFD20024.toInt()

        loadingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener {
                val value = animatedFraction

                // Swap between the two colors to create the loading effect
                val colors = if (value < 0.5f) {
                    intArrayOf(startColor, endColor, startColor)
                } else {
                    intArrayOf(endColor, startColor, endColor)
                }

                drawable.colors = colors
                binding.txtAddSticker.invalidate()
            }
            start()
        }
    }

    private fun stopLoadingAnimation() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        isAnimating = false

        // 1. Force clear the stuck mutated gradient
        binding.txtAddSticker.background = null

        // 2. Apply the fresh, solid blue XML background
        binding.txtAddSticker.setBackgroundResource(R.drawable.bg_btn_blue_gradient)

        binding.lottieAnimation.cancelAnimation()
        binding.lottieAnimation.visibility = View.GONE

        binding.txtAddSticker.text = "Added"
        binding.txtAddSticker.isEnabled = false
    }

    private fun resetAddButton() {
        loadingAnimator?.cancel()
        loadingAnimator = null
        isAnimating = false

        // 1. Force clear the stuck mutated gradient
        binding.txtAddSticker.background = null

        // 2. Re-apply the correct state
        applyButtonState()

        binding.txtAddSticker.text = "Add"
        binding.txtAddSticker.isEnabled = true
    }


    private fun showPremiumUnlockDialog(stickerPack: StickerPack, position: Int) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_premium_unlock)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.findViewById<ImageView>(R.id.ivCloseDialog)?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnWatchAd)?.setOnClickListener {

            val isNetworkAvailable = isNetworkAvailable(this@StickersDetailsActivity)
            val isPurchased = getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            val isAdEnabled = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_REWARDED_SINDHI_STICKERS, "ON").equals("ON", true)

            if (isNetworkAvailable && !isPurchased && isAdEnabled) {
                RewardedClassAdMob.loadAndShowRewardedAd(
                    context = this,
                    onUserEarnedReward = {
                        downloadInProgressMap[stickerPack.name] = true
                        startLoadingAnimation()
                        stickerViewModel.downloadStickerPack(stickerPack, this)
                    }, onAdDismissed = { })
            }
            dialog.dismiss()
        }
        dialog.show()
    }


    override fun onResume() {
        super.onResume()

        if (NetworkCheck.isNetworkAvailable(this)
            && !getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                SHOW_NATIVE_SINDHI_STICKERS_DETAILS,
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
                SHOW_BANNER_SINDHI_STICKERS_DETAILS,
                "ON"
            ).equals("ON", true)
        ) {
            binding.shimmerLayoutBanner.startShimmer()
            binding.shimmerLayoutBanner.visibility = View.VISIBLE
            binding.adViewContainer.visibility = View.VISIBLE
            binding.Separator.visibility = View.VISIBLE
            loadAdmobCollapsibleBannerAd()
        } else {
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
            binding.Separator.visibility = View.GONE
        }
    }

    private fun loadAdmobNativeAd() {
        val pref = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(
                AD_ID_NATIVE_STICKERS_SETTINGS,
                resources.getString(R.string.NATIVE_DEFAULT)
            )
        } else {
            resources.getString(R.string.NATIVE_DEFAULT)
        }

        NewNativeAdClass.checkAdRequestAdmob(
            mContext = this,
            fragmentName = "StickersDetailsActivity",
            adId = adId!!,
            isMedia = false,
            isMediaOnLeft = false,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = false,
            onFailed = {
                binding.nativeAdContainerAd.visibility = View.GONE
            },
            onAddLoaded = {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            })
    }

    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("StickersDetailsActivity")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["StickersDetailsActivity"]
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
        val pref = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(
                AD_ID_BANNER_STICKERS_SETTINGS,
                resources.getString(R.string.BANNER_DEFAULT)
            )
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }
        val adView = AdView(this)
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!
        val extras = Bundle()

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                        OVERALL_BANNER_RELOADING,
                        "SAVE"
                    ).equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["StickersDetailsActivity"] = adView
                }

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.shimmerLayoutBanner.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}
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


    private fun updateProgressViews() {
        val packName = StickerDataCache.stickerPackData!!.sticker_packs[selectedIndexNumber].name

        // Use an empty string as default instead of "Starting download" so it doesn't look weird
        val progress = progressMap[packName] ?: ""
        val isDownloadInProgress = downloadInProgressMap[packName] ?: false

        // 1. CHECK IF ALREADY COMPLETED FIRST!
        // If the map says it's done, force it to the blue "Added" state immediately.
        if (progress.equals("Download Complete", true) || progress.equals("Completed", true)) {
            stopLoadingAnimation()
            downloadInProgressMap[packName] = false
            return
        }

        // 2. If it is NOT downloading and NOT completed, set it to the default Add/Premium state
        if (!isDownloadInProgress) {
            if (!binding.txtAddSticker.text.toString().equals("Added", ignoreCase = true)) {
                resetAddButton()
            }
            return
        }

        // 3. If it IS currently downloading, show the animation and update the text
        if (!isAnimating) {
            startLoadingAnimation()
        }
        binding.txtAddSticker.text = progress
    }

    private fun fetchData() {
        if (StickerDataCache.stickerPackData == null) {
            if (NetworkCheck.isNetworkAvailable(this)) {
                stickerViewModel.fetchStickers()
            }
        } else {
            StickerDataCache.stickerPackData.let {
                Glide.with(this)
                    .load(StickerDataCache.stickerPackData!!.sticker_packs[selectedIndexNumber].tray_image_file)
                    .placeholder(R.drawable.ic_no_stickers)
                    .into(binding.ivStickerTrayImage)
            }
            populateAdapter(StickerDataCache.stickerPackData!!)
        }

        stickerViewModel.stickerPackData.observe(this) { stickerPackData ->
            if (stickerPackData != null) {
                populateAdapter(stickerPackData)
            }
        }
    }

    private fun populateAdapter(stickerPackData: StickerPackData) {
        val selectedPackDetails = stickerPackData.sticker_details[selectedIndexNumber]
        val stickers = selectedPackDetails.stickers.map { it.image_file_url }

        val adapter = StickersDetailsAdapter(stickers) { stickerUrl ->
            Glide.with(binding.root.context)
                .load(stickerUrl)
                .placeholder(R.drawable.ic_no_stickers)
                .into(binding.ivStickerShow)

            binding.clStickerShow.visibility = View.VISIBLE
        }
        binding.rvStickersDetails.adapter = adapter
        binding.rvStickersDetails.layoutManager = GridLayoutManager(this, 4)
        noInternet()
    }

    private fun noInternet() {
        binding.shimmerLayoutStickerDetails.stopShimmer()
        binding.shimmerLayoutStickerDetails.visibility = View.GONE
    }
}

