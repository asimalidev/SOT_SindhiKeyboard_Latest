package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText

import android.net.Uri
import android.provider.Settings
import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.manual.mediation.library.sotadlib.data.Language
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSpeechBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.CountryModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_VOICE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_VOICE_TRANSLATOR_3_CLICKS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_VOICE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.SelectCountryDialog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects
import kotlin.text.get

class SpeechFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: FragmentSpeechBinding
    lateinit var navController: NavController
    private var tts: TextToSpeech? = null
    var btnFavoriteClicked = false
    lateinit var mSharedPreferences: SharedPreferences
    var autoSpeakSTTSwitch: SwitchCompat? = null
    var autoSpeakSTTIsOn = true
    var bundle = Bundle()
    private var micPermissionDeniedCount = 0
    private var showAdCounterOnThreeClicks = 0
    lateinit var fromlanguacode: String
    lateinit var tolanguacode: String
    var fromCountryFlag: Int = 0
    var toCountryFlag: Int = 0
    var fromCountryName = ""
    var toCountryName = ""
    var fromTo = ""
    var isPurchase = false

    private val selectCountryDialog: SelectCountryDialog by lazy {
        SelectCountryDialog().apply {
            setCountrySelectionListener(object : SelectCountryDialog.CountrySelectionListener {
                override fun onCountrySelected(country: CountryModel) {
                    Toast.makeText(context, country.countryName, Toast.LENGTH_SHORT).show()
                    if (fromTo == "from") {
                        setFromLanguage(country = country.countryName)
                    } else if (fromTo == "to") {
                        setToLanguage(country = country.countryName)
                    }
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpeechBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        bundle.putString("SpeechFragment", "SpeechFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_speech", bundle)
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true
        mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        tts = TextToSpeech(requireActivity(), this, "com.google.android.tts")

        fromCountryFlag =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getInt("fromFlagSTT", Language.English.imageId)
        toCountryFlag =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getInt("toFlagSTT", Language.Urdu.imageId)

        fromCountryName =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getString("fromLangSTT", "English (En)").toString()
        toCountryName =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getString("toLangSTT", "Sindhi (سنڌي)").toString()



        setFromLanguage(fromCountryName)
        setToLanguage(toCountryName)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Fix: Check if we are still on the correct fragment before navigating
                    if (navController.currentDestination?.id == R.id.speechFragment) {
                        val action =
                            SpeechFragmentDirections.actionSpeechFragmentToTranslationFragment()
                        navController.navigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                }
            })

        binding.btnfrom.blockingClickListener {
            fromTo = "from"
            if (!selectCountryDialog.isAdded) {
                selectCountryDialog.show(
                    parentFragmentManager,
                    SelectCountryDialog::class.java.simpleName
                )
            }
        }

        binding.btnSpeaker.blockingClickListener {
            val text = binding.etsource.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "please write the text", Toast.LENGTH_SHORT).show()
            } else {
                speakOut(text)
            }
        }

        binding.btnto.blockingClickListener {
            fromTo = "to"
            if (!selectCountryDialog.isAdded) {
                selectCountryDialog.show(
                    parentFragmentManager,
                    SelectCountryDialog::class.java.simpleName
                )
            }
        }

        binding.btnmic.blockingClickListener {
            askforpermission()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    getFromLanguageCode(fromCountryName)
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak To convert into text")
                try {
                    startActivityForResult(intent, 101)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.ivSwipeLanguage.setOnClickListener {
            val rotate = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 500
            rotate.repeatCount = 0
            binding.ivSwipeLanguage.animation = rotate
            binding.ivSwipeLanguage.startAnimation(rotate)

            binding.imgClearText.performClick()
            val toCN = toCountryName
            val fromCN = fromCountryName
            setFromLanguage(toCN)
            setToLanguage(fromCN)
        }

        binding.btntranslate.blockingClickListener {
            requireActivity().hideKeyboard()
            if (binding.etsource.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "No Text", Toast.LENGTH_SHORT).show()
            } else {
                if (NetworkCheck.isNetworkAvailable(requireContext())
                    && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                        .getString(SHOW_INTER_VOICE_TRANSLATOR_3_CLICKS, "ON")
                        .equals("ON", true)
                ) {
                    if (isPurchase) {
                        btnTranslateClicked()
                    } else {
                        if (showAdCounterOnThreeClicks >= 2) {
                            showAdCounterOnThreeClicks = 0
                            showAdThenTranslate()
                        } else {
                            btnTranslateClicked()
                        }
                    }
                } else {
                    btnTranslateClicked()
                }
            }
        }

        binding.etsource.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (s.isNotEmpty()) {
                        binding.imgClearText.visibility = View.VISIBLE
                    } else {
                        binding.imgClearText.visibility = View.INVISIBLE
                    }
                }
            }
        })

        binding.imgClearText.blockingClickListener {
            binding.etsource.setText("")
            binding.imgClearText.visibility = View.INVISIBLE
            binding.clTranslated.visibility = View.GONE
        }

        binding.btnspeaker1.blockingClickListener {
            val text = binding.tvtranslatedtext.text.toString()
            speakOut(text)
        }

        binding.btncopytext1.blockingClickListener {
            val text = binding.tvtranslatedtext.text.toString()
            copytexttoclipborad(text)
        }

        binding.ivWhatsApp.blockingClickListener {
            val textToShare = binding.tvtranslatedtext.text.toString().trim()
            if (textToShare.isNotEmpty()) {
                shareOnWhatsApp(textToShare)
            } else {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnshare.blockingClickListener {
            if (binding.tvtranslatedtext.text.isNotEmpty()) {
//                shareOnWhatsApp()
                val s = binding.tvtranslatedtext.text.toString().trim()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareOnWhatsApp(text: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            requireActivity().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            // Try WhatsApp Business
            val whatsappBusinessIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp.w4b")
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                requireActivity().startActivity(whatsappBusinessIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(requireActivity(), "No WhatsApp found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAdThenTranslate() {
        InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
            requireActivity(),
            "SpeechFragment",
            onAdClosedCallBackAdmob = {
                showAdCounterOnThreeClicks = 0
                Handler(Looper.getMainLooper()).postDelayed({
                    btnTranslateClicked()
                }, 300)
            },
            onAdShowedCallBackAdmob = { })
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun loadAdmobCollapsibleBannerAd() {
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("VoiceTranslatorFragment")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["VoiceTranslatorFragment"]
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

    private fun loadCollapsibleBanner() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_BANNER_TRANSLATOR, resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }

        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
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
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                        OVERALL_BANNER_RELOADING, "SAVE"
                    ).equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["VoiceTranslatorFragment"] = adView
                }

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
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


    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        if (isPurchase) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext()) &&
                requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(SHOW_NATIVE_VOICE_TRANSLATOR, "ON").equals("ON", true)
                && !isPurchase
            ) {
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(AD_ID_NATIVE_TRANSLATOR,  resources.getString(R.string.NATIVE_DEFAULT))
                } else {
                    resources.getString(R.string.NATIVE_DEFAULT)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "Speech",
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

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_BANNER_VOICE_TRANSLATOR, "ON").equals("ON", true)
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

        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonConversation)
            .let { it?.visibility = View.INVISIBLE }
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }
        autoSpeakSTTSwitch = requireActivity().findViewById(R.id.switchButtonTTS)
        autoSpeakSTTIsOn = mSharedPreferences.getBoolean("AutoSpeakSTTIsOn", true)
        if (autoSpeakSTTSwitch != null) {
            autoSpeakSTTSwitch!!.visibility = View.VISIBLE
            autoSpeakSTTSwitch?.isChecked = mSharedPreferences.getBoolean("AutoSpeakSTTIsOn", true)

            autoSpeakSTTSwitch?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    autoSpeakSTTIsOn = true
                    mSharedPreferences.edit().putBoolean("AutoSpeakSTTIsOn", autoSpeakSTTIsOn)
                        .apply()
                    Log.e("TAG", "SwitchState:$autoSpeakSTTIsOn")
                } else if (!isChecked) {
                    autoSpeakSTTIsOn = false
                    mSharedPreferences.edit().putBoolean("AutoSpeakSTTIsOn", autoSpeakSTTIsOn)
                        .apply()
                    Log.e("TAG", "SwitchState:$autoSpeakSTTIsOn")
                }
            }
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
            txtSindhiKeyboard.text = resources.getString(R.string.label_speechTotext)
            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx

            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {  // Fixed: ACTION_DOWN instead of ACTION.DOWN
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        // Ensure navController is properly initialized before navigating
                        val currentNavController = navController ?: findNavController()
                        try {
                            val action =
                                SpeechFragmentDirections.actionSpeechFragmentToTranslationFragment()
                            currentNavController.navigate(action)
                        } catch (e: Exception) {
                            Log.e("Navigation", "Failed to navigate: ${e.message}")
                            // Fallback navigation or error handling
                            try {
                                findNavController().navigate(R.id.translationFragment) // Use direct ID if available
                            } catch (e2: Exception) {
                                Log.e(
                                    "Navigation",
                                    "Fallback navigation also failed: ${e2.message}"
                                )
                                requireActivity().onBackPressed() // Final fallback
                            }
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }


    private fun btnTranslateClicked() {
        val ctx = context ?: return // ensures Fragment is attached
        val sourceText = binding.etsource.text.toString()

        when {
            sourceText.isEmpty() -> {
                Toast.makeText(ctx, "No Text", Toast.LENGTH_SHORT).show()
            }

            fromlanguacode == "null" -> {
                Toast.makeText(ctx, "Please Select A Source Language", Toast.LENGTH_SHORT).show()
            }

            tolanguacode == "null" -> {
                Toast.makeText(ctx, "Please Select A Language for translation", Toast.LENGTH_SHORT)
                    .show()
            }

            !NetworkCheck.isNetworkAvailable(ctx) -> {
                Toast.makeText(ctx, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            }

            else -> {
                translateText(fromlanguacode, tolanguacode, sourceText)
            }
        }
    }




    private fun setFromLanguage(country: String) {
        fromCountryName = country
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
            .putString("fromlangVT", fromCountryName).apply()
        fromlanguacode = getFromLanguageCode(fromCountryName)
        binding.tvfromcountryname.text = fromCountryName

        setFlagFromLanguageCode(fromCountryName)
    }

    private fun setToLanguage(country: String) {
        toCountryName = country
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
            .putString("tolangVT", toCountryName).apply()
        tolanguacode = getFromLanguageCode(toCountryName)
        binding.tvtocountryname.text = toCountryName

        setFlagToLanguageCode(toCountryName)
    }


    private fun getFromLanguageCode(code: String): String {
        var languageCode: String? = null
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                languageCode = AllCountryListMe.getCountryList()[i].code
                return languageCode
            }
        }
        return "en"
    }

    private fun setFlagToLanguageCode(code: String) {
        var flagID: Int
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                flagID = AllCountryListMe.getCountryList()[i].arImage
                android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
                    .edit().putInt("toFlagSTT", flagID).apply()
                toCountryFlag = flagID
                toCountryFlag.let {
                    binding.tocountryflag.background =
                        AppCompatResources.getDrawable(requireActivity(), toCountryFlag)
                }
            }
        }
    }

    private fun setFlagFromLanguageCode(code: String) {
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                val flagID = AllCountryListMe.getCountryList()[i].arImage
                if (flagID != 0) {
                    try {
                        fromCountryFlag = flagID
                        binding.ivfromflag.background = AppCompatResources.getDrawable(requireContext(), fromCountryFlag)
                    } catch (e: Resources.NotFoundException) {
                        e.printStackTrace()
                    }
                }
                break
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localCode = "ur_PK"
            val locale = Locale(localCode)
            val result = tts!!.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }
        }
    }


    fun askforpermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    // Reset counter when permission is granted
                    micPermissionDeniedCount = 0
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response != null) {
                        // Check if it's permanently denied or denied twice
                        if (response.isPermanentlyDenied || micPermissionDeniedCount >= 1) {
                            Toast.makeText(
                                requireContext(),
                                "Please enable Microphone permission from app settings",
                                Toast.LENGTH_LONG
                            ).show()

                            // Open App Settings
                            try {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", requireActivity().packageName, null)
                                )
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            micPermissionDeniedCount++
                            Toast.makeText(
                                requireContext(),
                                "Microphone permission is required to use speech-to-text",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun copytexttoclipborad(text: String) {
        val clipboard: ClipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireActivity(), "Text Copied", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val result: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                binding.etsource.setText(
                    Objects.requireNonNull(result)[0]
                )
            }
        }
    }

    val job = Job()


    fun translateText(fromLanguageCode: String, toLanguageCode: String, source: String) {
        if (!isAdded || context == null) return // fragment not attached

        if (!NetworkCheck.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show()
            return
        }

        showAdCounterOnThreeClicks += 1

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val translatedString = withContext(Dispatchers.IO) {
                    TranslatorCall.callUrlAndParseResult(source, fromLanguageCode, toLanguageCode)
                }

                if (!isAdded || view == null) return@launch // fragment might be detached

                if (!translatedString.isNullOrEmpty()) {
                    if (autoSpeakSTTIsOn) {
                        speakOut(translatedString)
                    }

                    binding.tvtranslatedtext.setText(translatedString)
                    binding.clTranslated.visibility = View.VISIBLE
//                    binding.animationView.visibility = View.GONE
//                    binding.animationView.cancelAnimation()
                    btnFavoriteClicked = true
                } else {
                    Toast.makeText(requireContext(), "Translation failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

}