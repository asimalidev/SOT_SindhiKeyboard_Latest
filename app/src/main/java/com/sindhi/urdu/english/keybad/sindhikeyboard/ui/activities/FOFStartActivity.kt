package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.manual.mediation.library.sotadlib.adMobAdClasses.AdMobBannerAdSplash
import com.manual.mediation.library.sotadlib.callingClasses.LanguageScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsConfigurations
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsManager
import com.manual.mediation.library.sotadlib.callingClasses.WalkThroughScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.WelcomeScreensConfiguration
import com.manual.mediation.library.sotadlib.data.Language
import com.manual.mediation.library.sotadlib.data.WalkThroughItem
import com.manual.mediation.library.sotadlib.utils.MyLocaleHelper
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.manual.mediation.library.sotadlib.utilsGoogleAdsConsent.ConsentConfigurations
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.MBridgeSDK
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityFofstartBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.KEYPAD_BANNER_SHOW
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_WALK_THROUGH
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_CONVERSATION_SAVE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_HISTORY_TO_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SETTINGS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SINDHI_STATUS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_TRANSLATION_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.OPEN_AD_ENABLE_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.OPEN_AD_INSIDE_APP
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.isKeyboardEnabled
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.isKeyboardSelected
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_SPLASH
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_INTERSTITIAL_LETS_START
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_LANGUAGE_1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_LANGUAGE_2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_SURVEY_1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_SURVEY_2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_WALKTHROUGH_1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_WALKTHROUGH_2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_WALKTHROUGH_3
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_NATIVE_WALKTHROUGH_FULLSCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_SPLASH_INTERSTITIAL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_SPLASH_RESUME
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_SPLASH
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.TIMER_NATIVE_F_SRC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bytedance.sdk.openadsdk.api.PAGConstant
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.applicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ResumeAd
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_INTER_POST_GALLERY_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_BANNER_TRANSLATOR_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_NATIVE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.SHOW_NATIVE_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.BillingManager
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ACTION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_SINDHI_STATUS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_TRANSLATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_UNINSTAL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATVE_SURVAY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION3
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.FROM_SHORTCUT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.RESUME_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_IMAGE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_POETRY_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_STICKERS_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_STICKERS_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_STYLISH_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_THEMES_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_THEMES_TEST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_VOICE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_CONVERSATION_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_CROP_TO_IMG_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_EXIT_APP
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_HISTORY_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_HISTORY_TO_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SAVE_SINDHI_POST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SINDHI_EDITOR_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SINDHI_POETRY_CATEGORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SINDHI_STATUS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SINDHI_STICKERS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_STYLISH_TEXT_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SUBSCRIPTION_EXIT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_TEXT_REVERSE_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_TEXT_TRANSLATOR_3_CLICKS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_TEXT_TRANSLATOR_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_THEMES_APPLIED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_THEMES_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_VOICE_TRANSLATOR_3_CLICKS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_VOICE_TRANSLATOR_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_EXIT_APP
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_IMAGE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_POETRY_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_STICKERS_CATEGORY_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_SINDHI_STICKERS_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_STYLISH_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_THEMES_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_THEMES_TEST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_TRANSLATOR_HOME_SCR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_NATIVE_VOICE_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_REWARDED_SINDHI_STICKERS
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class FOFStartActivity : AppCompatActivity() {
    private var firstOpenFlowAdIds: HashMap<String, String> = HashMap()
    lateinit var binding: ActivityFofstartBinding
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private lateinit var sotAdsConfigurations: SOTAdsConfigurations
    private var isDuplicateScreenStarted = true
    private lateinit var prefs: SharedPreferences
    private var notificationTarget: String? = null
    private var action: String? = null
    private var isHeavyAdsFlowStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFofstartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InterstitialClassAdMob.resetSessionState()
        prefs = getSharedPreferences(REMOTE_CONFIG, Context.MODE_PRIVATE)
        hideSystemUIUpdated()
        setStatusBarColor(this, resources.getColor(R.color.white))
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        supportActionBar?.hide()

        notificationTarget = intent?.getStringExtra("target_screen")
        action = intent?.getStringExtra(ACTION)
        checkAppUpdate(this) {
            lifecycleScope.launch {
                val remoteConfigData = initializeRemoteConfigAndAdIds()
                startFirstOpenFlow(remoteConfigData)

            }
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(color)
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0, // remove light status bar appearance
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    private fun startFirstOpenFlow(remoteConfigData: HashMap<String, Any>) {
        if (isFinishing || isDestroyed) return
        CustomFirebaseEvents.logEvent(this, eventName = "splash_scr")

        SOTAdsManager.setOnFlowStateListener(
            reConfigureBuilders = {
                SOTAdsManager.refreshStrings(setUpWelcomeScreen(this), getWalkThroughList(this))
            },
            onFinish = {
                CustomFirebaseEvents.logEvent(this, eventName = "walkthrough3_scr_tap_start")
                gotoMainActivity()
            }
        )

        val consentConfig = ConsentConfigurations.Builder()
            .setApplicationContext(application)
            .setActivityContext(this)
            .setTestDeviceHashedIdList(
                arrayListOf(
                    "AD512F017A910F15ADB01D9295B01D51",
                    "16AA2DB5B834B81BDB7AB2AC0B65BD7D",
                    "84C3994693FB491110A5A4AEF8C5561B",
                    "CB2F3812ACAA2A3D8C0B31682E1473EB",
                    "F02B044F22C917805C3DF6E99D3B8800"
                )
            )
            .setOnConsentGatheredCallback {
                if (isFinishing || isDestroyed) return@setOnConsentGatheredCallback

                if (NetworkCheck.isNetworkAvailable(this) && !prefs.getBoolean(IS_PURCHASED, false)) {
                    try {
                        // Make sure you updated 'applicationClass' to 'application' here!
                        ResumeAd(applicationClass)

                        val sdk: MBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
                        sdk.setConsentStatus(this, MBridgeConstans.IS_SWITCH_ON)
                        PangleMediationAdapter.setPAConsent(PAGConstant.PAGPAConsentType.PAG_PA_CONSENT_TYPE_CONSENT)
                    } catch (e: Exception) {
                        Log.e("FOFStartActivity", "Mediation SDK Consent Error", e)
                    }
                }

                if (NetworkCheck.isNetworkAvailable(this)) {
                    if (remoteConfigData[BANNER_SPLASH] == true && !prefs.getBoolean(
                            IS_PURCHASED,
                            false
                        )
                    ) {
                        binding.bannerAd.visibility = View.VISIBLE
                        loadAdmobBannerAd(remoteConfigData)
                    } else {
                        binding.bannerAd.visibility = View.INVISIBLE
                        startHeavyAdsFlow(remoteConfigData)
                    }
                } else {
                    binding.bannerAd.visibility = View.INVISIBLE
                    startHeavyAdsFlow(remoteConfigData)
                }
            }
            .build()

        val welcomeScreensConfiguration = WelcomeScreensConfiguration.Builder()
            .setActivityContext(this)
            .setXMLLayout(setUpWelcomeScreen(this))
            .build()

        // 2. The drawables will now safely load because we proved the Context is alive
        val languageScreensConfiguration = LanguageScreensConfiguration.Builder()
            .setActivityContext(this)
            .setDrawableColors(
                selectedDrawable = AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_lang_selected
                )!!,
                unSelectedDrawable = AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_lang_unselected
                )!!,
                selectedRadio = AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_selected_radio
                )!!,
                unSelectedRadio = AppCompatResources.getDrawable(
                    this,
                    R.drawable.ic_unselected_radio
                )!!,
                tickSelector = AppCompatResources.getDrawable(
                    this,
                    R.drawable.done_bg
                )!!,
                themeColor = ContextCompat.getColor(this, R.color.white),
                statusBarColor = ContextCompat.getColor(this, R.color.white),
                font = ContextCompat.getColor(this, R.color.black),
                headingColor = ContextCompat.getColor(this, R.color.black)
            )
            .setLanguages(
                arrayListOf(
                    Language.Urdu,
                    Language.English,
                    Language.Hindi,
                    Language.French,
                    Language.Dutch,
                    Language.Arabic,
                    Language.German
                )
            )
            .build()

        val walkThroughScreensConfiguration = WalkThroughScreensConfiguration.Builder()
            .setActivityContext(this)
            .setWalkThroughContent(getWalkThroughList(this))
            .build()

        sotAdsConfigurations = SOTAdsConfigurations.Builder()
            .setFirstOpenFlowAdIds(firstOpenFlowAdIds)
            .setConsentConfig(consentConfig)
            .setLanguageScreenConfiguration(languageScreensConfiguration)
            .setWelcomeScreenConfiguration(welcomeScreensConfiguration)
            .setWalkThroughScreenConfiguration(walkThroughScreensConfiguration)
            .build()

        SOTAdsManager.startFlow(sotAdsConfigurations)
    }

    private fun startHeavyAdsFlow(remoteData: HashMap<String, Any>) {
        if (isHeavyAdsFlowStarted) {
            sotAdsConfigurations.proceedNext(this@FOFStartActivity)
            return
        }
        isHeavyAdsFlowStarted = true

        sotAdsConfigurations.setRemoteConfigData(
            activityContext = this@FOFStartActivity,
            myRemoteConfigData = remoteData
        )
    }

    private fun loadAdmobBannerAd(remoteData: HashMap<String, Any>) {
        val pID = firstOpenFlowAdIds["ADMOB_BANNER_SPLASH"]
            ?: resources.getString(R.string.ADMOB_BANNER_SPLASH)
        binding?.let { it ->
            AdMobBannerAdSplash(
                this@FOFStartActivity,
                placementID = pID,
                bannerContainer = it.bannerAd,
                shimmerContainer = it.bannerShimmerLayout,
                onAdFailed = {
                    startHeavyAdsFlow(remoteData)
                    binding?.let {
                        it.bannerAd.visibility = View.GONE
                    }

                },
                onAdLoaded = {
                    binding?.let {
                        it.bannerAd.visibility = View.VISIBLE
                    }
                },
                onAdClicked = {

                },
                onAdImpression = {
                    Log.i("SOT_ADS_TAG", "Banner is VISIBLE! Starting Interstitial/Native Flow.")
                    startHeavyAdsFlow(remoteData)
                }

            )
        }

    }



    private fun gotoMainActivity() {
        Log.d("action", "intent?.action :${intent?.action} ")
        Log.d("action", "action :$action ")

        // 1. Handle Uninstall Shortcut (Early Return)
        if (intent?.action == "android.intent.action.SHORTCUT_UNINSTALL_APP") {
            val uninstallIntent = Intent(this@FOFStartActivity, ConfirmUninstallActivity::class.java).apply {
                putExtra(FROM_SHORTCUT, intent?.getStringExtra(FROM_SHORTCUT))
            }
            startActivity(uninstallIntent)
            finish()
            return // Exits the function early
        }

        val isNetworkAvailable = NetworkCheck.isNetworkAvailable(this@FOFStartActivity)
        val moveTo = intent?.getStringExtra("MoveTo")

        // 2. Handle Action Destinations (Early Return)
//        if (action != null) {
//            when (action) {
//                DESTINATION1, DESTINATION2, DESTINATION3 -> {
//                    val targetClass = if (isNetworkAvailable) {
//                        SubscriptionActivity::class.java
//                    } else {
//                        NavigationActivity::class.java
//                    }
//
//                    startActivity(Intent(this@FOFStartActivity, targetClass).apply {
//                        putExtra("fromName", "Splash")
//                        putExtra(ACTION, action)
//                    })
//
//                    action = null
//                    finish()
//                    return
//                }
//            }
//        }


        if (action != null) {
            when (action) {
                // Scenario: Stickers Shortcut
                "DESTINATION2" -> {
                    if (isNetworkAvailable) {
                        // Online -> Subscription -> Stickers
                        startActivity(Intent(this, SubscriptionActivity::class.java).apply {
                            putExtra("ACTION", "DESTINATION2")
                        })
                    } else {
                        // Offline -> Directly to Stickers
                        startActivity(Intent(this, StickersViewActivity::class.java).apply {
                            putExtra("ACTION", "DESTINATION2")
                        })
                    }
                    finish()
                    return
                }

                // Scenario: Themes Shortcut
                "DESTINATION3" -> {
                    if (isNetworkAvailable) {
                        // Online -> Subscription -> Navigation (Themes)
                        startActivity(Intent(this, SubscriptionActivity::class.java).apply {
                            putExtra("ACTION", "DESTINATION3")
                        })
                    } else {
                        // Offline -> Directly to Navigation (Themes)
                        startActivity(Intent(this, NavigationActivity::class.java).apply {
                            putExtra("ACTION", "DESTINATION3")
                        })
                    }
                    finish()
                    return
                }
            }
        }

        // 3. Handle Keyboard and "MoveTo" Logic
        val nextIntent: Intent = when {
            // Condition A: Keyboard setup isn't complete
            !isKeyboardEnabled(this@FOFStartActivity) || !isKeyboardSelected(this@FOFStartActivity) -> {
                Intent(this@FOFStartActivity, KeyboardSelectionActivity::class.java).apply {
                    putExtra("MoveTo", moveTo)
                }
            }

            // Condition B: MoveTo is sindhi_stickers AND Network is Available
            moveTo == "sindhi_stickers" && isNetworkAvailable -> {
                Intent(this@FOFStartActivity, SubscriptionActivity::class.java).apply {
                    putExtra("MoveTo", moveTo)
                }
            }

            // Condition C: MoveTo is sindhi_stickers AND Network is NOT Available
            moveTo == "sindhi_stickers" && !isNetworkAvailable -> {
                // IMPORTANT: If "StickerActivity" is causing the error, make sure
                // you import it, or rename it to whatever your actual activity is called!
                Intent(this@FOFStartActivity, StickersViewActivity::class.java).apply {
                    putExtra("MoveTo", moveTo)
                }
            }

            // Condition D: Default fallback with Network
            isNetworkAvailable -> {
                Intent(this@FOFStartActivity, SubscriptionActivity::class.java).apply {
                    putExtra("fromName", "Splash")
                    putExtra("MoveTo", moveTo)
                }
            }

            // Condition E: Default fallback without Network
            else -> {
                Intent(this@FOFStartActivity, NavigationActivity::class.java).apply {
                    putExtra("MoveTo", moveTo)
                }
            }
        }

        startActivity(nextIntent)
        finish()
    }

    private fun getWalkThroughList(context: Context): ArrayList<WalkThroughItem> {
        val localizedContext = ContextWrapper(this).createConfigurationContext(
            resources.configuration.apply { MyLocaleHelper.onAttach(context, "en") }
        )

        return arrayListOf(
            WalkThroughItem(
                heading = "Amazing Themes",
                description = localizedContext.getString(R.string.label_w_1_descrption),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.white,
                nextBackground = R.drawable.done_bg,
                drawableResId = R.drawable.ic_w_1,  // Changed from Drawable to resource ID
                drawableBubbleResId = R.drawable.ic_bubble_one,
                R.color.white,
                imageScale = 0
            ),
            WalkThroughItem(
                heading = "Hussn e Gufttar",
                description = localizedContext.getString(R.string.label_w_2_descrption),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.white,
                nextBackground = R.drawable.done_bg,
                drawableResId = R.drawable.ic_w_2,
                drawableBubbleResId = R.drawable.ic_bubble_two,
                R.color.white,
                imageScale = 0
            ),
            WalkThroughItem(
                heading = "Funny Stickers & Emojis",
                description = localizedContext.getString(R.string.label_w_3_descrption),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.white,
                nextBackground = R.drawable.done_bg,
                drawableResId = R.drawable.ic_w_3,
                drawableBubbleResId = R.drawable.ic_bubble_three,
                R.color.white,
                imageScale = 0
            )
        )
    }

    private fun setUpWelcomeScreen(context: Context): View {
        val activity = context as? Activity ?: run {
            return View(context).apply {
                // You might want to log this error in production
                Log.e("WelcomeScreen", "Context is not an Activity")
            }
        }

        return try {
            val localizedConfig = context.resources.configuration.apply {
                MyLocaleHelper.onAttach(context, "en")
            }
            val localizedContext =
                ContextWrapper(context).createConfigurationContext(localizedConfig)

            val welcomeScreenView = LayoutInflater.from(localizedContext)
                .inflate(R.layout.layout_welcome_scr_1, null, false)

            // View initialization
            val progressAnim = welcomeScreenView.findViewById<LottieAnimationView>(R.id.progress)
            val txtWallpapers = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtWallpapers)
            val txtSindhiEditor = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSindhiEditor)
            val txtLiveThemes = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtLiveThemes)
            val txtPhotoOnKeyboard = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoOnKeyboard)
            val txtPhotoTranslator = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoTranslator)
            val txtInstantSticker = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtInstantSticker)
            val nextButton = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtNext)

            txtWallpapers.isSelected = true
            txtSindhiEditor.isSelected = true
            txtLiveThemes.isSelected = true
            txtPhotoOnKeyboard.isSelected = true
            txtPhotoTranslator.isSelected = true
            txtInstantSticker.isSelected = true

            // State variables
            var txtWallpapersBool = false
            var txtSindhiEditorBool = false
            var txtLiveThemesBool = false
            var txtPhotoOnKeyboardBool = false
            var txtCreatePDFBool = false
            var txtInstantStickerBool = false

            // Helper function to handle checkbox toggle
            fun handleCheckboxToggle(
                isChecked: Boolean,
                textView: AppCompatTextView,
                eventName: String
            ): Boolean {
                if (isDuplicateScreenStarted) {
                    SOTAdsManager.showWelcomeDupScreen()
                }
                isDuplicateScreenStarted = false
                progressAnim.visibility = View.GONE

                CustomFirebaseEvents.logEvent(activity, eventName = eventName)

                val newState = !isChecked
                textView.setBackgroundResource(
                    if (newState) R.drawable.ic_selected_state
                    else R.drawable.ic_unselected_state
                )
                return newState
            }

            // Set up click listeners
            txtWallpapers.setOnClickListener {
                txtWallpapersBool = handleCheckboxToggle(
                    txtWallpapersBool,
                    txtWallpapers,
                    "survey_scr_check_wallpaper"
                )
            }

            txtSindhiEditor.setOnClickListener {
                txtSindhiEditorBool = handleCheckboxToggle(
                    txtSindhiEditorBool,
                    txtSindhiEditor,
                    "survey_scr_check_sindhi_editor"
                )
            }

            txtLiveThemes.setOnClickListener {
                txtLiveThemesBool = handleCheckboxToggle(
                    txtLiveThemesBool,
                    txtLiveThemes,
                    "survey_scr_check_live_themes"
                )
            }

            txtPhotoOnKeyboard.setOnClickListener {
                txtPhotoOnKeyboardBool = handleCheckboxToggle(
                    txtPhotoOnKeyboardBool,
                    txtPhotoOnKeyboard,
                    "survey_scr_check_photo_on_keyboard"
                )
            }

            txtPhotoTranslator.setOnClickListener {
                txtCreatePDFBool = handleCheckboxToggle(
                    txtCreatePDFBool,
                    txtPhotoTranslator,
                    "survey_scr_check_photo_translator"
                )
            }

            txtInstantSticker.setOnClickListener {
                txtInstantStickerBool = handleCheckboxToggle(
                    txtInstantStickerBool,
                    txtInstantSticker,
                    "survey_scr_check_instant_stickers"
                )
            }

            nextButton.setOnClickListener {
                if (txtWallpapersBool || txtSindhiEditorBool ||
                    txtLiveThemesBool || txtPhotoOnKeyboardBool ||
                    txtCreatePDFBool || txtInstantStickerBool
                ) {
                    CustomFirebaseEvents.logEvent(activity, eventName = "survey2_scr")
                    CustomFirebaseEvents.logEvent(activity, eventName = "survey2_scr_tap_continue")
                    SOTAdsManager.completeWelcomeScreens()
                } else {
                    CustomFirebaseEvents.logEvent(activity, eventName = "survey1_scr")
                    CustomFirebaseEvents.logEvent(activity, eventName = "survey1_scr_tap_continue")

                    if (isDuplicateScreenStarted) {
                        SOTAdsManager.showWelcomeDupScreen()
                    }
                    isDuplicateScreenStarted = false

                    Toast.makeText(activity, "Please check the checkbox", Toast.LENGTH_SHORT)
                        .apply {
                            setGravity(Gravity.CENTER, 0, 0)
                            show()
                        }
                }
            }
            welcomeScreenView
        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty view or handle error appropriately
            View(context)
        }
    }

    private suspend fun initializeRemoteConfigAndAdIds(): HashMap<String, Any> =
        withContext(Dispatchers.IO) {
            if (FirebaseApp.getApps(this@FOFStartActivity).isEmpty()) {
                withContext(Dispatchers.Main) {
                    if (FirebaseApp.getApps(this@FOFStartActivity)
                            .isEmpty()
                    ) { // Double-check inside lock
                        FirebaseApp.initializeApp(this@FOFStartActivity)
                        Log.i("RemoteConfig", "FirebaseApp explicitly initialized on Main Thread")
                    }
                }
            }

            // Initialize Remote Config
            if (mFirebaseRemoteConfig == null) {
                mFirebaseRemoteConfig =
                    FirebaseRemoteConfig.getInstance() // ✅ THIS WILL NO LONGER CRASH
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0) // Good for testing, set to 3600 for production
                    .build()
                mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
            }

            /**The old code was checking fetchSuccessful, which is only satisfied if changes were made in Firebase Remote Config. This approach was incorrect.*/
            if (NetworkCheck.isNetworkAvailable(this@FOFStartActivity)) {
                if (!BuildConfig.DEBUG) {
                    try {
                        mFirebaseRemoteConfig!!.fetchAndActivate().await()
                        Log.i("RemoteConfig", "RemoteConfig ready (new or cached)")
                        fillAdIdsFromRemote(mFirebaseRemoteConfig!!)
                        saveAllValues()
                    } catch (e: Exception) {
                        Log.e("RemoteConfig", "Exception fetching config: ${e.message}")
                        fillAdIdsFromResources()
                    }
                } else {
                    try {
                        Log.i("RemoteConfig", "debug ...fetching (debug mode)")
                        mFirebaseRemoteConfig!!.fetchAndActivate().await()
                        Log.d("RemoteConfig", "RemoteConfig ready (debug)")

                        fillAdIdsFromResources()
                        saveAllValues()
                    } catch (e: Exception) {
                        Log.e("RemoteConfig", "Exception fetching config (debug): ${e.message}")
                        fillAdIdsFromResources()
                    }
                }
            } else {
                Log.i("RemoteConfig", "No network, using local resources")
                fillAdIdsFromResources()
            }

            return@withContext getSharedPreferencesValues()
        }

    private fun saveAllValues() {
        val remoteConfig = mFirebaseRemoteConfig ?: return
        getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
            val booleanKeys = listOf(
                BANNER_SPLASH,
                RemoteConfigConst.RESUME_OVERALL,
                RemoteConfigConst.NATIVE_UNINSTALL,
                RemoteConfigConst.NATIVE_SURVEY,
                RemoteConfigConst.NATIVE_LANGUAGE_1,
                RemoteConfigConst.NATIVE_LANGUAGE_2,
                RemoteConfigConst.NATIVE_SURVEY_1,
                RemoteConfigConst.NATIVE_SURVEY_2,
                RemoteConfigConst.NATIVE_WALKTHROUGH_1,
                RemoteConfigConst.NATIVE_WALKTHROUGH_2,
                RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR,
                RemoteConfigConst.NATIVE_WALKTHROUGH_3,
                RemoteConfigConst.INTERSTITIAL_LETS_START
            )

            for (key in booleanKeys) {
                val value = remoteConfig.getBoolean(key)
                if (key == RemoteConfigConst.RESUME_OVERALL) {
                    Log.e("resume", "value of $value")
                }
                putBoolean(key, value)
            }
            val stringKeys = listOf(
                RemoteConfigConst.RESUME_INTER_SPLASH,
                KEYPAD_BANNER_SHOW,
                ADS_NATIVE_WALK_THROUGH,
                ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT,
                SHOW_NATIVE_THEMES_LIST,
                SHOW_BANNER_THEMES_APPLY,
                NATIVE_CONVERSATION,
                ADMOB_BANNER_TRANSLATION,
                BANNER_INSIDE,
                ADMOB_BANNER_SINDHI_STATUS,
                SHOW_BANNER_SINDHI_POETRY_CATEGORY_LIST,
                SHOW_BANNER_TEXT_REVERSE,
                SHOW_NATIVE_TEXT_REVERSE,
                SHOW_BANNER_TEXT_TRANSLATOR,
                SHOW_INTER_TEXT_TRANSLATOR_ENTER,
                SHOW_NATIVE_HISTORY,
                SHOW_INTER_CONVERSATION_ENTER,
                NATIVE_OVER_ALL,
                SHOW_BANNER_THEMES_LIST,
                SHOW_INTER_TEXT_TRANSLATOR_3_CLICKS,
                SHOW_NATIVE_TEXT_TRANSLATOR,
                SHOW_INTER_SUBSCRIPTION_EXIT,
                SHOW_INTER_EXIT_APP,
                SHOW_NATIVE_THEMES_APPLY,
                SHOW_BANNER_THEMES_TEST,
                SHOW_BANNER_HISTORY,
                ADS_NATIVE_CONVERSATION,
                SHOW_REWARDED_SINDHI_STICKERS,
                SHOW_NATIVE_CONVERSATION,
                SHOW_INTER_VOICE_TRANSLATOR_3_CLICKS,
                SHOW_INTER_SAVE_SINDHI_POST,
                ADS_NATIVE_HISTORY,
                BANNER_POETRY,
                SHOW_NATIVE_EXIT_APP,
                NATIVE_TEXT_REVERSE,
                AD_ID_NATVE_SURVAY,
                SHOW_NATIVE_THEMES_TEST,
                AD_ID_NATIVE_UNINSTAL,
                ADMOB_BANNER_THEMES,
                ADS_NATIVE_POETRY,
                SHOW_NATIVE_SINDHI_POETRY,
                SHOW_NATIVE_SELECT_KEYBOARD,
                SHOW_BANNER_SETTINGS,
                SHOW_NATIVE_VOICE_TRANSLATOR,
                SHOW_NATIVE_TRANSLATOR_HOME_SCR,
                SHOW_BANNER_HOME_SCR,
                SHOW_NATIVE_HOME_SCR,
                SHOW_BANNER_TRANSLATOR_HOME_SCR,
                SHOW_BANNER_SELECT_KEYBOARD,
                SHOW_INTER_THEMES_ENTER,
                SHOW_INTER_THEMES_APPLIED,
                INTERSTITIAL_SINDHI_STATUS_ENTER,
                INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK,
                INTERSTITIAL_TRANSLATION_ENTER,
                INTERSTITIAL_SETTINGS_ENTER,
                SHOW_INTER_CONVERSATION_ENTER,
                INTERSTITIAL_CONVERSATION_SAVE,
                SHOW_INTER_VOICE_TRANSLATOR_ENTER,
                SHOW_BANNER_SINDHI_STICKERS_CATEGORY_LIST,
                SHOW_NATIVE_SINDHI_STICKERS_CATEGORY_LIST,
                SHOW_INTER_SINDHI_EDITOR_ENTER,
                SHOW_INTER_TEXT_REVERSE_ENTER,
                SHOW_INTER_SINDHI_STATUS_ENTER,
                SHOW_INTER_SINDHI_STICKERS_ENTER,
                SHOW_BANNER_SINDHI_POETRY,
                SHOW_NATIVE_SINDHI_STICKERS_DETAILS,
                SHOW_BANNER_SINDHI_STICKERS_DETAILS,
                SHOW_INTER_HISTORY_ENTER,
                SHOW_BANNER_CONVERSATION,
                INTERSTITIAL_HISTORY_TO_CONVERSATION,
                OPEN_AD_ENABLE_KEYBOARD,
                OPEN_AD_INSIDE_APP,
                SHOW_INTER_POST_GALLERY_ENTER,
                SHOW_BANNER_STYLISH_TEXT,
                SHOW_NATIVE_STYLISH_TEXT,
                SHOW_INTER_CROP_TO_IMG_TRANSLATOR,
                SHOW_BANNER_IMAGE_TRANSLATOR,
                SHOW_NATIVE_IMAGE_TRANSLATOR,
                SHOW_INTER_HISTORY_TO_CONVERSATION,
                SHOW_INTER_STYLISH_TEXT_ENTER,
                SHOW_NATIVE_SINDHI_POETRY_CATEGORY_LIST,
                SHOW_BANNER_VOICE_TRANSLATOR,
                SHOW_NATIVE_SETTINGS,
                SHOW_INTER_SINDHI_POETRY_CATEGORY
            )

            for (key in stringKeys) {
                val value = remoteConfig.getString(key).trim()
                if (value.isNotEmpty()) {
                    putString(key, value)
                }
            }
        }
    }

    private fun getSharedPreferencesValues(): HashMap<String, Any> {
        val remoteConfigHashMap: HashMap<String, Any> = HashMap()
        remoteConfigHashMap.apply {
            this["RESUME_INTER_SPLASH"] = "${prefs.getString(RemoteConfigConst.RESUME_INTER_SPLASH, "Empty")}"
            this["BANNER_SPLASH"] = prefs.getBoolean(BANNER_SPLASH, false)
            this["NATIVE_LANGUAGE_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1, false)
            this["NATIVE_LANGUAGE_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2, false)
            this["NATIVE_SURVEY_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_1, false)
            this["NATIVE_SURVEY_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_2, false)
            this["RESUME_OVERALL"] = prefs.getBoolean(RemoteConfigConst.RESUME_OVERALL, false)
            this["NATIVE_WALKTHROUGH_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1, false)
            this["NATIVE_WALKTHROUGH_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2, false)
            this["NATIVE_WALKTHROUGH_FULLSCR"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR, false)
            this["NATIVE_WALKTHROUGH_3"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3, false)
            this["INTERSTITIAL_LETS_START"] = prefs.getBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START, false)
            this["TIMER_NATIVE_F_SRC"] = "${prefs.getString(TIMER_NATIVE_F_SRC, "Empty")}"
            this["IS_PURCHASED"] = prefs.getBoolean(IS_PURCHASED, false)
        }
        return remoteConfigHashMap
    }

    private fun fillAdIdsFromRemote(remoteConfig: FirebaseRemoteConfig) {
        firstOpenFlowAdIds.apply {
            this["ADMOB_SPLASH_INTERSTITIAL"] = remoteConfig.getString(ADMOB_SPLASH_INTERSTITIAL)
            this["ADMOB_SPLASH_RESUME"] = remoteConfig.getString(ADMOB_SPLASH_RESUME)
            this["ADMOB_BANNER_SPLASH"] = remoteConfig.getString(ADMOB_BANNER_SPLASH)
            this["ADMOB_NATIVE_LANGUAGE_1"] = remoteConfig.getString(ADMOB_NATIVE_LANGUAGE_1)
            this["ADMOB_NATIVE_LANGUAGE_2"] = remoteConfig.getString(ADMOB_NATIVE_LANGUAGE_2)
            this["ADMOB_NATIVE_SURVEY_1"] = remoteConfig.getString(ADMOB_NATIVE_SURVEY_1)
            this["ADMOB_NATIVE_SURVEY_2"] = remoteConfig.getString(ADMOB_NATIVE_SURVEY_2)
            this["ADMOB_NATIVE_WALKTHROUGH_1"] = remoteConfig.getString(ADMOB_NATIVE_WALKTHROUGH_1)
            this["ADMOB_NATIVE_WALKTHROUGH_2"] = remoteConfig.getString(ADMOB_NATIVE_WALKTHROUGH_2)
            this["ADMOB_NATIVE_WALKTHROUGH_FULLSCR"] =
                remoteConfig.getString(ADMOB_NATIVE_WALKTHROUGH_FULLSCR)
            this["ADMOB_NATIVE_WALKTHROUGH_3"] = remoteConfig.getString(ADMOB_NATIVE_WALKTHROUGH_3)
            this["ADMOB_INTERSTITIAL_LETS_START"] =
                remoteConfig.getString(ADMOB_INTERSTITIAL_LETS_START)
            this["RESUME_OVER_ALL"] = remoteConfig.getString(RESUME_OVER_ALL)

            Log.d("AdConfig", "===== Loaded Ad IDs =====")
            forEach { (key, value) ->
                Log.d("AdConfig", "$key = $value")
            }
            Log.d("AdConfig", "=========================")
        }
    }

    private fun fillAdIdsFromResources() {
        firstOpenFlowAdIds.apply {
            this["ADMOB_SPLASH_INTERSTITIAL"] = getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
            this["ADMOB_SPLASH_RESUME"] = getString(R.string.ADMOB_SPLASH_RESUME)
            this["ADMOB_BANNER_SPLASH"] = getString(R.string.ADMOB_BANNER_SPLASH)
            this["ADMOB_NATIVE_LANGUAGE_1"] = getString(R.string.ADMOB_NATIVE_LANGUAGE_1)
            this["ADMOB_NATIVE_LANGUAGE_2"] = getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
            this["ADMOB_NATIVE_SURVEY_1"] = getString(R.string.ADMOB_NATIVE_SURVEY_1)
            this["ADMOB_NATIVE_SURVEY_2"] = getString(R.string.ADMOB_NATIVE_SURVEY_2)
            this["ADMOB_NATIVE_WALKTHROUGH_1"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_1)
            this["ADMOB_NATIVE_WALKTHROUGH_2"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_2)
            this["ADMOB_NATIVE_WALKTHROUGH_FULLSCR"] =
                getString(R.string.ADMOB_NATIVE_WALKTHROUGH_FULLSCR)
            this["ADMOB_NATIVE_WALKTHROUGH_3"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_3)
            this["ADMOB_INTERSTITIAL_LETS_START"] =
                getString(R.string.ADMOB_INTERSTITIAL_LETS_START)
        }
    }

    override fun onResume() {
        super.onResume()
        BillingManager.getInstance(this).checkActivePurchases()
    }

    private fun checkAppUpdate(context: Context, onDone: () -> Unit) {
        val prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedVersion = prefs.getInt("last_version_code", -1)
        val currentVersion = getCurrentVersionCode(context)

        if (currentVersion > savedVersion) {
            prefs.edit { putInt("last_version_code", currentVersion) }
            PrefHelper(this).putBoolean("StartScreens", value = false)
            onDone.invoke()
        } else {
            onDone.invoke()
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            packageInfo.versionCode
        }
    }

}