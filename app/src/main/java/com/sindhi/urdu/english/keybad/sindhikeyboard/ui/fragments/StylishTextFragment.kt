package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.utils.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentStylishTextBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.StylishTextAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_REVERSE_FANCYFONTS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_REVERSE_FANCYFONTS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_STYLISH_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_STYLISH_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.addStrikethroughOverlay
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.decorateText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.decorateTextB
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.decorateWithFace
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.fancyDecoratedText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.getCustomDecoratedText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.glitchNepali
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.heartStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.stylishDecorate
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBlackBoxedText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBoldCaps
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBoldFancyStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBoldFraktur
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBoldScript
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toBubbleBold
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toCherokeeStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toCursiveScriptText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toDotMatrix
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toDoubleStruck
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toDoubleStruck1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toEnclosedAlphanumerics
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toFancyAltText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toFancySerif
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toFancyStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toFraktur
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toInverted
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toItalic
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toItalicStrikethrough
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toLightGrayCaps
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toSansSerifItalic
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toScriptStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toSubscriptText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toUnderlineOverlayStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toUpsideDown
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toUsamaStyle
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.toZalgoText
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.TextStylesUtils.Companion.wrapWithMarkers
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate


class StylishTextFragment : Fragment() {
    private lateinit var binding: FragmentStylishTextBinding
    private lateinit var adapter: StylishTextAdapter
    private var navController: NavController? = null
    private val maxChars = 50
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStylishTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNavController()
        checkToLoadNative()
        binding.ivBackPress.setOnClickListener {
            navController?.safeNavigate(StylishTextFragmentDirections.actionStylishFragmentToNavHome())
        }
        if (isNetworkAvailable(requireContext()) && requireContext().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                SHOW_BANNER_STYLISH_TEXT, "ON"
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


    private fun checkToLoadNative() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_NATIVE_REVERSE_FANCYFONTS,resources.getString(R.string.NATIVE_DEFAULT))
        }
        else {
            resources.getString(R.string.NATIVE_DEFAULT)
        }

        if (isNetworkAvailable(requireContext()) && requireContext().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(SHOW_NATIVE_STYLISH_TEXT, "ON").equals("ON", true)
        ){
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                fragmentName = "theme",
                adId = adId!!,
                isMedia = false,
                isMediaOnLeft = false,
                adContainer = binding.nativeAdContainerAd,
                isMediumAd = false,
                onFailed = {
                    binding.nativeAdContainerAd.visibility = View.GONE
                },
                onAddLoaded = {
                    binding.shimmerLayoutBanner1.stopShimmer()
                    binding.shimmerLayoutBanner1.visibility = View.GONE
                })
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }
    }

    private fun loadBanner() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_BANNER_REVERSE_FANCYFONTS,resources.getString(R.string.BANNER_DEFAULT))
        }
        else{
            resources.getString(R.string.BANNER_DEFAULT)
        }

        val adView = AdView(requireContext())
        adView.setAdSize(adSize)
        adView.adUnitId =adId!!
        val extras = Bundle()
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                NativeMaster.collapsibleBannerAdMobHashMap!!["HomeActivity"] = adView

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("failederror", "error is" + error)
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
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
                requireActivity(),
                adWidth
            )
        }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as NavigationActivity).hidetoolbar()
        isNavControllerAdded()
//        requireActivity().findViewById<ImageView>(R.id.ivHistory)?.visibility = View.INVISIBLE
        requireActivity().findViewById<ImageView>(R.id.ivClose)?.visibility = View.INVISIBLE

        val txtUrduKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        txtUrduKeyboard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));


        val toolbar = requireActivity().findViewById<ConstraintLayout>(R.id.clSubDiv)
        toolbar.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_header_blue)

//        txtUrduKeyboard?.apply {
//            setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null
//            )
//            text = context.getString(R.string.fancy_font)
//            compoundDrawablePadding = 30
//            setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_DOWN && event.x <= (compoundDrawables[0]?.bounds?.width() ?: 0)) {
//                    navController?.safeNavigate(StylishTextFragmentDirections.actionStylishFragmentToNavHome())
//                    return@setOnTouchListener true
//                }
//                false
//            }
//        }
        initNavController()

        trackAnalyticsEvent()
        clickEvents()
        handleBackPressDispatcher()

    }

    override fun onPause() {
        super.onPause()
        (activity as NavigationActivity).showtoolbar()
    }


    private fun initNavController() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun handleBackPressDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navController != null) {
                        val action = StylishTextFragmentDirections.actionStylishFragmentToNavHome()
                        navController!!.safeNavigate(action)

                    } else {
                        initNavController()
                    }

                }
            }) }


    private fun clickEvents() {
        adapter = StylishTextAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.etInput.filters = arrayOf(InputFilter.LengthFilter(50)) // optional
        adapter.updateList(generateStyledTexts("Try To Make Your Own Styles"))

        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                binding.tvCharCount.text = String.format("%02d/%d", length, maxChars)
                adapter.updateList(generateStyledTexts(s.toString()))
                if (length == 0) {
                    adapter.updateList(generateStyledTexts("Try To Make Your Own Styles"))

                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                adapter.updateList(generateStyledTexts("Try To Make Your Own Styles"))

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun generateStyledTexts(input: String): List<String> {
        return listOf(
            input,
            TextStylesUtils.toCircledText(input),
            toSubscriptText(input),
            toItalic(input),
            toBoldFancyStyle(input),
            toScriptStyle(input),
            toItalicStrikethrough(input),
            glitchNepali(input),
            addStrikethroughOverlay(input),
            toUpsideDown(toUpsideDown(input)),
            toBlackBoxedText(input),
            toFancyAltText(input),
            toBoldFraktur(input),
            toDoubleStruck1(input),
            toBoldScript(input),
            toFraktur(input),
            toDoubleStruck(input),
            toSansSerifItalic(input),
            "\"✌" + toCursiveScriptText(input) + "✌\"",
            toEnclosedAlphanumerics(input),
            toUnderlineOverlayStyle(input),
            getCustomDecoratedText(input),
            toFancyStyle(input),
            toUsamaStyle(input),
            toFancySerif(input),
            toZalgoText(input),
            toBubbleBold(input),
            toCherokeeStyle(input),
            decorateText(input),
            decorateTextB(input),
            stylishDecorate(input),
            decorateWithFace(input),
            fancyDecoratedText(input),
            heartStyle(input),
            wrapWithMarkers(input),
            toLightGrayCaps(input),
            toBoldCaps(input),
            toDotMatrix(input),
            toInverted(input)
        )
    }

    private fun trackAnalyticsEvent() {
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "StylishTextFragment")
    }

}