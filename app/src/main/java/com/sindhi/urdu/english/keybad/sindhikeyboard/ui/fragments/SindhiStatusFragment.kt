package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSindhiStatusBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.LabelsAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.LabelNamesIcons
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_POETRY_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SINDHI_POETRY_CATEGORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_POETRY_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate

class SindhiStatusFragment : Fragment() {
    lateinit var binding: FragmentSindhiStatusBinding
    private lateinit var adapter: LabelsAdapter
    var navController: NavController? = null
    lateinit var labelNamesArrayList: ArrayList<LabelNamesIcons>
    private var isPurchase = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSindhiStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        checkForLoadBanner()
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        labelNamesArrayList = ArrayList()
        fillUpPoetList()

        binding.rvLabels.layoutManager = LinearLayoutManager(requireActivity())
        adapter = LabelsAdapter(labelNamesArrayList) { name ->
            Log.e("SindhiStatusFragment", "labelNamesArrayList: $name")
            if (navController != null) {
                if (isNetworkAvailable(context) && !(PreferenceManager.getDefaultSharedPreferences(
                        requireActivity()
                    ).getBoolean(PURCHASE, false))
                    && requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(SHOW_INTER_SINDHI_POETRY_CATEGORY, "ON")
                        .equals("ON", true)
                ) {
                    InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                        context = context,
                        "SindhiStatusFragment",
                        onAdClosedCallBackAdmob = {
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (navController != null) {
                                    if (isAdded) {
                                        navController?.safeNavigate(
                                            SindhiStatusFragmentDirections.actionNavSindhiStatusToNavSindhiStatusShow(
                                                name
                                            )
                                        )
                                    }
                                } else {
                                    isNavControllerAdded()
                                }
                            }, 300)
                        },
                        onAdShowedCallBackAdmob = { })
                } else {
                    if (isAdded) {
                        navController?.safeNavigate(
                            SindhiStatusFragmentDirections.actionNavSindhiStatusToNavSindhiStatusShow(
                                name
                            )
                        )
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }

        binding.rvLabels.adapter = adapter

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navController != null) {
                        navController?.safeNavigate(SindhiStatusFragmentDirections.actionNavSindhiStatusToNavHome())
                    } else {
                        isNavControllerAdded()
                    }
                }
            })
    }

    private fun checkForLoadBanner() {
        if (isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                "RemoteConfig",
                MODE_PRIVATE
            ).getString(
                SHOW_BANNER_SINDHI_POETRY_CATEGORY_LIST, "ON"
            ).equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("sindiStatus")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["sindiStatus"]
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
            pref.getString(AD_ID_BANNER_SINDHI_POETRY, resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                NativeMaster.collapsibleBannerAdMobHashMap!!["sindiStatus"] = adView
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
            txtSindhiKeyboard.text = resources.getString(R.string.label_sindhi_status)
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

        if (isPurchase) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(
                        SHOW_NATIVE_SINDHI_POETRY_CATEGORY_LIST, "ON"
                    ).equals("ON", true)
            ) {

                val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(AD_ID_NATIVE_SINDHI_POETRY, resources.getString(R.string.NATIVE_DEFAULT))
                } else {
                    resources.getString(R.string.NATIVE_DEFAULT)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "sindiStatus",
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
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun fillUpPoetList() {
        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_1
                ), "استادبخاري"
            )
        )
        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_2
                ), "رومينٽڪ شاعري"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_13
                ), "شاھ لطيف جي شاعري ۽ سمجھاني"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_4
                ), "شيخ اياز"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_12
                ), "بن لائنن جي شاعري"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_10
                ), "اداس شاعري"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_9
                ), "عڀدالطيف جي شاعري"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_8
                ), "غزل"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_11
                ), "لوڪل شاعري"
            )
        )

        labelNamesArrayList.add(
            LabelNamesIcons(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_7
                ), "کل ۽پوگ"
            )
        )
    }
}