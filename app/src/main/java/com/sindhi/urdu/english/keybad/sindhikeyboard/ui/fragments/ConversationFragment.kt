package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import com.sindhi.urdu.english.keybad.databinding.FragmentConversationBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountryListMe
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.SpeechFragmentDirections
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.TranslatorCall
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.history_fragment.Companion.conversationExtensionForwardList
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtensionTemp
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.CountryModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.SelectCountryDialog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.ViewModelFactory
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects

class ConversationFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: FragmentConversationBinding
    var fromlanguageflag: Int = 0
    var tolanguageflag: Int = 0
    lateinit var viewModel: HistoryViewModel
    private var navController: NavController? = null
    lateinit var fromCode: String
    lateinit var toCode: String
    lateinit var sourceText: String
    var fromCountryFlag: Int = 0
    var toCountryFlag: Int = 0

    var btnFrom = false
    var btnTo = false
    private val job = Job()
    lateinit var adapter: GroupAdapter<GroupieViewHolder>
    private var tts: TextToSpeech? = null
    private var instance: ConversationFragment? = null
    private val args: ConversationFragmentArgs by navArgs()
    var conversationName = "New1"
    var fromCountryName = ""
    var toCountryName = "String"
    var isFavorite = false
    var conversationExtension: ArrayList<ConversationExtension> = ArrayList()
    var convExtTempForUpdatingAdapter: ArrayList<ConversationExtension> = ArrayList()
    private val dialogSelectCountryDialog = SelectCountryDialog()

    private var showSave = false
    private var showDelete = false

    private var isExpanded = false
    lateinit var mSharedPreferences: SharedPreferences
    var fromTo = ""
    var autoSpeakSwitch: SwitchCompat? = null
    var autoSpeakIsOn = true

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.from_bottom_fab
        )
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.to_bottom_fab
        )
    }
    private val rotateClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_clock_wise
        )
    }
    private val rotateAntiClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_anti_clock_wise
        )
    }

    private val delayedActionHandler = Handler()

    private val delayedActionRunnable = kotlinx.coroutines.Runnable {
        if (isAdded) {
            if (isExpanded) {
                shrinkFab()
            }
        }
    }

    private fun startDelayedAction() {
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
        delayedActionHandler.postDelayed(delayedActionRunnable, 5000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "conversation_scr")
        isNavControllerAdded()

        fromCountryFlag = android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getInt("fromflagTT", Language.English.imageId)
        toCountryFlag = android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getInt("toflagTT", Language.Urdu.imageId)

        fromCountryFlag.let {
            binding.ivfromflag.background =
                AppCompatResources.getDrawable(requireActivity(), fromCountryFlag)
        }

        toCountryFlag.let {
            binding.tocountryflag.background = AppCompatResources.getDrawable(requireActivity(), toCountryFlag)
        }


        mSharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }


        dialogSelectCountryDialog.setCountrySelectionListener(object :
            SelectCountryDialog.CountrySelectionListener {
            override fun onCountrySelected(country: CountryModel) {
                Toast.makeText(context, country.countryName, Toast.LENGTH_SHORT).show()
                if (fromTo == "from") {
                    setFromLanguage(country = country)
                } else if (fromTo == "to") {
                    setToLanguage(country = country)
                }
            }
        })

        binding.animationView.visibility = View.GONE

        fromlanguageflag = args.fromFlag
        tolanguageflag = args.toFlag
        if (fromlanguageflag == 0 && tolanguageflag == 0) {
            conversationName = conversationExtensionForwardList[0].conversationName
        }

        setFromLanguage()
        setToLanguage()

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(requireActivity().application)
        )[HistoryViewModel::class.java]

        tts = TextToSpeech(requireContext(), this, "com.google.android.tts")
        adapter = GroupAdapter<GroupieViewHolder>()
        binding.rvConversation.setHasFixedSize(false)

        if (fromlanguageflag == 0 && tolanguageflag == 0) {
            viewModel.updateTheList(false)
        } else {
            viewModel.updateTheList(true)
        }

        viewModel.conversationExtensionTemp.observe(viewLifecycleOwner) { mainObject ->
            mainObject.forEach {
                if (it.from == "from") {
                    tts?.let { it1 ->
                        ConversationFromItem(it.translation, it.source, it1, requireActivity())
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(it.translation, it.source, it1, requireActivity())
                    }?.let { it2 -> adapter.add(it2) }
                }
                val existingItem = conversationExtension.find { existing ->
                    existing.conversationName == it.conversationName &&
                            existing.source == it.source &&
                            existing.translation == it.translation &&
                            existing.from == it.from &&
                            existing.fromLang == it.fromLang &&
                            existing.toLang == it.toLang &&
                            existing.isFavorite == it.isFavorite &&
                            existing.time == it.time
                }
                if (existingItem == null) {
                    conversationExtension.add(
                        ConversationExtension(
                            conversationName = it.conversationName,
                            source = it.source,
                            translation = it.translation,
                            from = it.from,
                            fromLang = it.fromLang,
                            toLang = it.toLang,
                            isFavorite = it.isFavorite,
                            time = it.time
                        )
                    )
                }
            }

            if (adapter.itemCount > 0) {
                binding.emptyConversation.visibility = View.GONE
                binding.fabConstraint.visibility = View.VISIBLE
                binding.rvConversation.smoothScrollToPosition(adapter.itemCount - 1)
            } else {
                binding.emptyConversation.visibility = View.VISIBLE
                binding.fabConstraint.visibility = View.INVISIBLE
            }

            if (mainObject.isNotEmpty()) {
                fadeFabViewButton()
            }
        }

        viewModel.conversationExtensionPermanent.observe(viewLifecycleOwner) { mainObject ->
            mainObject.forEach {
                if (it.from == "from") {
                    tts?.let { it1 ->
                        ConversationFromItem(
                            it.translation,
                            it.source,
                            it1,
                            requireActivity()
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(
                            it.translation,
                            it.source,
                            it1,
                            requireActivity()
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }
            }
            if (adapter.itemCount > 0) {
                binding.emptyConversation.visibility = View.GONE
                binding.fabConstraint.visibility = View.VISIBLE
                binding.rvConversation.smoothScrollToPosition(adapter.itemCount - 1)
            } else {
                binding.fabConstraint.visibility = View.INVISIBLE
                binding.emptyConversation.visibility = View.VISIBLE
            }

            if (mainObject.isNotEmpty()) {
                fadeFabViewButton()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    myBackPressed()
                }
            })

        binding.tvfromcountryname.blockingClickListener {
            fromTo = "from"
            dialogSelectCountryDialog.show(
                parentFragmentManager,
                SelectCountryDialog::class.java.simpleName
            )
        }

        binding.tvtocountryname.isSelected = true
        binding.tvfromcountryname.isSelected = true

        binding.tvtocountryname.blockingClickListener {
            fromTo = "to"
            dialogSelectCountryDialog.show(
                parentFragmentManager,
                SelectCountryDialog::class.java.simpleName
            )
        }

        binding.ivmic1.blockingClickListener {
            askForPermission()
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
                    getCodeForSpeechRecognizer(toCountryName)
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text")
                try {
                    btnTo = true
                    startActivityForResult(intent, 101)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.ivmic.blockingClickListener {
            askForPermission()
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
                    getCodeForSpeechRecognizer(fromCountryName)
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text")
                try {
                    btnFrom = true
                    startActivityForResult(intent, 101)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.rvConversation.adapter = adapter

        binding.imgSave.blockingClickListener {
            CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "tap_save_conversation")
            showSaveDialog()
        }

        binding.imgClear.blockingClickListener {
            CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "tap_delete_conversation")
            showClearDialog()
        }

        binding.mainFabBtn.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            } else {
                expandFab()
                startDelayedAction()
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

            val toCN = toCountryName
            val fromCN = fromCountryName
            setFromLanguage(getCountryModel(toCN))
            setToLanguage(getCountryModel(fromCN))
        }
    }

    private fun setFlagFromLanguageCode(code: String) {
        var flagID: Int
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                flagID = AllCountryListMe.getCountryList()[i].arImage
                android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
                    .putInt("fromflagTT", flagID).apply()
                fromCountryFlag = flagID
                fromCountryFlag.let {
                    binding.ivfromflag.background =
                        AppCompatResources.getDrawable(requireActivity(), fromCountryFlag)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = resources.getString(R.string.label_conversation)
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
            }}

        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_NATIVE_CONVERSATION, "ON").equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(
                RemoteConfigConst.REMOTE_CONFIG,
                Context.MODE_PRIVATE
            ).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(SHOW_BANNER_CONVERSATION, "ON").equals("ON", true)
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

        requireActivity().findViewById<ImageView>(R.id.ivHistory)
            .let { it?.visibility = View.INVISIBLE }
        requireActivity().findViewById<ImageView>(R.id.ivClose)
            .let { it?.visibility = View.INVISIBLE }

        autoSpeakSwitch = requireActivity().findViewById(R.id.switchButtonConversation)
        autoSpeakIsOn = mSharedPreferences.getBoolean("AutoSpeakIsOn", true)

        if (autoSpeakSwitch != null) {
            autoSpeakSwitch!!.visibility = View.VISIBLE
            autoSpeakSwitch?.isChecked = mSharedPreferences.getBoolean("AutoSpeakIsOn", true)

            autoSpeakSwitch?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    autoSpeakIsOn = true
                    mSharedPreferences.edit().putBoolean("AutoSpeakIsOn", autoSpeakIsOn).apply()
                    Log.e("TAG", "SwitchState:$autoSpeakIsOn")
                } else if (!isChecked) {
                    autoSpeakIsOn = false
                    mSharedPreferences.edit().putBoolean("AutoSpeakIsOn", autoSpeakIsOn).apply()
                    Log.e("TAG", "SwitchState:$autoSpeakIsOn")
                }
            }
        }


    }

    private fun loadAdmobNativeAd() {
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_NATIVE_TRANSLATOR, resources.getString(R.string.NATIVE_DEFAULT))
        } else {
            resources.getString(R.string.NATIVE_DEFAULT)
        }
        NewNativeAdClass.checkAdRequestAdmob(
            mContext = requireActivity(),
            fragmentName = "ConversationFragment",
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
        if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("ConversationFragment")) {
            val collapsibleAdView: AdView? =
                NativeMaster.collapsibleBannerAdMobHashMap!!["ConversationFragment"]
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
                    NativeMaster.collapsibleBannerAdMobHashMap!!["ConversationFragment"] = adView
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


    private fun setFromLanguage() {
        fromCountryName = mSharedPreferences.getString("fromlangconvo", "English (En)").toString()
        fromCode = getFromLanguageCode(fromCountryName)
        // Extract text before '(' and remove trailing spaces
        binding.tvfromcountryname.text = fromCountryName.substringBefore("(").trim()
        setFlagFromLanguageCode(fromCountryName)
    }

    private fun setFromLanguage(country: CountryModel) {
        fromCountryName = country.countryName
        mSharedPreferences.edit().putString("fromlangconvo", fromCountryName).apply()
        fromCode = getFromLanguageCode(fromCountryName)
        // Extract text before '(' and remove trailing spaces
        binding.tvfromcountryname.text = fromCountryName.substringBefore("(").trim()
        setFlagFromLanguageCode(fromCountryName)
    }

    private fun setToLanguage() {
        toCountryName = mSharedPreferences.getString("tolangconvo", "Sindhi (سنڌي)").toString()
        toCode = getFromLanguageCode(toCountryName)
        // Extract text before '(' and remove trailing spaces
        binding.tvtocountryname.text = toCountryName.substringBefore("(").trim()
        setFlagToLanguageCode(toCountryName)
    }

    private fun setToLanguage(country: CountryModel) {
        toCountryName = country.countryName
        mSharedPreferences.edit().putString("tolangconvo", toCountryName).apply()
        toCode = getFromLanguageCode(toCountryName)
        // Extract text before '(' and remove trailing spaces
        binding.tvtocountryname.text = toCountryName.substringBefore("(").trim()
        setFlagToLanguageCode(toCountryName)
    }

    private fun setFlagToLanguageCode(code: String) {
        var flagID: Int
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                flagID = AllCountryListMe.getCountryList()[i].arImage
                android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
                    .putInt("toflagTT", flagID).apply()
                toCountryFlag = flagID
                toCountryFlag.let {
                    binding.tocountryflag.background =
                        AppCompatResources.getDrawable(requireActivity(), toCountryFlag)
                }
            }
        }
    }

    private fun fadeFabViewButton() {
        Handler().postDelayed({
            binding.fabConstraint.alpha = 0.3f
        }, 3000)
    }

    private fun handleTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (isExpanded) {
                val outRect = Rect()
                binding.fabConstraint.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    shrinkFab()
                }
            }
        }
        return false
    }

    private fun myBackPressed() {
        Log.d("NavDebug", "myBackPressed() triggered")

        autoSpeakSwitch.let {
            it?.visibility = View.INVISIBLE
        }

        if (navController != null) {
            // 1. Check current destination
            Log.d("NavDebug", "Current Destination before pop: ${navController?.currentDestination?.label} (${navController?.currentDestination?.id})")

            // 2. Try to find navTranslator in the back stack
            try {
                val backStackEntry = navController?.getBackStackEntry(R.id.translationFragment)
                Log.d("NavDebug", "navTranslator found in back stack! Entry: $backStackEntry")
            } catch (e: Exception) {
                Log.e("NavDebug", "CRITICAL: navTranslator is NOT in the back stack! Exception: ${e.message}")
            }

            // 3. Attempt the pop and log the boolean result
            val popSuccess = navController?.popBackStack(R.id.translationFragment, false)
            Log.d("NavDebug", "popBackStack(R.id.navTranslator, false) returned: $popSuccess")

            // 4. Fallback if the specific pop fails
            if (popSuccess == false) {
                Log.d("NavDebug", "Since specific pop failed, attempting generic popBackStack()")
                val genericPop = navController?.popBackStack()
                Log.d("NavDebug", "generic popBackStack() returned: $genericPop")
            }

        } else {
            Log.e("NavDebug", "navController is NULL!")
        }
    }

    private fun showSaveDialog() {
        val saveDialog = Dialog(requireContext())
        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        saveDialog.setCancelable(false)
        saveDialog.setContentView(R.layout.conversation_name_dialog)
        saveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = saveDialog.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnSave = saveDialog.findViewById<AppCompatButton>(R.id.btnSave)
        val etConvName = saveDialog.findViewById<AppCompatEditText>(R.id.etConvName)

        if (conversationName != "New1") {
            val parts = conversationName.split(getString(R.string.splitPattern))
            etConvName.setText(parts[0])
            etConvName.isEnabled = false
        }

        btnSave.blockingClickListener {
            if (etConvName.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please must type conversation name", Toast.LENGTH_SHORT).show()
                // DO NOT dismiss or perform save if text is blank!
            } else {
                performSave()

                if (isExpanded) {
                    shrinkFab()
                }
                // Only dismiss the dialog when validation passes and save executes
                saveDialog.dismiss()
            }
        }

        btnCancel.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            }
            saveDialog.dismiss()
        }

        etConvName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                conversationName = s.toString()
            }
        })

        if (isAdded) {
            saveDialog.show()
        }
    }

//    private fun showSaveDialog() {
//        val saveDialog = Dialog(requireContext())
//        saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        saveDialog.setCancelable(false)
//        saveDialog.setContentView(R.layout.conversation_name_dialog)
//        saveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        val btnCancel = saveDialog.findViewById<AppCompatButton>(R.id.btnCancel)
//        val btnSave = saveDialog.findViewById<AppCompatButton>(R.id.btnSave)
//        val etConvName = saveDialog.findViewById<AppCompatEditText>(R.id.etConvName)
//
//        if (conversationName != "New1") {
//            val parts = conversationName.split(getString(R.string.splitPattern))
//            etConvName.setText(parts[0])
//            etConvName.isEnabled = false
//        }
//
//        btnSave.blockingClickListener {
//            if (etConvName.text.isNullOrBlank()){
//                Toast.makeText(requireContext(), "Please must type conversation name", Toast.LENGTH_SHORT).show()
//            }
//            else{
//                performSave()
//            }
//            if (isExpanded) {
//                shrinkFab()
//            }
//
//            saveDialog.dismiss()
//        }
//
//        btnCancel.blockingClickListener {
//            if (isExpanded) {
//                shrinkFab()
//            }
//            saveDialog.dismiss()
//        }
//
//        etConvName.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable?) {
//                conversationName = s.toString()
//            }
//        })
//
//        if (isAdded) {
//            saveDialog.show()
//        }
//    }

    private fun showClearDialog() {
        val clearDialog = Dialog(requireContext())
        clearDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        clearDialog.setCancelable(false)
        // Inflate the dedicated Clear XML
        clearDialog.setContentView(R.layout.dialog_clear)
        clearDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = clearDialog.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnYes = clearDialog.findViewById<AppCompatButton>(R.id.btnYes)

        btnYes.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            }
            performClear()
            clearDialog.dismiss()
        }

        btnCancel.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            }
            clearDialog.dismiss()
        }

        if (isAdded) {
            clearDialog.show()
        }
    }

    private fun performSave() {
        Handler(Looper.getMainLooper()).postDelayed({
            showSave = false
            val curTime = System.currentTimeMillis()
            conversationExtension.forEach { conversationExt ->
                if (!conversationExt.conversationName.contains(getString(R.string.splitPattern))) {
                    conversationExt.conversationName =
                        this.conversationName + resources.getString(R.string.splitPattern) + curTime
                }
                viewModel.insertConversationReFormulated(conversationExt)
            }

            if (fromlanguageflag != 0 && tolanguageflag != 0) {
                viewModel.deleteFromTemporaryDB()
                conversationName = "New1"
            }

            conversationExtension.clear()
            conversationExtension = ArrayList()

            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(context, "Conversation Saved", Toast.LENGTH_SHORT).show()
                binding.fabConstraint.visibility = View.GONE
            }, 500)
        }, 100)
    }

    private fun performClear() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (fromlanguageflag != 0 && tolanguageflag != 0) {
                binding.fabConstraint.visibility = View.GONE
                binding.emptyConversation.visibility = View.VISIBLE
                adapter.clear()
                viewModel.deleteFromTemporaryDB()
                conversationExtension.clear()
                conversationExtension = ArrayList()
            }
        }, 100)
    }

    private fun dialogSaveClear(title: String) {
        Handler().postDelayed({
            if (title.startsWith("Save")) {
                showSave = false
                val curTime = System.currentTimeMillis()
                conversationExtension.forEach { conversationExt ->
                    if (!conversationExt.conversationName.contains(getString(R.string.splitPattern))) {
                        conversationExt.conversationName =
                            this.conversationName + resources.getString(R.string.splitPattern) + curTime
                    }
                    viewModel.insertConversationReFormulated(conversationExt)
                }

                if (fromlanguageflag != 0 && tolanguageflag != 0) {
                    viewModel.deleteFromTemporaryDB()
                    conversationName = "New1"
                }

                conversationExtension.clear()
                conversationExtension = ArrayList()
                Handler().postDelayed({
                    Toast.makeText(context, "Conversation Saved", Toast.LENGTH_SHORT).show()
                    binding.fabConstraint.visibility = View.GONE
                }, 500)
            } else if (title.startsWith("Clear")) {
                if (fromlanguageflag != 0 && tolanguageflag != 0) {
                    binding.fabConstraint.visibility = View.GONE
                    binding.emptyConversation.visibility = View.VISIBLE
                    adapter.clear()
                    viewModel.deleteFromTemporaryDB()
                    conversationExtension.clear()
                    conversationExtension = ArrayList()
                }
            }
        }, 100)
    }

    fun getInstance(): ConversationFragment? {
        return instance
    }

    override fun onDestroy() {
        job.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onDestroyView() {
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
        super.onDestroyView()
    }

    private fun askForPermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun getCodeForSpeechRecognizer(countryName: String): String {
        val languageCode: String?
        for (i in AllCountryListMe.getCountryList().indices) {
            if (countryName == AllCountryListMe.getCountryList()[i].countryName) {
                languageCode = AllCountryListMe.getCountryList()[i].codeForSpeechRecognizer
                return languageCode
            }
        }
        return "en"
    }

    private fun getFromLanguageCode(code: String): String {
        val languageCode: String?
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                languageCode = AllCountryListMe.getCountryList()[i].code
                return languageCode
            }
        }
        return "en"
    }

    private fun getCountryModel(code: String): CountryModel {
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                return AllCountryListMe.getCountryList()[i]
            }
        }
        return AllCountryListMe.getCountryList()[0]
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val result: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                sourceText = Objects.requireNonNull(result)[0]
                if (btnFrom) {
                    translateTextFrom(fromCode, toCode, sourceText)
                    btnFrom = false
                } else if (btnTo) {
                    translateTextTo(toCode, fromCode, sourceText)
                    btnFrom = false
                }
                showSave = true
                showDelete = true
            }
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun translateTextFrom(
        fomlanguagecode: String,
        tolanguagecode: String,
        source: String?
    ) {
        if (!isInternetAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                "No internet connection. Please check your network settings.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        CoroutineScope(job).launch(Dispatchers.Main) {
            var translatedString: String?
            withContext(Dispatchers.IO) {
                translatedString = TranslatorCall.callUrlAndParseResult(
                    source!!,
                    fomlanguagecode,
                    tolanguagecode
                )
            }

            if (!translatedString.equals(null)) {
                convExtTempForUpdatingAdapter.clear()
                binding.animationView.visibility = View.GONE
                binding.animationView.cancelAnimation()
                if (autoSpeakIsOn) {
                    tts?.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null, "")
                }
                val time = System.currentTimeMillis()

                val convoExtentionPermanent = ConversationExtension(
                    conversationName = conversationName,
                    source = source!!,
                    translation = translatedString!!,
                    from = "from",
                    fromLang = fromCountryName,
                    toLang = toCountryName,
                    isFavorite = isFavorite,
                    time = time
                )

                val convoExtentionTemp = ConversationExtensionTemp(
                    conversationName = conversationName,
                    source = source,
                    translation = translatedString!!,
                    from = "from",
                    fromLang = fromCountryName,
                    toLang = toCountryName,
                    isFavorite = isFavorite,
                    time = time
                )

                conversationExtension.add(convoExtentionPermanent)
                convExtTempForUpdatingAdapter.add(convoExtentionPermanent)
                if (fromlanguageflag != 0 && tolanguageflag != 0) {
                    viewModel.insertConvoTemp(convoExtentionTemp)
                }
                addToAdapter()
            }
        }
    }

    private fun translateTextTo(fomlanguagecode: String, tolanguagecode: String, source: String) {
        if (!isInternetAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                "No internet connection. Please check your network settings.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        CoroutineScope(job).launch(Dispatchers.Main) {
            var translatedString: String?
            withContext(Dispatchers.IO) {
                translatedString = TranslatorCall.callUrlAndParseResult(
                    source,
                    fomlanguagecode,
                    tolanguagecode,
                )
            }
            if (!translatedString.equals(null)) {
                convExtTempForUpdatingAdapter.clear()
                binding.animationView.visibility = View.GONE
                binding.animationView.cancelAnimation()
                if (autoSpeakIsOn) {
                    tts?.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null, "")
                }
                val time = System.currentTimeMillis()

                val convoExtentionPermanent = ConversationExtension(
                    conversationName = conversationName,
                    source = source,
                    translation = translatedString!!,
                    from = "to",
                    fromLang = fromCountryName,
                    toLang = toCountryName,
                    isFavorite = isFavorite,
                    time = time
                )

                val convoExtentionTemp = ConversationExtensionTemp(
                    conversationName = conversationName,
                    source = source,
                    translation = translatedString!!,
                    from = "to",
                    fromLang = fromCountryName,
                    toLang = toCountryName,
                    isFavorite = isFavorite,
                    time = time
                )
                conversationExtension.add(convoExtentionPermanent)
                convExtTempForUpdatingAdapter.add(convoExtentionPermanent)
                if (fromlanguageflag != 0 && tolanguageflag != 0) {
                    viewModel.insertConvoTemp(convoExtentionTemp)
                }
                addToAdapter()
            }
        }
    }

    private fun addToAdapter() {
        if (fromlanguageflag == 0 && tolanguageflag == 0) {
            convExtTempForUpdatingAdapter.forEach {
                if (it.from == "from") {
                    tts?.let { it1 ->
                        ConversationFromItem(
                            it.translation,
                            it.source,
                            it1,
                            requireActivity()
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(
                            it.translation,
                            it.source,
                            it1,
                            requireActivity()
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }
            }
            binding.fabConstraint.visibility = View.VISIBLE
            binding.emptyConversation.visibility = View.GONE
            binding.rvConversation.smoothScrollToPosition(adapter.itemCount - 1)
        } else {
            adapter.clear()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localCode = "ur_PK"
            val locale = Locale(localCode)
            val result = tts!!.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            }
        }
    }

    private fun shrinkFab() {
        binding.mainFabBtn.startAnimation(rotateAntiClockWiseFabAnim)
        binding.imgClear.startAnimation(toBottomFabAnim)
//        binding.txtDelete.startAnimation(toBottomFabAnim)
        binding.imgSave.startAnimation(toBottomFabAnim)
//        binding.txtSave.startAnimation(toBottomFabAnim)
        isExpanded = !isExpanded
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
        fadeFabViewButton()
    }

    private fun expandFab() {
        binding.fabConstraint.alpha = 1f
        binding.mainFabBtn.startAnimation(rotateClockWiseFabAnim)
        binding.imgSave.startAnimation(fromBottomFabAnim)
//        binding.txtSave.startAnimation(fromBottomFabAnim)
        binding.imgClear.startAnimation(fromBottomFabAnim)
//        binding.txtDelete.startAnimation(fromBottomFabAnim)
        isExpanded = !isExpanded
    }
}

class ConversationFromItem(
    val from: String,
    private val fromText: String,
    val tts: TextToSpeech,
    val activity: Activity
) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tvChatFrom = viewHolder.itemView.findViewById<TextView>(R.id.tvChatFrom)
        val tvChatFrom2 = viewHolder.itemView.findViewById<TextView>(R.id.tvChatFrom2)
        tvChatFrom.text = fromText
        tvChatFrom2.text = from

        viewHolder.itemView.findViewById<ImageView>(R.id.ivCopy).blockingClickListener {
            copyToClipBoard(activity, from)
        }
    }

    override fun getLayout(): Int {
        return R.layout.conversation_from_row
    }
}

class ConversationToItem(
    val to: String,
    val fromText: String,
    val tts: TextToSpeech,
    val activity: Activity
) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tvChatTo = viewHolder.itemView.findViewById<TextView>(R.id.tvChatTo)
        val tvChatTo2 = viewHolder.itemView.findViewById<TextView>(R.id.tvChatTo2)
        tvChatTo.text = fromText
        tvChatTo2.text = to

        viewHolder.itemView.findViewById<ImageView>(R.id.ivCopy).blockingClickListener {
            copyToClipBoard(activity, to)
        }
    }

    override fun getLayout(): Int {
        return R.layout.conversation_to_row
    }
}

fun copyToClipBoard(activity: Activity, text: String) {
    val clipboard: ClipboardManager =
        activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}