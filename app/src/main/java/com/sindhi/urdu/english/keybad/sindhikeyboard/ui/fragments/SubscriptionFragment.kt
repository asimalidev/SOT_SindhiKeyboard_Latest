package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSubscriptionBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SUBSCRIPTION_EXIT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.privacyPolicy
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.termsOfService
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.unsubscribe
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.SubscriptionAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.BillingManager
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.itemDs
import kotlinx.coroutines.launch

class SubscriptionFragment : Fragment() {
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!
    private lateinit var billingManager: BillingManager
    private lateinit var adapter: SubscriptionAdapter
    // Data Lists
    private val subscriptionsArrayList = ArrayList<itemDs>()
    // Helper list to map UI items back to actual Billing objects
    private val mappedOffers = mutableListOf<Pair<ProductDetails, SubscriptionOfferDetails>>()

    private var selectedListIndex = -1
    private var navController: NavController? = null
    private val args: SubscriptionFragmentArgs by navArgs()
    private var fromSubscription: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Init Data & Tools
        ensureNavController()
        fromSubscription = args.fromName
        billingManager = BillingManager.getInstance(requireContext())
        subscriptionsArrayList.clear()

        // 2. Setup UI
        binding.cvLoaded.visibility = View.VISIBLE
        setupRecyclerView()
        setupClickListeners()
        setupBackPressHandler()

        // 3. Start Observers
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        toggleSystemUI(hide = true)
        toggleActivityViews(isSubscriptionMode = true)
        setupDetailsPopupLogic()
    }

    override fun onStop() {
        super.onStop()
        toggleSystemUI(hide = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toggleSystemUI(hide = false)
        toggleActivityViews(isSubscriptionMode = false)
        _binding = null
    }

    // region --- UI Setup & Listeners ---

    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter { itemDs ->
            // Store the selected index from the list
            selectedListIndex = itemDs.planIndex // Assuming planIndex stores the list position
            PrefHelper(requireContext()).putString("SubscriberPosition", selectedListIndex.toString())

            // --- NEW CODE: Update bottom text on click ---
            if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
                val (_, offerDetails) = mappedOffers[selectedListIndex]
                val basePlanId = offerDetails.basePlanId
                updateSubscriptionUI(basePlanId)
            }
            // ---------------------------------------------
        }
        binding.rvSubscription.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubscription.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { handleBackAction() }

        binding.btnContinue.setOnClickListener {
            initiatePurchase()
        }

        binding.ivDetails.setOnClickListener {
            binding.cvDetails.visibility = View.VISIBLE
        }

        // Legal Links
        binding.apply {
            txtPrivacyPolicy.setOnClickListener { privacyPolicy(requireActivity()) }
            txtUnsubscribe.setOnClickListener { unsubscribe(requireActivity()) }
            txtTermsAndConditions.setOnClickListener { termsOfService(requireActivity()) }
            txtGooglePlay.setOnClickListener { openSubscriptionsLink() }
            txtCancelSubscriptionsOnGoogle.setOnClickListener { openSubscriptionsLink() }
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackAction()
            }
        })
    }

    private fun handleBackAction() {
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClosePurchase)

        if (binding.cvDetails.visibility == View.VISIBLE) {
            binding.cvDetails.visibility = View.GONE
            showCloseBtn()
        } else if (ivClose?.visibility == View.VISIBLE) {
            checkAdAndExit()
        } else {
            showCloseBtn()
        }
    }

    private fun toggleActivityViews(isSubscriptionMode: Boolean) {
        activity?.let { act ->
            val visibilityMode = if (isSubscriptionMode) View.GONE else View.VISIBLE
            val subModeVisibility = if (isSubscriptionMode) View.VISIBLE else View.GONE

            act.findViewById<View>(R.id.inclToolBar)?.visibility = visibilityMode
            act.findViewById<View>(R.id.ivPurchase)?.visibility = visibilityMode
            act.findViewById<View>(R.id.clSubDiv)?.visibility = visibilityMode
            act.findViewById<View>(R.id.ivClose)?.visibility = visibilityMode
            act.findViewById<View>(R.id.ivSettings)?.visibility = visibilityMode

            // Subscription specific containers
            act.findViewById<View>(R.id.clSubscription)?.visibility = subModeVisibility
            act.findViewById<ImageView>(R.id.ivClosePurchase)?.apply {
                visibility = subModeVisibility
                setOnClickListener { act.onBackPressedDispatcher.onBackPressed() }
            }
        }
    }



    private fun setupDetailsPopupLogic() {
        activity?.let { act ->
            val ivDetails = act.findViewById<TextView>(R.id.ivDetails)
            val ivCloseDetails = act.findViewById<ImageView>(R.id.ivCloseDetails)

            ivDetails?.visibility = View.VISIBLE
            ivDetails?.setOnClickListener {
                binding.cvDetails.visibility = View.VISIBLE
                ivCloseDetails?.visibility = View.VISIBLE
                ivDetails.visibility = View.INVISIBLE
            }

            ivCloseDetails?.setOnClickListener {
                binding.cvDetails.visibility = View.GONE
                ivCloseDetails.visibility = View.GONE
                ivDetails?.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleSystemUI(hide: Boolean) {
        val window = requireActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (hide) {
                controller?.hide(WindowInsets.Type.statusBars())
                controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller?.show(WindowInsets.Type.statusBars())
            }
        } else {
            @Suppress("DEPRECATION")
            if (hide) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }
    }

    private fun showCloseBtn() {
        activity?.findViewById<ImageView>(R.id.ivClosePurchase)?.visibility = View.VISIBLE
    }


    private fun setupObservers() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    billingManager.productDetailsList.collect { productList ->
                        processBillingList(productList)
                    }
                }
                launch {
                    billingManager.isPremium.collect { isPremium ->
                        if (isPremium) handleSuccessfulPurchase()
                    }
                }
            }
        }
    }



private fun processBillingList(productDetailsList: List<ProductDetails>) {
    if (productDetailsList.isEmpty()) return

    // Temporary lists to hold data before sorting
    val tempUiList = ArrayList<itemDs>()
    val tempOfferList = ArrayList<Pair<ProductDetails, SubscriptionOfferDetails>>()

    val processedPlans = HashSet<String>()

    // 1. Extract Data
    for (productDetails in productDetailsList) {
        productDetails.subscriptionOfferDetails?.forEach { offerDetails ->
            val basePlanId = offerDetails.basePlanId

            if (!processedPlans.contains(basePlanId)) {
                var periodTitle = ""
                var detailsLine = ""
                var billingLine = ""
                var suffix = ""
                var isValid = false

                when (basePlanId) {
                    "sindhikeyboard-weekly" -> {
                        periodTitle = "Week"
                        detailsLine = "Auto Renewal"
                        billingLine = "Billed Weekly"
                        isValid = true
                    }
                    "sindhikeyboard-monthly" -> {
                        periodTitle = "Month"
                        detailsLine = "3 Days Free Trial, Auto Renewal"
                        billingLine = "Billed Monthly"
                        suffix = " (After Free Trial)"
                        isValid = true
                    }
                    "sindhikeyboard-yearly" -> {
                        periodTitle = "Yearly"
                        detailsLine = "7 Days Free Trial, Auto Renewal"
                        billingLine = "Billed Yearly"
                        suffix = " (After Free Trial)"
                        isValid = true
                    }
                }

                if (isValid) {
                    val pricingPhase = offerDetails.pricingPhases.pricingPhaseList.lastOrNull()
                    val price = pricingPhase?.formattedPrice ?: "N/A"

                    val headerText = "$price / $periodTitle$suffix"
                    val fullDetails = "$detailsLine\n$billingLine"

                    // Create item with temporary index 0, we will fix it after sorting
                    tempUiList.add(itemDs(fullDetails, headerText, 0))
                    tempOfferList.add(Pair(productDetails, offerDetails))

                    processedPlans.add(basePlanId)
                }
            }
        }
    }

    // 2. Sort both lists together based on the UI text (Week -> Month -> Year)
    val combined = tempUiList.zip(tempOfferList).sortedBy { (item, _) ->
        if (item.formattedPrice.contains("Week")) 1
        else if (item.formattedPrice.contains("Month")) 2
        else 3
    }

    // 3. Clear Main Lists
    subscriptionsArrayList.clear()
    mappedOffers.clear()

    // 4. Re-populate Main Lists AND FIX INDICES
    combined.forEachIndexed { newIndex, (item, offer) ->
        // CRITICAL FIX: Create a NEW itemDs with the CORRECTED index (newIndex)
        // This ensures the item knows it is now at position 0, 1, or 2
        val reIndexedItem = itemDs(item.subsName, item.formattedPrice, newIndex)

        subscriptionsArrayList.add(reIndexedItem)
        mappedOffers.add(offer)
    }

    updateAdapterAndSelection()
}


    private fun updateSubscriptionUI(basePlanId: String) {
        // Note: Replace 'bottomTextView' with your actual TextView ID from XML
        if (basePlanId == "sindhikeyboard-weekly") {
            // Plan without a free trial
            binding.txtCancelAnytime.text = "Billed Immediately, Cancel Anytime"

            // Or if you prefer to hide it completely, uncomment the line below:
            // binding.bottomTextView.visibility = View.GONE
        } else {
            // Plans with a free trial (Monthly / Yearly)
            binding.txtCancelAnytime.text = "No Initial Payment, Cancel Anytime"
            binding.txtCancelAnytime.visibility = View.VISIBLE
        }
    }

    private fun updateAdapterAndSelection() {
        if (!isAdded) return

        adapter.setSubscriptionList(subscriptionsArrayList)
        binding.cvLoaded.visibility = View.GONE
        showCloseBtn()

        if (subscriptionsArrayList.isNotEmpty()) {
            // Default selection: Yearly
            var defaultPosition = 0
            for (i in subscriptionsArrayList.indices) {
                if (subscriptionsArrayList[i].formattedPrice.contains("Yearly", ignoreCase = true)) {
                    defaultPosition = i
                    break
                }
            }

            selectedListIndex = defaultPosition // Set selected index
            PrefHelper(requireContext()).putString("SubscriberPosition", defaultPosition.toString())

            // --- NEW CODE: Set initial text for the default plan ---
            if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
                val (_, offerDetails) = mappedOffers[selectedListIndex]
                val basePlanId = offerDetails.basePlanId
                updateSubscriptionUI(basePlanId)
            }
            // -------------------------------------------------------

            // Note: If you want the adapter to visually update the radio button immediately:
            adapter.notifyDataSetChanged()
        }
    }

    private fun initiatePurchase() {
        if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
            val (product, offer) = mappedOffers[selectedListIndex]
            val offerToken = offer.offerToken

            if (offerToken.isNotEmpty()) {
                billingManager.launchPurchaseFlow(requireActivity(), product, offerToken)
            } else {
                Toast.makeText(requireContext(), "Offer not valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please select a plan", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleSuccessfulPurchase() {
        if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
            val (_, offerDetails) = mappedOffers[selectedListIndex]
            val basePlanId = offerDetails.basePlanId

            val eventName = when (basePlanId) {
                "sindhikeyboard-weekly" -> "inapp_purchase_sindhi_weekly"
                "sindhikeyboard-monthly" -> "inapp_purchase_sindhi_monthly"
                "sindhikeyboard-yearly" -> "inapp_purchase_sindhi_yearly"
                else -> "inapp_purchase_other"
            }

            try {
                // Assuming you copied the CustomFirebaseEvents object to this project:
                CustomFirebaseEvents.logEvent(requireActivity(),eventName = eventName)
                Log.e("FIREBASE_EVENT", "Logged: $eventName")
            } catch (e: Exception) {
                Log.e("FIREBASE_EVENT", "Error logging event: ${e.message}")
            }
        }

        Toast.makeText(requireActivity(), "Purchase Successful!", Toast.LENGTH_SHORT).show()
        requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
            .edit { putBoolean(Preferences.IS_PURCHASED, true) }

        navigateAway()
    }

    // endregion

    // region --- Navigation & Logic ---

    private fun ensureNavController() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun checkAdAndExit() {
        ensureNavController()

        val isPurchased = requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
            .getBoolean(Preferences.IS_PURCHASED, false)

        val showInterstitial = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
            .getString(INTERSTITIAL_SUBSCRIPTION_EXIT, "ON").equals("ON", true)

        if (navController != null && NetworkCheck.isNetworkAvailable(requireActivity()) && !isPurchased && showInterstitial) {
            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                context = requireContext(),
                nameFragment = "SubscriptionFragment",
                onAdClosedCallBackAdmob = {
                    Handler(Looper.getMainLooper()).postDelayed({ navigateAway() }, 300)
                },
                onAdShowedCallBackAdmob = {}
            )
        } else {
            navigateAway()
        }
    }

    private fun navigateAway() {
        ensureNavController()
        if (navController == null) return

        try {
            if (fromSubscription == "Home") {
                val action = SubscriptionFragmentDirections.actionNavSubscriptionFragmentToNavHome()
                navController?.navigate(action)
            } else {
                navController?.popBackStack()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openSubscriptionsLink() {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
            startActivity(browserIntent)
        } catch (e: Exception) { }
    }

    // endregion
}