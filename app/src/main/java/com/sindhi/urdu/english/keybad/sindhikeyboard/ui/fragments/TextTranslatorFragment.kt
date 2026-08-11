package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.manual.mediation.library.sotadlib.data.Language
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentTextTranslatorBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountryListMe
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.TranslatorCall
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.CountryModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_TEXT_TRANSLATOR_3_CLICKS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.SelectCountryDialog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects

class TextTranslatorFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: FragmentTextTranslatorBinding
    lateinit var navController: NavController
    lateinit var fromlanguacode: String
    lateinit var tolanguacode: String
    private var tts: TextToSpeech? = null
    var btnFavoriteClicked = false
    var autoSpeakTTSwitch: SwitchCompat? = null
    var autoSpeakTTIsOn = true
    var bundle = Bundle()
    private var showAdCounterOnThreeClicks = 1
    var fromCountryFlag: Int = 0
    var toCountryFlag: Int = 0
    var fromCountryName = ""
    var toCountryName = ""
    var fromTo = ""
    var autoSpeakSTTSwitch: SwitchCompat? = null
    var autoSpeakSTTIsOn = true
    lateinit var mSharedPreferences: SharedPreferences

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTextTranslatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showSelectCountryDialog() {
        val dialog = SelectCountryDialog().apply {
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
        dialog.show(parentFragmentManager, SelectCountryDialog::class.java.simpleName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "text_translator_scr")
        mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        tts = TextToSpeech(requireActivity(), this, "com.google.android.tts")

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_BANNER_TEXT_TRANSLATOR, "ON").equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("Texttanslation")) {
                val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["Texttanslation"]
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

        fromCountryFlag = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getInt("fromflagTT", Language.English.imageId)
        toCountryFlag = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getInt("toflagTT", Language.Urdu.imageId)

        fromCountryName = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getString("fromlangTT", "English (En)").toString()
        toCountryName = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getString("tolangTT", "Sindhi (سنڌي)").toString()

        fromCountryFlag.let {
            binding.ivfromflag.background = AppCompatResources.getDrawable(requireActivity(), fromCountryFlag)
        }

        toCountryFlag.let {
            binding.tocountryflag.background = AppCompatResources.getDrawable(requireActivity(), toCountryFlag)
        }

        setFromLanguage(fromCountryName)
        setToLanguage(toCountryName)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action= TextTranslatorFragmentDirections.actionNavTextTranslatorToNavTranslator()
                    findNavController().navigate(action)
                }
            })

        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_NATIVE_TRANSLATOR, resources.getString(R.string.NATIVE_DEFAULT))
        } else {
            resources.getString(R.string.NATIVE_DEFAULT)
        }

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
                .getString(SHOW_NATIVE_TEXT_TRANSLATOR, "ON").equals("ON", true)
        ) {
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                adId = adId!!,
                fragmentName = "Texttanslation",
                isMedia = false,
                isMediaOnLeft = false,
                adContainer = binding.nativeAdContainerAd,
                isMediumAd = false,
                onFailed = { binding.nativeAdContainerAd.visibility = View.GONE },
                onAddLoaded = {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                })
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
        }

        binding.btnfrom.blockingClickListener {
            fromTo = "from"
            if (isAdded) {
                showSelectCountryDialog()
            }
        }

        binding.btnto.blockingClickListener {
            fromTo = "to"
            if (isAdded) {
                showSelectCountryDialog()
            }
        }

        binding.btnto.blockingClickListener {
            fromTo = "to"
            if (isAdded) {
                showSelectCountryDialog()
            }
        }

        binding.btntranslate.blockingClickListener {
            requireActivity().hideKeyboard()
            if (binding.etsource.text.toString().isEmpty()) {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            } else {
                if (NetworkCheck.isNetworkAvailable(requireActivity())) {
                    if (!requireActivity().getSharedPreferences(
                            RemoteConfigConst.REMOTE_CONFIG,
                            Context.MODE_PRIVATE
                        ).getBoolean(Preferences.IS_PURCHASED, false)
                        && requireActivity().getSharedPreferences(
                            "RemoteConfig",
                            Context.MODE_PRIVATE
                        ).getString(
                            SHOW_INTER_TEXT_TRANSLATOR_3_CLICKS, "ON"
                        ).equals("ON", true)
                    ) {
                        if (showAdCounterOnThreeClicks >= 2) {
                            showAdCounterOnThreeClicks = 0
                            showAdThenTranslate()
                        } else {
                            btnTranslateClicked()
                        }
                    } else {
                        btnTranslateClicked()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Please turn on internet", Toast.LENGTH_SHORT)
                        .show()
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

        binding.btnshare.blockingClickListener {
            if (binding.tvtranslatedtext.text.isEmpty()) {
                Toast.makeText(requireActivity(), "Nothing to share", Toast.LENGTH_SHORT).show()
            } else {
                val s = binding.tvtranslatedtext.text.toString().trim()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
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

        binding.ivWhatsApp.blockingClickListener {
            val textToShare = binding.tvtranslatedtext.text.toString().trim()
            if (textToShare.isNotEmpty()) {
                shareOnWhatsApp(textToShare)
            } else {
                Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun btnTranslateClicked() {
        val sourceText = binding.etsource.text.toString()
        if (sourceText.isEmpty()) {
            Toast.makeText(requireActivity(), "No Text", Toast.LENGTH_SHORT).show()
        } else if (fromlanguacode == "null") {
            Toast.makeText(requireActivity(), "Please Select A Source Language", Toast.LENGTH_SHORT)
                .show()
        } else if (tolanguacode == "null") {
            Toast.makeText(
                requireActivity(),
                "Please Select A Language for translation",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            translateText(fromlanguacode, tolanguacode, sourceText)
        }
    }

    private fun loadBanner() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_BANNER_TRANSLATOR, resources.getString(R.string.BANNER_DEFAULT))
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

                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["Texttanslation"] = adView
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
            nameFragment = "SpeechFragment",
            onAdClosedCallBackAdmob = {
                showAdCounterOnThreeClicks = 0
                Handler(Looper.getMainLooper()).postDelayed({
                    btnTranslateClicked()
                }, 300)
            },
            onAdShowedCallBackAdmob = { })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        requireActivity().findViewById<ImageView>(R.id.ivHistory)
            .let { it?.visibility = View.INVISIBLE }

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
        val clSubDiv = requireActivity().findViewById<ConstraintLayout>(R.id.clSubDiv)

        if (clSubDiv != null) {
            clSubDiv.background = requireActivity().resources.getDrawable(R.drawable.bg_header_blue, null)
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = resources.getString(R.string.label_text_translator)
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
                            val action= TextTranslatorFragmentDirections.actionNavTextTranslatorToNavTranslator()
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            Log.e("Navigation", "Failed to navigate: ${e.message}")
                            // Fallback navigation or error handling
                            try {
                               val action= TextTranslatorFragmentDirections.actionNavTextTranslatorToNavTranslator()
                                findNavController().navigate(action)
                            } catch (e2: Exception) {
                                Log.e(
                                    "Navigation",
                                    "Fallback navigation also failed: ${e2.message}"
                                )
                                val action= TextTranslatorFragmentDirections.actionNavTextTranslatorToNavTranslator()
                                findNavController().navigate(action)
                            }
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }


    private fun setFromLanguage(country: String) {
        fromCountryName = country
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
            .putString("fromlangTT", fromCountryName).apply()
        fromlanguacode = getFromLanguageCode(fromCountryName)
        binding.tvfromcountryname.text = fromCountryName

        setFlagFromLanguageCode(fromCountryName)

        // Hide the translated layout when the 'From' language is changed
        binding.clTranslated.visibility = View.GONE
    }

    private fun setToLanguage(country: String) {
        toCountryName = country
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
            .putString("tolangTT", toCountryName).apply()
        tolanguacode = getFromLanguageCode(toCountryName)
        binding.tvtocountryname.text = toCountryName

        setFlagToLanguageCode(toCountryName)

        // Hide the translated layout when the 'To' language is changed
        binding.clTranslated.visibility = View.GONE
    }

//    private fun setToLanguage(country: String) {
//        toCountryName = country
//        PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
//            .putString("tolangTT", toCountryName).apply()
//        tolanguacode = getFromLanguageCode(toCountryName)
//        binding.tvtocountryname.text = toCountryName
//
//        setFlagToLanguageCode(toCountryName)
//    }

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
                PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
                    .putInt("toflagTT", flagID).apply()
                toCountryFlag = flagID
                toCountryFlag.let {
                    binding.tocountryflag.background =
                        AppCompatResources.getDrawable(requireActivity(), toCountryFlag)
                }
            }
        }
    }

    private fun setFlagFromLanguageCode(code: String) {
        var flagID: Int
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                flagID = AllCountryListMe.getCountryList()[i].arImage
                PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
                    .putInt("fromflagTT", flagID).apply()
                fromCountryFlag = flagID
                fromCountryFlag.let {
                    binding.ivfromflag.background =
                        AppCompatResources.getDrawable(requireActivity(), fromCountryFlag)
                }
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

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun copytexttoclipborad(text: String) {
        val clipboard: ClipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
        showAdCounterOnThreeClicks += 1
        CoroutineScope(job).launch(Dispatchers.Main) {
            val translatedString = withContext(Dispatchers.IO) {
                TranslatorCall.callUrlAndParseResult(
                    source,
                    fromLanguageCode,
                    toLanguageCode
                )
            }

            translatedString?.let {

                if (autoSpeakTTIsOn) {
                    speakOut(it)
                }
                binding.tvtranslatedtext.setText(it)
                binding.clTranslated.visibility = View.VISIBLE

                btnFavoriteClicked = true
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