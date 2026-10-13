package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.FontAssetDelegate
import com.android.billingclient.api.ProductDetails
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.sindhi.urdu.english.keybad.databinding.ActivitySubscriptionBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.privacyPolicy
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.termsOfService
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.SubscriptionAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.BillingManager
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SUBSCRIPTION_EXIT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideSystemUIUpdated
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.itemDs
import kotlinx.coroutines.launch

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var subscriptionsArrayList: ArrayList<itemDs>
    private val mappedOffers = mutableListOf<Pair<ProductDetails, ProductDetails.SubscriptionOfferDetails>>()
    private var selectedOfferIndex = -1
    private lateinit var billingManager: BillingManager
    private lateinit var adapter: SubscriptionAdapter
    private var fromSubscription: String? = null
    private var isPurchase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySubscriptionBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            Log.e("CRASH_DEBUG", "The exact reason for the crash is: ", e)
            throw e
        }

        binding.btnContinue.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun fetchFont(fontFamily: String?): Typeface {
                return Typeface.DEFAULT
            }
        })

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        supportActionBar?.hide()
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        hideSystemUIUpdated()
        isPurchase = getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).getBoolean(Preferences.IS_PURCHASED, false)
        fromSubscription = intent.getStringExtra("fromName")
        binding.cvLoaded.visibility = View.VISIBLE
        subscriptionsArrayList = arrayListOf()
        billingManager = BillingManager.getInstance(this)
        setupUIListeners()
        setupObservers()
        setupBackPressHandler()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
        binding.ivBack.visibility = View.GONE
        binding.ivBack.postDelayed({
            binding.ivBack.visibility = View.VISIBLE
        }, 4500)
    }

    override fun onStop() {
        super.onStop()
        showStatusBar()
    }

    private fun setupUIListeners() {
        binding.ivBack.setOnClickListener {
            handleBackAction()
        }

        adapter = SubscriptionAdapter { itemDs ->
            selectedOfferIndex = itemDs.planIndex
        }

        binding.rvSubscription.layoutManager = GridLayoutManager(this, 3)

        binding.rvSubscription.adapter = adapter

        binding.btnContinue.setOnClickListener {
            if (selectedOfferIndex != -1) {
                subscribeProduct()
            } else {
                Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtPrivacyPolicy.setOnClickListener { privacyPolicy(this) }

        binding.txtTermsAndConditions.setOnClickListener { termsOfService(this) }
        binding.tvGooglePlayStore.setOnClickListener { openSubscriptionsLink() }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackAction()
            }
        })
    }

    private fun handleBackAction() {
        if (binding.cvDetails.visibility == View.VISIBLE) {
            binding.cvDetails.visibility = View.GONE
        } else {
            checkAdAndExit()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    billingManager.productDetailsList.collect { productList ->
                        updateSubscriptionList(productList)
                    }
                }
                launch {
                    billingManager.isPremium.collect { isPremium ->
                        if (isPremium) {
                            handleSuccessfulPurchase()
                        }
                    }
                }
            }
        }
    }

    private fun updateSubscriptionList(productDetailsList: List<ProductDetails>) {
        if (productDetailsList.isEmpty()) return
        val tempUiList = ArrayList<itemDs>()
        val tempOfferList = ArrayList<Pair<ProductDetails, ProductDetails.SubscriptionOfferDetails>>()
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
                            detailsLine = "Starter Plan"
                            billingLine = "Billed Weekly"
                            suffix = ""
                            isValid = true
                        }

                        "sindhikeyboard-monthly" -> {
                            periodTitle = "Month"
                            detailsLine = "3 Days Free Trail"
                            billingLine = "Billed Monthly"
                            suffix = " (After Free Trial)"
                            isValid = true
                        }

                        "sindhikeyboard-yearly" -> {
                            periodTitle = "Yearly"
                            detailsLine = "7 Days Free Trail"
                            billingLine = "Billed Yearly"
                            suffix = " (After Free Trial)"
                            isValid = true
                        }
                    }

                    if (isValid) {
                        val pricingPhase = offerDetails.pricingPhases.pricingPhaseList.lastOrNull()
                        val price = pricingPhase?.formattedPrice ?: "N/A"

                        val headerText = "$price / $periodTitle$suffix"
                        val fullDetails = "$billingLine\n$detailsLine"

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

        if (!isFinishing && !isDestroyed) {
            adapter.setSubscriptionList(subscriptionsArrayList)
            binding.cvLoaded.visibility = View.GONE
            if (subscriptionsArrayList.isNotEmpty()) {
                val yearlyIndex = subscriptionsArrayList.indexOfFirst {
                    it.formattedPrice.contains("Year", ignoreCase = true)
                }
                val defaultIndex = if (yearlyIndex != -1) yearlyIndex else 0

                selectedOfferIndex = subscriptionsArrayList[defaultIndex].planIndex
                PrefHelper(this).putString("SubscriberPosition", defaultIndex.toString())
                adapter.notifyDataSetChanged()
            }
        }

    }

    private fun subscribeProduct() {
        if (selectedOfferIndex != -1 && selectedOfferIndex < mappedOffers.size) {
            val (product, offer) = mappedOffers[selectedOfferIndex]
            val offerToken = offer.offerToken

            if (offerToken.isNotEmpty()) {
                billingManager.launchPurchaseFlow(this, product, offerToken)
            }
            else {
                Toast.makeText(this, "Offer not valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSuccessfulPurchase() {
        if (selectedOfferIndex != -1 && selectedOfferIndex < mappedOffers.size) {
            val (_, offerDetails) = mappedOffers[selectedOfferIndex]
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
        getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE).edit { putBoolean(Preferences.IS_PURCHASED, true) }
        navigateAway()
    }

    private fun checkAdAndExit() {
        val isEnable = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
            .getString(SHOW_INTER_SUBSCRIPTION_EXIT, "ON").equals("ON", true)
        val hasNetwork = NetworkCheck.isNetworkAvailable(this)
        if (hasNetwork && isEnable && !isPurchase) {
            Log.d(
                "AdDebug",
                "✅ All conditions met. Requesting Interstitial Manager to show/load ad."
            )
            InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                context = this,
                nameFragment = "SubscriptionActivity",
                onAdClosedCallBackAdmob = {
                    Log.d("AdDebug", "Ad closed callback triggered. Navigating away.")
                    navigateAway()
                },
                onAdShowedCallBackAdmob = {
                    Log.d("AdDebug", "Ad successfully showed on screen.")
                }
            )
        } else {
            Log.d("AdDebug", "❌ Conditions NOT met. Skipping ad and navigating away immediately.")
            navigateAway()
        }
    }

//    private fun navigateAway() {
//        val fromSubscription = intent.getStringExtra("fromName")
//        val actionData = intent.getStringExtra("ACTION")
//        val moveToData = intent.getStringExtra("MoveTo")
//        if (moveToData == "sindhi_stickers" || actionData == DESTINATION2) {
//            val stickerIntent = Intent(this, StickersViewActivity::class.java).apply {
//                if (actionData != null) putExtra("ACTION", actionData)
//            }
//            startActivity(stickerIntent)
//            finish()
//            return // Exit the function here so the code below doesn't execute
//        }
//
//        if (fromSubscription == "Splash") {
//            val navIntent = Intent(this, NavigationActivity::class.java).apply {
//                if (actionData != null) putExtra("ACTION", actionData)
//                if (moveToData != null) putExtra("MoveTo", moveToData)
//            }
//            startActivity(navIntent)
//            finish()
//        } else {
//            val navIntent = Intent(this, NavigationActivity::class.java)
//            startActivity(navIntent)
//            finish() // <--- ADD THIS SO SUBSCRIPTION ACTIVITY CLOSES
//        }
//    }


    private fun navigateAway() {
        // Note: Ensure you use the exact string "ACTION" to match your ApplicationClass
        val actionData = intent.getStringExtra("ACTION")
        val moveToData = intent.getStringExtra("MoveTo")

        // If it's Stickers, route directly to StickersViewActivity and STOP
        if (moveToData == "sindhi_stickers" || actionData == "DESTINATION2") {
            val stickerIntent = Intent(this, StickersViewActivity::class.java).apply {
                if (actionData != null) putExtra("ACTION", actionData)
                if (moveToData != null) putExtra("MoveTo", moveToData)
            }
            startActivity(stickerIntent)
            finish()
            return // Exits here so the rest of the code doesn't run
        }

        // For everything else (including DESTINATION3 / Themes), route to NavigationActivity
        val navIntent = Intent(this, NavigationActivity::class.java).apply {
            if (actionData != null) putExtra("ACTION", actionData)
            if (moveToData != null) putExtra("MoveTo", moveToData)
        }
        startActivity(navIntent)
        finish() // Closes SubscriptionActivity
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

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun showStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

}