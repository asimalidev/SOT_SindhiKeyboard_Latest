package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
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
import com.sindhi.urdu.english.keybad.databinding.FragmentHistoryFragmentBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.Util.fromConversation
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.viewmodelfactory
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADS_BANNER_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.INTER_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG

class history_fragment : Fragment(), HistoryConversationAdapter.HistoryConversationClickListener {

    private var _binding: FragmentHistoryFragmentBinding? = null
    lateinit var navController: NavController
    private val binding get() = _binding!!
    lateinit var viewModel: HistoryViewModel
    private val handler = Handler(Looper.getMainLooper())
    lateinit var wordsHistoryAdapter: HistoryAdapter
    lateinit var conversationHistoryAdapter: HistoryConversationAdapter
    lateinit var conversationExtensionAdapterList: ArrayList<ConversationExtension>

    companion object {
        lateinit var conversationExtensionForwardList: ArrayList<ConversationExtension>
    }

    var bundle = Bundle()

    var conversationMapParent: Map<String, List<ConversationExtension>> = mutableMapOf()

    //    var isPurchased: Boolean? = null
    var isPurchase = false

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
        _binding = FragmentHistoryFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true
        checkForLoadBanner()
        bundle.putString("HistoryFragment", "HistoryFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_history", bundle)
        conversationExtensionAdapterList = ArrayList()
        conversationExtensionForwardList = ArrayList()

        wordsHistoryAdapter = HistoryAdapter()
        binding.rvWordHistory.apply { layoutManager = LinearLayoutManager(requireContext()) }
        binding.rvWordHistory.adapter = wordsHistoryAdapter

        conversationHistoryAdapter = HistoryConversationAdapter()
        conversationHistoryAdapter.historyConversationClickListener = this
        binding.rvConversation.apply { layoutManager = LinearLayoutManager(requireContext()) }
        binding.rvConversation.adapter = conversationHistoryAdapter



        viewModel = ViewModelProvider(
            this,
            viewmodelfactory(requireActivity().application)
        )[HistoryViewModel::class.java]

        viewModel.allWords.observe(viewLifecycleOwner) { list ->
            list?.let {
                if (it.isEmpty()) {
                    /*binding.nativeAdContainerAd.visibility=View.GONE*/
                } else {
                    wordsHistoryAdapter.setHistoryList(it)
                }
            }
            handler.postDelayed({
                val b = _binding ?: return@postDelayed  // exits if null
                if (wordsHistoryAdapter.itemCount > 0) {
                    b.imgNoWordHistory.visibility = View.GONE
                } else {
                    b.imgNoWordHistory.visibility = View.VISIBLE
                }
                b.imgNoConversationHistory.visibility = View.GONE
            }, 100)
        }

        viewModel.ConversationExtReFormulated.observe(viewLifecycleOwner) { conversationList ->
            conversationList?.let {
                conversationExtensionAdapterList.clear()
                val conversationMap = conversationList.groupBy { it.conversationName }
                conversationMapParent = conversationMap
                for ((key, value) in conversationMap) {
                    for (conversation in value) {
                        val parts =
                            conversation.conversationName.split(getString(R.string.splitPattern))
                        conversation.conversationName = parts[0]
                    }
                    conversationExtensionAdapterList.add(value[value.size - 1])
                }

                if (conversationExtensionAdapterList.isEmpty()) {

                } else {
                    conversationHistoryAdapter.setHistoryList(conversationExtensionAdapterList)
                }
            }
            Handler().postDelayed({
                if (binding.rvConversation.visibility == View.VISIBLE) {
                    if (conversationExtensionAdapterList.size > 0) {
                        binding.imgNoConversationHistory.visibility = View.GONE
                    } else {
                        binding.imgNoConversationHistory.visibility = View.VISIBLE
                    }
                    binding.imgNoWordHistory.visibility = View.GONE
                }
            }, 100)
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        history_fragmentDirections.actionHistoryFragmentToTranslationFragment()
                    navController.navigate(action)
                }
            })
        binding.fragmentTranslationBtn.blockingClickListener {
            changeStateTranslation()
        }

        binding.fragmentConversationBtn.blockingClickListener {
            changeStateConversation()
        }

        if (fromConversation) {
            changeStateConversation()
            fromConversation = false
            if (binding.rvWordHistory.visibility != View.VISIBLE) {
                binding.imgNoWordHistory.visibility = View.GONE
            }
        } else {
            if (binding.rvConversation.visibility != View.VISIBLE) {
                binding.imgNoConversationHistory.visibility = View.GONE
            }
        }
        return root
    }

    private fun checkForLoadBanner() {
        if (isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                ADS_BANNER_HISTORY, "ON"
            ).equals("ON", true)
            && !isPurchase
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("history")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["history"]
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
        val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(ADMOB_BANNER_INSIDE, "ca-app-pub-3747520410546258/1697692330")
        } else {
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        Log.d("jdjasjjsa", "loading: ")

        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                NativeMaster.collapsibleBannerAdMobHashMap!!["history"] = adView
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

    private fun changeStateTranslation() {
        if (wordsHistoryAdapter.itemCount > 0) {
            binding.imgNoWordHistory.visibility = View.GONE
        } else {
            binding.imgNoWordHistory.visibility = View.VISIBLE
        }

        binding.rvConversation.visibility = View.GONE
        binding.rvWordHistory.visibility = View.VISIBLE

        val colorTranslation = resources.getColor(R.color.White)
        val colorConversation = resources.getColor(R.color.tabTextDull)

        if (binding.rvConversation.visibility != View.VISIBLE) {
            binding.imgNoConversationHistory.visibility = View.GONE
        }

        val drawableTranslation: Drawable? = binding.fragmentTranslationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableTranslation!!, colorTranslation)
        binding.fragmentTranslationBtn.setBackgroundColor(resources.getColor(R.color.maroon_500))
        binding.fragmentTranslationBtn.setTextColor(colorTranslation)
        binding.fragmentTranslationBtn.setCompoundDrawablesWithIntrinsicBounds(
            null,
            drawableTranslation,
            null,
            null
        )

        val drawableConversation: Drawable? = binding.fragmentConversationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableConversation!!, colorConversation)
        binding.fragmentConversationBtn.setBackgroundColor(resources.getColor(R.color.tabBg))
        binding.fragmentConversationBtn.setTextColor(colorConversation)
        binding.fragmentConversationBtn.setCompoundDrawablesWithIntrinsicBounds(
            null,
            drawableConversation,
            null,
            null
        )
    }

    private fun changeStateConversation() {
        if (conversationExtensionAdapterList.size > 0) {
            binding.imgNoConversationHistory.visibility = View.GONE
        } else {
            binding.imgNoConversationHistory.visibility = View.VISIBLE
        }

        binding.rvWordHistory.visibility = View.GONE
        binding.rvConversation.visibility = View.VISIBLE

        val colorTranslation = resources.getColor(R.color.tabTextDull)
        val colorConversation = resources.getColor(R.color.White)

        if (binding.rvWordHistory.visibility != View.VISIBLE) {
            binding.imgNoWordHistory.visibility = View.GONE
        }

        val drawableTranslation: Drawable? = binding.fragmentTranslationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableTranslation!!, colorTranslation)
        binding.fragmentTranslationBtn.setBackgroundColor(resources.getColor(R.color.tabBg))
        binding.fragmentTranslationBtn.setTextColor(colorTranslation)

        val drawableConversation: Drawable? = binding.fragmentConversationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableConversation!!, colorConversation)
        binding.fragmentConversationBtn.setBackgroundColor(resources.getColor(R.color.maroon_500))
        binding.fragmentConversationBtn.setTextColor(colorConversation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // clear all pending posts
        _binding = null
    }

    override fun onRootViewClick(position: Int) {
        if (NetworkCheck.isNetworkAvailable(requireContext())
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getString(Preferences.INTERSTITIAL_HISTORY_TO_CONVERSATION, "ON")
                .equals("ON", true)
        ) {
            if (isPurchase) {
                onRootViewClicked(pos = position)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    onRootViewClicked(pos = position)
                }, 1200)
                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                    requireActivity(),
                    "history_fragment",
                    onAdClosedCallBackAdmob = {},
                    onAdShowedCallBackAdmob = {})
            }
        } else {
            onRootViewClicked(pos = position)
        }
    }

    override fun onFavoriteClick(position: Int) {
        context?.let { safeContext ->
            Toast.makeText(safeContext, "Fav$position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteClick(position: Int) {
        val keyAtIndex: String? = conversationMapParent.keys.elementAtOrNull(position)
        if (conversationExtensionAdapterList.isNotEmpty()) {
            if (conversationExtensionAdapterList[position].conversationName != "") {
                viewModel.deleteSelectedConversationHistory(keyAtIndex!!)
                conversationMapParent = conversationMapParent.filterKeys { it != keyAtIndex }
                conversationExtensionAdapterList.removeAt(position)
                conversationHistoryAdapter.setHistoryList(conversationExtensionAdapterList)
            }
        }
        context?.let { safeContext ->
            Toast.makeText(safeContext, "Deleted :: ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRootViewClicked(pos: Int) {
        if (!isAdded || isDetached) return // Safety check

        fromConversation = true
        conversationExtensionForwardList.clear()
        val keyAtIndex: String? = conversationMapParent.keys.elementAtOrNull(pos)

        for ((key, value) in conversationMapParent) {
            if (key == keyAtIndex) {
                for (conversation in value) {
                    conversation.conversationName = key
                    conversationExtensionForwardList.add(conversation)
                }
                break
            }
        }

        // Add safety check before navigation
        if (isAdded && findNavController().currentDestination?.id == R.id.history_fragment) {
            val action = history_fragmentDirections.actionHistoryFragmentToConversationFragment(
                conversationExtensionForwardList.last().fromLang,
                0,
                conversationExtensionForwardList.last().toLang,
                0
            )
            findNavController().navigate(action)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        if (!isAdded || isDetached) return
        isNavControllerAdded()
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonConversation)
            .let { it?.visibility = View.INVISIBLE }
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonTTS)
            .let { it?.visibility = View.INVISIBLE }

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = resources.getString(R.string.label_history)

            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx

            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        val action =
                            history_fragmentDirections.actionHistoryFragmentToTranslationFragment()
                        navController.navigate(action)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        if (isPurchase) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (isNetworkAvailable(requireContext()) && !isPurchase &&
                requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(Preferences.ADS_NATIVE_HISTORY, "ON").equals("ON", true)
            ) {
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(INTER_OVER_ALL, "ca-app-pub-3747520410546258/1702944653")
                } else {
                    resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,//getString(R.string.NATIVE_INSIDE_BIDDING),
                    fragmentName = "OverallTranslationConvo",
                    isMedia = true,
                    isMediaOnLeft = true,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = true,
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

}