package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
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
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.sindhi.urdu.english.keybad.databinding.ActivitySubscriptionBinding
import androidx.core.view.isVisible
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideSystemUIUpdated

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var billingManager: BillingManager
    private lateinit var adapter: SubscriptionAdapter
    private var isPurchase = false
    private val subscriptionsArrayList = ArrayList<itemDs>()
    private val mappedOffers = mutableListOf<Pair<ProductDetails, SubscriptionOfferDetails>>()
    private var selectedListIndex = -1
    private var fromSubscription: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // This hides the status bar specifically
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        hideSystemUIUpdated()
        isPurchase = getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
        fromSubscription = intent.getStringExtra("fromName")
        billingManager = BillingManager.getInstance(this)
        subscriptionsArrayList.clear()

        binding.cvLoaded.visibility = View.VISIBLE
        binding.ivBack.setOnClickListener {
            checkAdAndExit()
        }

        setupRecyclerView()
        setupClickListeners()
        setupBackPressHandler()
        setupDetailsPopupLogic()
        setupObservers()
    }



    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        binding.ivBack.visibility = View.GONE
        binding.ivBack.postDelayed({
            binding.ivBack.visibility = View.VISIBLE
        }, 4500)
    }

    override fun onStop() {
        super.onStop()
    }

    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter { itemDs ->
            selectedListIndex = itemDs.planIndex
            PrefHelper(this).putString("SubscriberPosition", selectedListIndex.toString())

            if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
                val (_, offerDetails) = mappedOffers[selectedListIndex]
                val basePlanId = offerDetails.basePlanId
                updateSubscriptionUI(basePlanId)
            }
        }
        binding.rvSubscription.layoutManager = LinearLayoutManager(this)
        binding.rvSubscription.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener { initiatePurchase() }
        binding.apply {
            txtPrivacyPolicy.setOnClickListener { privacyPolicy(this@SubscriptionActivity) }
            txtUnsubscribe.setOnClickListener { unsubscribe(this@SubscriptionActivity) }
            txtTermsAndConditions.setOnClickListener { termsOfService(this@SubscriptionActivity) }
            txtGooglePlay.setOnClickListener { openSubscriptionsLink() }
            txtCancelSubscriptionsOnGoogle.setOnClickListener { openSubscriptionsLink() }
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackAction()
            }
        })
    }

    private fun handleBackAction() {
        if (binding.cvDetails.isVisible) {
            binding.cvDetails.visibility = View.GONE
        } else {
            // FIX #2: If the popup isn't showing, trigger the exit logic (which shows the ad)
            checkAdAndExit()
        }
    }

    private fun setupDetailsPopupLogic() {
        binding.ivDetails.visibility = View.VISIBLE
        binding.ivDetails.setOnClickListener {
            binding.cvDetails.visibility = View.VISIBLE
            binding.ivDetails.visibility = View.INVISIBLE
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

        val tempUiList = ArrayList<itemDs>()
        val tempOfferList = ArrayList<Pair<ProductDetails, SubscriptionOfferDetails>>()
        val processedPlans = HashSet<String>()

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

                        tempUiList.add(itemDs(fullDetails, headerText, 0))
                        tempOfferList.add(Pair(productDetails, offerDetails))

                        processedPlans.add(basePlanId)
                    }
                }
            }
        }

        val combined = tempUiList.zip(tempOfferList).sortedBy { (item, _) ->
            if (item.formattedPrice.contains("Week")) 1
            else if (item.formattedPrice.contains("Month")) 2
            else 3
        }

        subscriptionsArrayList.clear()
        mappedOffers.clear()

        combined.forEachIndexed { newIndex, (item, offer) ->
            val reIndexedItem = itemDs(item.subsName, item.formattedPrice, newIndex)
            subscriptionsArrayList.add(reIndexedItem)
            mappedOffers.add(offer)
        }

        updateAdapterAndSelection()
    }

    private fun updateSubscriptionUI(basePlanId: String) {
        if (basePlanId == "sindhikeyboard-weekly") {
            binding.txtCancelAnytime.text = "Billed Immediately, Cancel Anytime"
        } else {
            binding.txtCancelAnytime.text = "No Initial Payment, Cancel Anytime"
            binding.txtCancelAnytime.visibility = View.VISIBLE
        }
    }

    private fun updateAdapterAndSelection() {
        if (isFinishing || isDestroyed) return

        adapter.setSubscriptionList(subscriptionsArrayList)
        binding.cvLoaded.visibility = View.GONE

        if (subscriptionsArrayList.isNotEmpty()) {
            var defaultPosition = 0
            for (i in subscriptionsArrayList.indices) {
                if (subscriptionsArrayList[i].formattedPrice.contains("Yearly", ignoreCase = true)) {
                    defaultPosition = i
                    break
                }
            }

            selectedListIndex = defaultPosition
            PrefHelper(this).putString("SubscriberPosition", defaultPosition.toString())

            if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
                val (_, offerDetails) = mappedOffers[selectedListIndex]
                val basePlanId = offerDetails.basePlanId
                updateSubscriptionUI(basePlanId)
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun initiatePurchase() {
        if (selectedListIndex != -1 && selectedListIndex < mappedOffers.size) {
            val (product, offer) = mappedOffers[selectedListIndex]
            val offerToken = offer.offerToken

            if (offerToken.isNotEmpty()) {
                billingManager.launchPurchaseFlow(this, product, offerToken)
            } else {
                Toast.makeText(this, "Offer not valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show()
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
                CustomFirebaseEvents.logEvent(this, eventName = eventName)
                Log.e("FIREBASE_EVENT", "Logged: $eventName")
            } catch (e: Exception) {
                Log.e("FIREBASE_EVENT", "Error logging event: ${e.message}")
            }
        }

        Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_SHORT).show()
        getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
            .edit { putBoolean(Preferences.IS_PURCHASED, true) }

        navigateAway()
    }

    private fun checkAdAndExit() {
        Log.e("ivclose", "iv close is clicked")

        // Use the proper SharedPrefs constants here too to prevent crashes or bad logic
        val isEnable = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
            .getString(INTERSTITIAL_SUBSCRIPTION_EXIT, "ON").equals("ON", true)

        Log.e("intervalue", "$isEnable")

        if (com.manual.mediation.library.sotadlib.utils.NetworkCheck.isNetworkAvailable(this) && isEnable && !isPurchase) {
            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                context = this,
                nameFragment = "subscription screen",
                onAdShowedCallBackAdmob = {},
                onAdClosedCallBackAdmob = {
                    navigateAway()
                }
            )
        } else {
            navigateAway()
        }
    }

    private fun navigateAway() {
        val fromSubscription = intent.getStringExtra("fromName")
        val actionData = intent.getStringExtra("ACTION")
        val moveToData = intent.getStringExtra("MoveTo")

        if (fromSubscription == "Splash") {
            val navIntent = Intent(this, NavigationActivity::class.java).apply {
                if (actionData != null) putExtra("ACTION", actionData)
                if (moveToData != null) putExtra("MoveTo", moveToData)
            }
            startActivity(navIntent)
            finish()
        } else {
            val navIntent = Intent(this, NavigationActivity::class.java)
            startActivity(navIntent)
            finish() // <--- ADD THIS SO SUBSCRIPTION ACTIVITY CLOSES
        }
    }

    private fun openSubscriptionsLink() {
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions")
            )
            startActivity(browserIntent)
        } catch (e: Exception) {
        }
    }
}