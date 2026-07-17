package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.TextReverse

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentTextReverseBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate
import java.util.Locale
import kotlin.collections.set

class TextReverseFragment : Fragment() {
    private val defBanner="ca-app-pub-3747520410546258/1697692330"
    lateinit var binding: FragmentTextReverseBinding
    var navController: NavController? = null

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTextReverseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "text_reverse_scr")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController != null) {
                    val action = TextReverseFragmentDirections.actionTextReverseToHome()
                    navController?.safeNavigate(action)
                } else {
                    isNavControllerAdded()
                }
            }
        })

        binding.tvReverse.blockingClickListener {
            reverseText()
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(BANNER_TEXT_REVERSE, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeFragment")) {
                val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"]
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            } else {
                loadBanner()
            }
        } else {

            binding.adViewContainer.visibility = View.GONE
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }


        binding.ivDelete.blockingClickListener {
            binding.tvResult.text = ""
            binding.etTestKeyboard.setText("")
        }

        binding.tvFlip.blockingClickListener {
            binding.tvResult.text = getFlippedOrEncircled(binding.etTestKeyboard.text.toString(),false, requireActivity())
        }


        binding.ivWhatsApp.blockingClickListener {
            if (binding.tvResult.text.isNotEmpty()) {
                shareOnWhatsApp()
            } else {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivShare.blockingClickListener {
            if (binding.tvResult.text.isNotEmpty()) {
                shareAnywhere()
            } else {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivCopy.blockingClickListener {
            copyText()
        }
    }



    private fun getFlippedOrEncircled(toEncode: String, encircled: Boolean, context: Context): String {
        val unEncoded = toEncode.lowercase(Locale.getDefault())
        val length = toEncode.length
        val styledString = StringBuilder(length)
        val style: Array<String> = if (encircled) {
            context.resources.getStringArray(R.array.encircled)
            //Log("encircled : " + Arrays.toString(style));
        } else {
            context.resources.getStringArray(R.array.simple_flip)
            //Log("simple_flip : " + Arrays.toString(style));
        }
        var index = 0
        while (index < length) {
            when (unEncoded[index]) {
                'a' -> styledString.append(style[0])
                'b' -> styledString.append(style[1])
                'c' -> styledString.append(style[2])
                'd' -> styledString.append(style[3])
                'e' -> styledString.append(style[4])
                'f' -> styledString.append(style[5])
                'g' -> styledString.append(style[6])
                'h' -> styledString.append(style[7])
                'i' -> styledString.append(style[8])
                'j' -> styledString.append(style[9])
                'k' -> styledString.append(style[10])
                'l' -> styledString.append(style[11])
                'm' -> styledString.append(style[12])
                'n' -> styledString.append(style[13])
                'o' -> styledString.append(style[14])
                'p' -> styledString.append(style[15])
                'q' -> styledString.append(style[16])
                'r' -> styledString.append(style[17])
                's' -> styledString.append(style[18])
                't' -> styledString.append(style[19])
                'u' -> styledString.append(style[20])
                'v' -> styledString.append(style[21])
                'w' -> styledString.append(style[22])
                'x' -> styledString.append(style[23])
                'y' -> styledString.append(style[24])
                'z' -> styledString.append(style[25])
                ',' -> if (!encircled) {
                    styledString.append("ʻ")
                }

                '"' -> if (!encircled) {
                    styledString.append("❝")
                }

                '_' -> if (!encircled) {
                    styledString.append("‾")
                }

                '?' -> if (!encircled) {
                    styledString.append("¿")
                }

                '!' -> if (!encircled) {
                    styledString.append("i")
                }

                '1' -> if (encircled) {
                    styledString.append(style[26])
                }

                '2' -> if (encircled) {
                    styledString.append(style[27])
                }

                '3' -> if (encircled) {
                    styledString.append(style[28])
                }

                '4' -> if (encircled) {
                    styledString.append(style[29])
                }

                '5' -> if (encircled) {
                    styledString.append(style[30])
                }

                '6' -> if (encircled) {
                    styledString.append(style[31])
                }

                '7' -> if (encircled) {
                    styledString.append(style[32])
                }

                '8' -> if (encircled) {
                    styledString.append(style[33])
                }

                '9' -> if (encircled) {
                    styledString.append(style[34])
                }

                '0' -> if (encircled) {
                    styledString.append(style[14])
                }

                else -> styledString.append(unEncoded[index])
            }
            index++
        }
        return if (encircled) {
            styledString.toString()
        } else styledString.reverse().toString()
    }



    private fun loadBanner() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(BANNER_INSIDE, defBanner)
        } else {
            resources.getString(R.string.admob_banner_inside)
        }

        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!

        // 💡 FIX: Just build a standard AdRequest without the collapsible extras
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)

                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val pref =requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref?.getString(NATIVE_OVER_ALL,"ca-app-pub-3747520410546258/1702944653")
        }
        else{
            resources.getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED,false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(NATIVE_TEXT_REVERSE,"ON").equals("ON",true)) {
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                fragmentName = "TextReverseResizeFragment",
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
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }

        val clSubDiv = requireActivity().findViewById<ConstraintLayout>(R.id.clSubDiv)
        if (clSubDiv != null) {
            clSubDiv.background = requireActivity().resources.getDrawable(R.drawable.bg_header_blue, null)
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtUrduKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtUrduKeyboard != null) {
            txtUrduKeyboard.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null)
            txtUrduKeyboard.text = resources.getString(R.string.label_text_reverse)
            txtUrduKeyboard.setTextColor(resources.getColor(R.color.white, null))

            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtUrduKeyboard.compoundDrawablePadding = gapInPx
            val startDrawable = txtUrduKeyboard.compoundDrawables[0]
            txtUrduKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        requireActivity().onBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    private fun reverseText() {
        binding.tvResult.text = binding.etTestKeyboard.text.reversed()
    }

    private fun shareOnWhatsApp() {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, binding.tvResult.text)
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            whatsappIntent.setPackage("com.whatsapp.w4b")
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, binding.tvResult.text)
            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                requireContext().startActivity(whatsappIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(requireActivity(), "No Whats app found !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareAnywhere() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Flipped Text")
        shareIntent.putExtra(Intent.EXTRA_TEXT, binding.tvResult.text)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun copyText() {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", binding.tvResult.text.toString())
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireActivity(), "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}