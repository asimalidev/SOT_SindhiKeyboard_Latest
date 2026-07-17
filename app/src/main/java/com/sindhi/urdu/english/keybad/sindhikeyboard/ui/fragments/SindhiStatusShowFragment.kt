package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSindhiStatusShowBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.SindhiPoetryShowAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetry
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetryItemClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_SINDHI_STATUS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_SINDHI_STATUS_SHOW
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import kotlin.collections.set

class SindhiStatusShowFragment : Fragment(), SindhiPoetryItemClickListener {
    lateinit var binding: FragmentSindhiStatusShowBinding
    var isPurchased: Boolean? = null
    var navController: NavController? = null
    private val args: SindhiStatusShowFragmentArgs by navArgs()
    var adapter: SindhiPoetryShowAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSindhiStatusShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navController = findNavController()
                    val currentDestId = navController.currentDestination?.id
                    if (currentDestId == R.id.navSindhiStatusShow) {
                        navController.navigate(SindhiStatusShowFragmentDirections.actionNavSindhiStatusShowToNavSindhiStatus())
                    } else {
                        navController.popBackStack()
                    }

                }
            })

        checkForLoadBanner()

        binding.rvPoetryShow.layoutManager = LinearLayoutManager(requireActivity())

        when (args.poetryName) {
            "استادبخاري" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getUstadBukhariList(), this)
                binding.rvPoetryShow.adapter = adapter
            }

            "رومينٽڪ شاعري" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getRomanticShayriList(), this)
                binding.rvPoetryShow.adapter = adapter
            }

            "شاھ لطيف جي شاعري ۽ سمجھاني" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getShahLatifShayriList(), this)
                binding.rvPoetryShow.adapter = adapter
            }

            "شيخ اياز" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getSheikhAyazShayriList(), this)
                binding.rvPoetryShow.adapter = adapter
            }

            "کل ۽پوگ" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getKulPogShayriList(), this)
                binding.rvPoetryShow.adapter = adapter
            }

            "لطيفيات" -> {
                adapter = SindhiPoetryShowAdapter(SindhiPoetry.getLatifiyatShayriList(), this)
                binding.rvPoetryShow.adapter = adapter
            }
        }
    }

    private fun checkForLoadBanner() {
        val isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        if (!isPurchase && isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                BANNER_SINDHI_STATUS_SHOW, "ON"
            ).equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeActivity")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeActivity"]
                Handler().postDelayed({
                    binding.shimmerLayoutBanner.stopShimmer()
                    binding.shimmerLayoutBanner.visibility = View.GONE
                    binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                    val parent = collapsibleAdView?.parent as? ViewGroup
                    parent?.removeView(collapsibleAdView)

                    binding.adViewContainer.addView(collapsibleAdView)
                }, 500)
            } else {
                loadBanner()
            }
        } else {
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }
    }

    /*private val adSize: AdSize
        get() = AdSize.BANNER*/
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
                requireContext(),
                adWidth
            )
        }

    private fun loadBanner() {
        Log.d("jdjasjjsa", "loading: ")
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(ADMOB_BANNER_SINDHI_STATUS, "ca-app-pub-3747520410546258/5122866133")
        } else {
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!//getString(R.string.BANNER_BIDDING)
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                NativeMaster.collapsibleBannerAdMobHashMap!!["baner_pet"] = adView
                Log.d("jdjasjjsa", "onAdLoaded: ")

            }

            override fun onAdFailedToLoad(error: LoadAdError) {


            }

            override fun onAdOpened() {
                Log.d("jdjasjjsa", "onAdOpened: ")

            }

            override fun onAdClicked() {

            }

            override fun onAdClosed() {

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = args.poetryName
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

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(Preferences.ADS_NATIVE_POETRY_INSIDE, "ON").equals("ON", true)
            ) {
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
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onFailed = {
                        binding.nativeAdContainerAd.visibility = View.GONE
                        binding.separator.visibility = View.GONE
                    },
                    onAddLoaded = {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        binding.separator.visibility = View.VISIBLE
                    })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
                binding.separator.visibility = View.GONE

            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onPoetryItemCopyClicked(poetry: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", poetry)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text Copied to ClipBoard", Toast.LENGTH_SHORT).show()
    }

    override fun onPoetryItemShareClicked(poetry: String) {
        val intent2 = Intent()
        intent2.action = Intent.ACTION_SEND
        intent2.type = "text/plain"
        intent2.putExtra(Intent.EXTRA_TEXT, poetry)
        requireContext().startActivity(Intent.createChooser(intent2, "Share via"))
    }

    override fun onPoetryItemWhatsappClicked(poetry: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, poetry)

        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            requireContext().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPoetryItemMessengerClicked(poetry: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.type = "text/plain"
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, poetry)
        sendIntent.setPackage("com.facebook.orca")
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(Intent.createChooser(sendIntent, "Share text via..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please Install Facebook Messenger", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPoetryItemTwitterClicked(poetry: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, poetry)
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            requireContext().startActivity(Intent.createChooser(intent, "Share text via..."))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "You don't seem to have twitter installed on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}