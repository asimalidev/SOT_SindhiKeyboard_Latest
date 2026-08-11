package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.drawable.Drawable
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentHistoryFragmentBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.OVERALL_BANNER_RELOADING
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_HISTORY_TO_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.Util.fromConversation
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.ViewModelFactory
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate

class history_fragment : Fragment(), HistoryConversationAdapter.HistoryConversationClickListener {
    private lateinit var binding: FragmentHistoryFragmentBinding
    var navController: NavController? = null
    private lateinit var viewModel: HistoryViewModel
    private lateinit var wordsHistoryAdapter: HistoryAdapter
    lateinit var conversationHistoryAdapter: HistoryConversationAdapter
    lateinit var conversationExtensionAdapterList: ArrayList<ConversationExtension>
    companion object { lateinit var conversationExtensionForwardList: ArrayList<ConversationExtension> }

    var conversationMapParent: Map<String, List<ConversationExtension>> = mutableMapOf()

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.activitiesFragmentEvent(requireActivity(), "history_scr")
        conversationExtensionAdapterList = ArrayList()
        conversationExtensionForwardList = ArrayList()

        // REMOVED: Initial Ad Loading Logic has been moved to checkAndLoadAds()

        wordsHistoryAdapter = HistoryAdapter()
        binding.rvWordHistory.apply { layoutManager = LinearLayoutManager(requireContext()) }
        binding.rvWordHistory.adapter = wordsHistoryAdapter

        conversationHistoryAdapter = HistoryConversationAdapter()
        conversationHistoryAdapter.historyConversationClickListener = this
        binding.rvConversation.apply { layoutManager = LinearLayoutManager(requireContext()) }
        binding.rvConversation.adapter = conversationHistoryAdapter

        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application))[HistoryViewModel::class.java]

        viewModel.conversationExtReFormulated.observe(viewLifecycleOwner) { conversationList ->
            conversationList?.let {
                conversationExtensionAdapterList.clear()
                val conversationMap = conversationList.groupBy { it.conversationName }
                conversationMapParent = conversationMap

                for ((key, value) in conversationMap) {
                    for (conversation in value) {
                        val parts = conversation.conversationName.split(getString(R.string.splitPattern))
                        conversation.conversationName = parts[0]
                    }
                    conversationExtensionAdapterList.add(value.last())
                }

                // NEW: Ad Logic Condition
                if (conversationExtensionAdapterList.isNotEmpty()) {
                    conversationHistoryAdapter.setHistoryList(conversationExtensionAdapterList)
                    // The list is not empty, load and show ads
                    checkAndLoadAds()
                } else {
                    // The list is empty, hide all ad containers and stop shimmers
                    binding.adViewContainer.visibility = View.GONE
                    binding.shimmerLayoutBanner.stopShimmer()
                    binding.shimmerLayoutBanner.visibility = View.GONE

                    binding.nativeAdContainerAd.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                }
            }

            // UI visibility update
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController != null) {
                    findNavController().popBackStack()
                } else {
                    isNavControllerAdded()
                }
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
    }

    private fun checkAndLoadAds() {
        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(SHOW_BANNER_HISTORY, "ON").equals("ON", true)
        ) {
            binding.adViewContainer.visibility = View.VISIBLE
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("History")) {
                val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["History"]
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
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }

        // Native Ad Logic
        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(SHOW_NATIVE_HISTORY, "ON").equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            val pref = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
            val adId = if (!BuildConfig.DEBUG) {
                pref.getString(AD_ID_NATIVE_TRANSLATOR, resources.getString(R.string.NATIVE_DEFAULT))
            } else {
                resources.getString(R.string.NATIVE_DEFAULT)
            }
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = requireActivity(),
                adId = adId!!,
                fragmentName = "HistoryFragment",
                isMedia = true,
                isMediaOnLeft = true,
                adContainer = binding.nativeAdContainerAd,
                isMediumAd = false,
                onFailed = { binding.nativeAdContainerAd.visibility = View.GONE },
                onAddLoaded = {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                })
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
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
                    NativeMaster.collapsibleBannerAdMobHashMap!!["History"] = adView
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

    override fun onResume() {
        super.onResume()

        isNavControllerAdded()
        requireActivity().findViewById<ImageView>(R.id.ivClose)?.visibility = View.INVISIBLE

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = resources.getString(R.string.label_history)
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
                            navController?.popBackStack()
                        } catch (e: Exception) {
                            Log.e("Navigation", "Failed to navigate: ${e.message}")
                            // Fallback navigation or error handling
                            try {
                                navController?.popBackStack()
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
    }


    private fun changeStateTranslation() {
        if (wordsHistoryAdapter.itemCount > 0) {
            binding.imgNoWordHistory.visibility = View.GONE
        } else {
            binding.imgNoWordHistory.visibility = View.VISIBLE
        }

        binding.rvConversation.visibility = View.GONE
        binding.rvWordHistory.visibility = View.VISIBLE

        val colorTranslation = resources.getColor(R.color.white)
        val colorConversation = resources.getColor(R.color.blackTxt)

        if (binding.rvConversation.visibility != View.VISIBLE) {
            binding.imgNoConversationHistory.visibility = View.GONE
        }

        val drawableTranslation: Drawable? = binding.fragmentTranslationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableTranslation!!, colorTranslation)
        binding.fragmentTranslationBtn.setBackgroundColor(resources.getColor(R.color.appThemeColor))
        binding.fragmentTranslationBtn.setTextColor(colorTranslation)
        binding.fragmentTranslationBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableTranslation, null, null)

        val drawableConversation: Drawable? = binding.fragmentConversationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableConversation!!, colorConversation)
        binding.fragmentConversationBtn.setBackgroundColor(resources.getColor(R.color.tabBg))
        binding.fragmentConversationBtn.setTextColor(colorConversation)
        binding.fragmentConversationBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableConversation, null, null)
    }

    private fun changeStateConversation() {
        if (conversationExtensionAdapterList.size > 0) {
            binding.imgNoConversationHistory.visibility = View.GONE
        } else {
            binding.imgNoConversationHistory.visibility = View.VISIBLE
        }

        binding.rvWordHistory.visibility = View.GONE
        binding.rvConversation.visibility = View.VISIBLE

        val colorTranslation = resources.getColor(R.color.blackTxt)
        val colorConversation = resources.getColor(R.color.white)

        if (binding.rvWordHistory.visibility != View.VISIBLE) {
            binding.imgNoWordHistory.visibility = View.GONE
        }

        val drawableTranslation: Drawable? = binding.fragmentTranslationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableTranslation!!, colorTranslation)
        binding.fragmentTranslationBtn.setBackgroundColor(resources.getColor(R.color.tabBg))
        binding.fragmentTranslationBtn.setTextColor(colorTranslation)

        val drawableConversation: Drawable? = binding.fragmentConversationBtn.compoundDrawables[1]
        DrawableCompat.setTint(drawableConversation!!, colorConversation)
        binding.fragmentConversationBtn.setBackgroundColor(resources.getColor(R.color.appThemeColor))
        binding.fragmentConversationBtn.setTextColor(colorConversation)
    }

    override fun onRootViewClick(position: Int) {
        if (NetworkCheck.isNetworkAvailable(requireActivity())
            && !requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED,false)
            && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                SHOW_INTER_HISTORY_TO_CONVERSATION ,"ON").equals("ON",true)) {
            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                context = requireActivity(),
                nameFragment = "HistoryFragment",
                onAdClosedCallBackAdmob = {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onRootViewClicked(pos = position)
                    },300)
                },
                onAdShowedCallBackAdmob = {})
        } else {
            onRootViewClicked(pos = position)
        }
    }

    override fun onFavoriteClick(position: Int) {
        Toast.makeText(context, "Fav$position", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(context, "Deleted :: ", Toast.LENGTH_SHORT).show()
    }

    private fun onRootViewClicked(pos: Int) {
        if (navController != null) {
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

            val action = history_fragmentDirections.actionHistoryFragmentToConversationFragment(
                conversationExtensionForwardList.last().fromLang,
                0,
                conversationExtensionForwardList.last().toLang,
                0
            )
            navController?.safeNavigate(action)
        }
    }
}