package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.view.*
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
import androidx.preference.PreferenceManager
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
import com.sindhi.newvoicetyping.ui.Speechtotext.CountryCountry
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentConversationBinding
import com.sindhi.urdu.english.keybad.databinding.FragmentHomeBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.SelectCountryDialog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountryListMe
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.TranslatorCall
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.history_fragment.Companion.conversationExtensionForwardList
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtensionTemp
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.viewmodelfactory
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.coroutines.*
import java.util.*

class ConversationFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var binding: FragmentConversationBinding
    var fromlanguageflag: Int = 0
    lateinit var viewModel: HistoryViewModel
    var tolanguageflag: Int = 0
    var isPurchased: Boolean? = null
    lateinit var navController: NavController
    lateinit var fromCode: String
    lateinit var toCode: String
    lateinit var sourcetext: String
    var btnFrom = false

    var btnTo = false
    val job = Job()
    lateinit var adapter: GroupAdapter<GroupieViewHolder>
    private var tts: TextToSpeech? = null
    private var instance: ConversationFragment? = null
    val args: ConversationFragmentArgs by navArgs()

    var conversationName = "New1"
    var fromCountryName = ""
    var toCountryName = "String"
    var isFavorite = false
    var conversationExtension: ArrayList<ConversationExtension> = ArrayList()
    var convExtTempForUpdatingAdapter: ArrayList<ConversationExtension> = ArrayList()

    val dialog = SelectCountryDialog()

    private var showSave = false
    private var showDelete = false

    private var isExpanded = false
    lateinit var mSharedPreferences: SharedPreferences
    var fromTo = ""
    var autoSpeakSwitch: SwitchCompat? = null
    var autoSpeakIsOn = true
    var bundle = Bundle()

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.to_bottom_fab)
    }
    private val rotateClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.rotate_clock_wise)
    }
    private val rotateAntiClockWiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.rotate_anti_clock_wise)
    }

    private val delayedActionHandler = Handler()
    private val delayedActionRunnable = Runnable {
        if (isAdded) {
            if (isExpanded) {
                shrinkFab()
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


    private fun startDelayedAction() {
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
        delayedActionHandler.postDelayed(delayedActionRunnable, 5000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
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

        if (isAdded) {
            navController = findNavController()
        }

        bundle.putString("ConversationFragment", "ConversationFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_conversation", bundle)

        mSharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        dialog.setCountrySelectionListener(object : SelectCountryDialog.CountrySelectionListener {
            override fun onCountrySelected(country: CountryCountry) {
                activity?.let {
                    Toast.makeText(it, country.countryName, Toast.LENGTH_SHORT).show()
                }

                if (!isAdded) return  // fragment is not attached anymore

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
            viewmodelfactory(requireActivity().application)
        )[HistoryViewModel::class.java]

        tts = TextToSpeech(requireContext(), this, "com.google.android.tts")


        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

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
                        ConversationFromItem(
                            it.translation,
                            it.source,
                            it1
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(
                            it.translation,
                            it.source,
                            it1
                        )
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
            Log.e("TAG", "Checking Size: " + conversationExtension.size)
            if (adapter.itemCount > 0) {
                binding.emptyConversation.visibility = View.GONE
                binding.fabConstraint.visibility = View.VISIBLE
                binding.rvConversation.smoothScrollToPosition(adapter.itemCount - 1)
            } else {
                binding.emptyConversation.visibility = View.VISIBLE
                binding.fabConstraint.visibility = View.INVISIBLE
            }
        }

        viewModel.conversationExtensionPermanent.observe(viewLifecycleOwner) { mainObject ->
            mainObject.forEach {
                if (it.from == "from") {
                    tts?.let { it1 ->
                        ConversationFromItem(
                            it.translation,
                            it.source,
                            it1
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(
                            it.translation,
                            it.source,
                            it1
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
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    myBackPressed()
                }
            })

        binding.btnfrom.blockingClickListener {
            fromTo = "from"
            dialog.show(parentFragmentManager, SelectCountryDialog::class.java.simpleName)
        }

        binding.btnto.blockingClickListener {
            fromTo = "to"
            dialog.show(parentFragmentManager, SelectCountryDialog::class.java.simpleName)
        }

        binding.ivmic1.blockingClickListener {
            askForPermission()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getfromlanguagecode(toCountryName))
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
                    getfromlanguagecode(fromCountryName)
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
            showCustomDialog("Save Conversation")
        }

        binding.imgClear.blockingClickListener {
            showCustomDialog("Clear Conversation")
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonTTS)
            .let { it?.visibility = View.INVISIBLE }
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

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

            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx

            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        myBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        /*if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(Preferences.ADS_NATIVE_CONVERSATION,"ON").equals("ON",true)) {
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = getString(R.string.admob_native_conversation),
                    fragmentName = "OverallTranslationConvo",
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onfailed = {
                        binding.nativeAdContainerAd.visibility = View.GONE
                    },
                    onAddLoaded = {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }*/
        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchased!!
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(ADS_NATIVE_CONVERSATION, "ON").equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !isPurchased!!
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(COLLAPSIBLE_CONVERSATION, "ON").equals("ON", true)
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
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(NATIVE_CONVERSATION, "ca-app-pub-3747520410546258/5450617979")
        } else {
            resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
        }
        NewNativeAdClass.checkAdRequestAdmob(
            mContext = requireActivity(),
            adId = adId!!,//getString(R.string.admob_native),
            fragmentName = "ConversationFragment",
            isMedia = false,
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
            Handler().postDelayed({
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            }, 500)
        } else {
            loadCollapsibleBanner()
        }
    }

    private fun loadCollapsibleBanner() {
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(BANNER_INSIDE, "ca-app-pub-3747520410546258/1697692330")
        } else {
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        adView.adUnitId = adId!!//getString(R.string.admob_banner_inside)
        //adView.adUnitId = getString(R.string.admob_banner_inside)
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
                if (requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_BANNER_RELOADING, "SAVE").equals("SAVE")
                ) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["ConversationFragment"] = adView
                }

                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
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
        fromCode = getfromlanguagecode(fromCountryName)
        binding.tvfromcountryname.text = fromCountryName
    }

    private fun setFromLanguage(country: CountryCountry) {
        fromCountryName = country.countryName
        mSharedPreferences.edit().putString("fromlangconvo", fromCountryName).apply()
        fromCode = getfromlanguagecode(fromCountryName)
        binding.tvfromcountryname.text = fromCountryName
    }

    private fun setToLanguage() {
        toCountryName = mSharedPreferences.getString("tolangconvo", "Sindhi (سنڌي)").toString()
        toCode = getfromlanguagecode(toCountryName)
        binding.tvtocountryname.text = toCountryName
    }

    private fun setToLanguage(country: CountryCountry) {
        toCountryName = country.countryName
        mSharedPreferences.edit().putString("tolangconvo", toCountryName).apply()
        toCode = getfromlanguagecode(toCountryName)
        binding.tvtocountryname.text = toCountryName
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
        autoSpeakSwitch.let {
            it?.visibility = View.INVISIBLE
        }

        if (isExpanded) {
            shrinkFab()
        } else {
            if (fromlanguageflag == 0 && tolanguageflag == 0) {
                val action =
                    ConversationFragmentDirections.actionConversationFragmentToHistoryFragment()
                navController.navigate(action)
            } else {
                val action =
                    ConversationFragmentDirections.actionConversationFragmentToTranslationFragment()
                navController.navigate(action)
            }
        }
    }

    private fun showCustomDialog(title: String) {
        val nameDialog = Dialog(requireContext())
        nameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nameDialog.setCancelable(true)
        nameDialog.setContentView(R.layout.custom_dialog)
        nameDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtTitle = nameDialog.findViewById<AppCompatTextView>(R.id.txtTitle)
        val btnCancel = nameDialog.findViewById<AppCompatButton>(R.id.btnCancel)
        val etConvName = nameDialog.findViewById<AppCompatEditText>(R.id.etConvName)
        val txtClearDes = nameDialog.findViewById<AppCompatTextView>(R.id.txtClearDes)
        val btnSave = nameDialog.findViewById<AppCompatButton>(R.id.btnSave)
        txtTitle.text = title

        if (title.startsWith("Save")) {
            btnSave.text = "Save"
            etConvName.visibility = View.VISIBLE
            txtClearDes.visibility = View.GONE
            if (conversationName != "New1") {
                val parts = conversationName.split(getString(R.string.splitPattern))
                etConvName.setText(parts[0])
                etConvName.isEnabled = false
            }
        } else if (title.startsWith("Clear")) {
            btnSave.text = "Yes"
            txtClearDes.visibility = View.VISIBLE
            etConvName.visibility = View.GONE
        }

        btnSave.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            }
            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(Preferences.INTERSTITIAL_CONVERSATION_SAVE, "ON").equals("ON", true)
            ) {
                if (isPurchased!!) {
                    dialogSaveClear(title = title)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogSaveClear(title = title)
                    }, 1200)
                    InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                        requireActivity(),
                        "ConversationFragment",
                        onAdClosedCallBackAdmob = {},
                        onAdShowedCallBackAdmob = {})
                }
            } else {
                dialogSaveClear(title = title)
            }
            nameDialog.dismiss()
        }

        btnCancel.blockingClickListener {
            if (isExpanded) {
                shrinkFab()
            }
            nameDialog.dismiss()
        }

        etConvName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                conversationName = s.toString()
            }
        })

        if (isAdded) {
            nameDialog.show()
        }
    }

    fun dialogSaveClear(title: String) {
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
        super.onDestroy()
    }

    override fun onDestroyView() {
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
        super.onDestroyView()
    }

    fun askForPermission() {
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

    fun getfromlanguagecode(code: String): String {
        val languageCode: String?
        for (i in AllCountryListMe.getCountryList().indices) {
            if (code == AllCountryListMe.getCountryList()[i].countryName) {
                languageCode = AllCountryListMe.getCountryList()[i].code
                return languageCode
            }
        }
        return "en"
    }

    private fun getCountryModel(code: String): CountryCountry {
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
                sourcetext = Objects.requireNonNull(result)[0]
                if (btnFrom) {
                    translateTextFrom(fromCode, toCode, sourcetext)
                    btnFrom = false
                } else if (btnTo) {
                    translateTextTo(toCode, fromCode, sourcetext)
                    btnFrom = false
                }
                showSave = true
                showDelete = true
            }
        }
    }

    fun translateTextFrom(fomlanguagecode: String, tolanguagecode: String, source: String?) {
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

    fun translateTextTo(fomlanguagecode: String, tolanguagecode: String, source: String) {
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

    fun addToAdapter() {
        if (fromlanguageflag == 0 && tolanguageflag == 0) {
            convExtTempForUpdatingAdapter.forEach {
                if (it.from == "from") {
                    tts?.let { it1 ->
                        ConversationFromItem(
                            it.translation,
                            it.source,
                            it1
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }

                if (it.from == "to") {
                    tts?.let { it1 ->
                        ConversationToItem(
                            it.translation,
                            it.source,
                            it1
                        )
                    }?.let { it2 -> adapter.add(it2) }
                }
            }
            binding.fabConstraint.visibility = View.VISIBLE
            binding.emptyConversation.visibility = View.VISIBLE
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
        binding.txtDelete.startAnimation(toBottomFabAnim)
        binding.imgSave.startAnimation(toBottomFabAnim)
        binding.txtSave.startAnimation(toBottomFabAnim)
        isExpanded = !isExpanded
        delayedActionHandler.removeCallbacks(delayedActionRunnable)
    }

    private fun expandFab() {
        binding.mainFabBtn.startAnimation(rotateClockWiseFabAnim)
        binding.imgSave.startAnimation(fromBottomFabAnim)
        binding.txtSave.startAnimation(fromBottomFabAnim)
        binding.imgClear.startAnimation(fromBottomFabAnim)
        binding.txtDelete.startAnimation(fromBottomFabAnim)
        isExpanded = !isExpanded
    }

}

class ConversationFromItem(val from: String, val fromText: String, val tts: TextToSpeech) :
    Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tvChatFrom = viewHolder.itemView.findViewById<TextView>(R.id.tvChatFrom)
        val tvChatFrom2 = viewHolder.itemView.findViewById<TextView>(R.id.tvChatFrom2)

        tvChatFrom.text = fromText
        tvChatFrom2.text = from
    }

    override fun getLayout(): Int {
        return R.layout.conversation_from_row
    }
}

class ConversationToItem(val to: String, val fromText: String, val tts: TextToSpeech) :
    Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tvChatTo = viewHolder.itemView.findViewById<TextView>(R.id.tvChatTo)
        val tvChatTo2 = viewHolder.itemView.findViewById<TextView>(R.id.tvChatTo2)

        tvChatTo.text = fromText
        tvChatTo2.text = to
    }

    override fun getLayout(): Int {
        return R.layout.conversation_to_row
    }
}