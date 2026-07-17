package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_EXIT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_POETRY_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_SPEECHTOTEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_THEMES_APPLIED_TEST
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_THEMES_APPLY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_TRANSLATION_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.ADS_NATIVE_WALK_THROUGH
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_HOME
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_SELECT_KEYBOARD
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_SETTINGS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.COLLAPSIBLE_TRANSLATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_CONVERSATION_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_CONVERSATION_SAVE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_HISTORY_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_HISTORY_TO_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SETTINGS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SINDHI_STATUS_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SPEECH_TO_TEXT_3_CLICK_ENABLE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SPEECH_TO_TEXT_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_SUBSCRIPTION_EXIT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_THEME_APPLIED
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.INTERSTITIAL_THEME_ENTER
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADS_BANNER_HISTORY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADS_BANNER_THEMES_TEST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_SPLASH
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_STICKER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_STICKER_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.INTERSTITIAL_STICKER_ENTER
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_STICKERS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_STICKERS_DETAILS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.TIMER_NATIVE_F_SRC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.bytedance.sdk.openadsdk.api.PAGConstant
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.applicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ResumeAd
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.BillingManager
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ACTION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_SINDHI_STATUS
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_THEMES
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ADMOB_BANNER_TRANSLATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_UNINSTAL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATVE_SURVAY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_SINDHI_STATUS_SHOW
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_THEMES_APPLIED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.BANNER_THEMES_LIST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION3
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.FROM_SHORTCUT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_INSIDE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_TEXT_REVERSE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_TEXT_TRANSLATOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.RESUME_OVER_ALL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        CoroutineScope(Dispatchers.Default).launch {
            try {
                FirebaseApp.initializeApp(this@FOFStartActivity)
                Log.d("AppInit", "Firebase initialized successfully")
            } catch (e: Exception) {
                Log.e("AppInit", "Firebase initialization failed", e)
            }
        }

      //  checkAppUpdate(this)
        prefs = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        hideSystemUIUpdated()
        setStatusBarColor(this, resources.getColor(R.color.board_theme__red))
        supportActionBar?.hide()
       // initializeRemoteConfigAndStartFlow()
        checkAppUpdate(this) {
            lifecycleScope.launch {
                val remoteConfigData = initializeRemoteConfigAndAdIds()
                startFirstOpenFlow(remoteConfigData)

            }
        }
        notificationTarget = intent?.getStringExtra("target_screen")
        action = intent?.getStringExtra(ACTION)
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


    /*private fun initializeRemoteConfigAndStartFlow() {
        initializeRemoteConfigAndAdIds { remoteConfigData ->
            Log.d("RemoteConfig", "Remote config completed, $remoteConfigData")
            startFirstOpenFlow(remoteConfigData)
        }
    }*/


    private fun startFirstOpenFlow(remoteConfigData: HashMap<String, Any>) {
        // 1. CRITICAL LIFECYCLE CHECK: Abort if the Activity is no longer active
        // This prevents the NullPointerException when fetching Drawables on a dead context.
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

                if (NetworkCheck.isNetworkAvailable(this) && !prefs.getBoolean(
                        IS_PURCHASED,
                        false
                    )
                ) {
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
                    com.manual.mediation.library.sotadlib.R.drawable.ic_done
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

    @RequiresApi(Build.VERSION_CODES.N)
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


    @RequiresApi(Build.VERSION_CODES.N)
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

        if (intent?.action == "android.intent.action.SHORTCUT_UNINSTALL_APP") {
            val uninstallIntent =
                Intent(this@FOFStartActivity, ConfirmUninstallActivity::class.java)
            uninstallIntent.putExtra(FROM_SHORTCUT, intent?.getStringExtra(FROM_SHORTCUT))
            startActivity(uninstallIntent)
            finish()
        } else {
            if (action != null) {
                when (action) {
                    DESTINATION1, DESTINATION3 -> {
                        // CHANGED: Route to SubscriptionActivity, pass 'action', and set fromName = "Splash"
                        startActivity(
                            Intent(this, SubscriptionActivity::class.java)
                                .putExtra("fromName", "Splash")
                                .putExtra(ACTION, action)
                        )
                        action = null
                        finish() // Don't forget to finish splash
                    }

                    DESTINATION2 -> {
                        startActivity(Intent(this, StickersViewActivity::class.java))
                        action = null
                        finish()
                    }
                }
            } else {
                val nextIntent: Intent =
                    if (!isKeyboardEnabled(this) || !isKeyboardSelected(this)) {
                        Intent(this, KeyboardSelectionActivity::class.java).putExtra(
                            "MoveTo", intent.getStringExtra("MoveTo")
                        )
                    } else if (intent.getStringExtra("MoveTo") == "sindhi_stickers") {
                        Intent(this, StickersViewActivity::class.java).putExtra(
                            "MoveTo", intent.getStringExtra("MoveTo")
                        )
                    } else {
                        // CHANGED: Route all other NavigationActivity cases to SubscriptionActivity
                        Intent(this, SubscriptionActivity::class.java).apply {
                            putExtra("fromName", "Splash")
                            putExtra("MoveTo", intent.getStringExtra("MoveTo"))
                        }
                    }

                startActivity(nextIntent)
                finish()
            }
        }
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
                nextColor = R.color.colorPrimary,
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
                nextColor = R.color.colorPrimary,
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
                nextColor = R.color.colorPrimary,
                drawableResId = R.drawable.ic_w_3,
                drawableBubbleResId = R.drawable.ic_bubble_three,
                R.color.white,
                imageScale = 0
            )
        )
    }

    private fun setUpWelcomeScreen(context: Context): View {
        // First, ensure we have an Activity context
        val activity = context as? Activity ?: run {
            // Fallback if we don't have an Activity context
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
            val txtWallpapers =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtWallpapers)
            val txtSindhiEditor =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSindhiEditor)
            val txtLiveThemes =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtLiveThemes)
            val txtPhotoOnKeyboard =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoOnKeyboard)
            val txtPhotoTranslator =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoTranslator)
            val txtInstantSticker =
                welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtInstantSticker)
            val nextButton = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtNext)

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
    private suspend fun initializeRemoteConfigAndAdIds(): HashMap<String, Any> = withContext(Dispatchers.IO) {
        // Initialize Remote Config
        if (mFirebaseRemoteConfig == null) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // Good for testing, set to 3600 for production
                .build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)

        }

        if (NetworkCheck.isNetworkAvailable(this@FOFStartActivity)){
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
            }
            else
            {
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
        }
        else{
            Log.i("RemoteConfig", "No network, using local resources")
            fillAdIdsFromResources()
        }

        return@withContext getSharedPreferencesValues()
    }
    private fun saveAllValues() {
        val config = mFirebaseRemoteConfig
        if (config == null) {
            Log.e("RemoteConfig", "mFirebaseRemoteConfig is null")
            return
        }

        // 1. Group all Boolean keys into a single list
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

        // 2. Group all String keys into a single list
        val stringKeys = listOf(
            RemoteConfigConst.RESUME_INTER_SPLASH,
            KEYPAD_BANNER_SHOW,
            ADS_NATIVE_WALK_THROUGH,
            ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT,
            NATIVE_TEXT_TRANSLATOR,
            BANNER_THEMES_APPLIED,
            ADMOB_BANNER_TRANSLATION,
            BANNER_INSIDE,
            ADMOB_BANNER_SINDHI_STATUS,
            BANNER_TEXT_REVERSE,
            BANNER_TEXT_TRANSLATOR,
            ADMOB_BANNER_INSIDE,
            NATIVE_OVER_ALL,
            BANNER_THEMES_LIST,
            INTERSTITIAL_SUBSCRIPTION_EXIT,
            INTERSTITIAL_STICKER_ENTER,
            ADS_NATIVE_THEMES_APPLY,
            ADS_NATIVE_CONVERSATION,
            ADS_NATIVE_HISTORY,
            BANNER_POETRY,
            ADS_NATIVE_HOME,
            ADS_NATIVE_EXIT,
            ADS_NATIVE_THEMES,
            NATIVE_TEXT_REVERSE,
            AD_ID_NATVE_SURVAY,
            ADS_NATIVE_THEMES_APPLIED_TEST,
            AD_ID_NATIVE_UNINSTAL,
            NATIVE_INSIDE,
            ADMOB_BANNER_THEMES,
            ADS_NATIVE_POETRY,
            ADS_NATIVE_POETRY_INSIDE,
            ADS_NATIVE_SELECT_KEYBOARD,
            ADS_NATIVE_SETTINGS,
            ADS_NATIVE_SPEECHTOTEXT,
            ADS_NATIVE_TRANSLATION_HOME,
            COLLAPSIBLE_HOME,
            COLLAPSIBLE_TRANSLATION,
            COLLAPSIBLE_SELECT_KEYBOARD,
            COLLAPSIBLE_SETTINGS,
            ADS_BANNER_THEMES_TEST,
            ADS_BANNER_HISTORY,
            COLLAPSIBLE_CONVERSATION,
            INTERSTITIAL_THEME_ENTER,
            INTERSTITIAL_THEME_APPLIED,
            INTERSTITIAL_SINDHI_STATUS_ENTER,
            INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK,
            INTERSTITIAL_TRANSLATION_ENTER,
            INTERSTITIAL_SETTINGS_ENTER,
            INTERSTITIAL_CONVERSATION_ENTER,
            INTERSTITIAL_CONVERSATION_SAVE,
            INTERSTITIAL_SPEECH_TO_TEXT_ENTER,
            BANNER_STICKER,
            NATIVE_STICKERS,
            BANNER_SINDHI_STATUS_SHOW,
            NATIVE_STICKERS_DETAILS,
            BANNER_STICKER_DETAILS,
            INTERSTITIAL_SPEECH_TO_TEXT_3_CLICK_ENABLE,
            INTERSTITIAL_HISTORY_ENTER,
            INTERSTITIAL_HISTORY_TO_CONVERSATION,
            OPEN_AD_ENABLE_KEYBOARD,
            OPEN_AD_INSIDE_APP
        )

        // 3. Save everything in a single edit block
        // Note: Assuming REMOTE_CONFIG holds the "RemoteConfig" string based on your first snippet
        getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {

            // Save Booleans
            booleanKeys.forEach { key ->
                putBoolean(key, config.getBoolean(key))
            }

            // Save Strings (ignoring empty/blank values like the original code did)
            stringKeys.forEach { key ->
                val value = config.getString(key).trim()
                if (value.isNotEmpty()) {
                    putString(key, value)
                }
            }
        }

        Log.e("Ids Firebase", "Fetched SuccessFully: Native :: ${config.getString(KEYPAD_BANNER_SHOW)}")
        Log.e("resume", "value of ${config.getBoolean(RemoteConfigConst.RESUME_OVERALL)}")

    }
    /*private fun initializeRemoteConfigAndAdIds(onComplete: (HashMap<String, Any>) -> Unit) {
        if (mFirebaseRemoteConfig == null) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        }

        if (NetworkCheck.isNetworkAvailable(this@FOFStartActivity)) {
            if (!BuildConfig.DEBUG) {
                mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "RemoteConfig fetched successfully")
                        mFirebaseRemoteConfig!!.activate()
                        // 1. Fill firstOpenFlowAdIds for SOTAds
                        fillAdIdsFromRemote(mFirebaseRemoteConfig!!)
                        // 2. Save all values to SharedPreferences
                        saveAllValues {
                            onComplete.invoke(getSharedPreferencesValues())
                        }

                    } else {
                        Log.d("RemoteConfig", "RemoteConfig fetch failed, using fallback")
                        // Fallback to local resources
                        fillAdIdsFromResources()
                        onComplete.invoke(getSharedPreferencesValues())

                        if (resources.getString(R.string.ShowPopups) == "true") {
                            Toast.makeText(this, "RemoteConfig Failed", Toast.LENGTH_SHORT).show()
                        }
                        onComplete.invoke(getSharedPreferencesValues())
                    }
                }
            } else {
                // debug mode - use local resources
                Log.d("RemoteConfig", "Using local resources (debug mode or no network)")
                mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "RemoteConfig fetched successfully")
                        mFirebaseRemoteConfig!!.activate()

                        fillAdIdsFromResources()
                        // 2. Save all values to SharedPreferences
                        saveAllValues {
                            onComplete.invoke(getSharedPreferencesValues())
                        }

                    } else {
                        Log.d("RemoteConfig", "RemoteConfig fetch failed, using fallback")
                        // Fallback to local resources
                        fillAdIdsFromResources()
                        onComplete.invoke(getSharedPreferencesValues())

                        if (resources.getString(R.string.ShowPopups) == "true") {
                            Toast.makeText(this, "RemoteConfig Failed", Toast.LENGTH_SHORT).show()
                        }
                        onComplete.invoke(getSharedPreferencesValues())
                    }
                }

            }
        } else {
            // No network
            fillAdIdsFromResources()
            onComplete.invoke(getSharedPreferencesValues())
        }
    }*/

    /*private fun saveAllValues(onCompleteSave: (() -> Unit)? = null) {
        val editor = getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE).edit()
        mFirebaseRemoteConfig?.apply {
            getString(RemoteConfigConst.RESUME_INTER_SPLASH).trim().takeIf { it.isNotEmpty() }
                ?.let {
                    editor.putString(RemoteConfigConst.RESUME_INTER_SPLASH, it)
                }
            editor.putBoolean(
                BANNER_SPLASH, getBoolean(BANNER_SPLASH)
            )

            editor.putBoolean(
                RemoteConfigConst.RESUME_OVERALL,
                getBoolean(RemoteConfigConst.RESUME_OVERALL)
            )


            editor.putBoolean(
                RemoteConfigConst.NATIVE_UNINSTALL,
                getBoolean(RemoteConfigConst.NATIVE_UNINSTALL)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_SURVEY,
                getBoolean(RemoteConfigConst.NATIVE_SURVEY)
            )


            Log.e("resume", "value of ${getBoolean(RemoteConfigConst.RESUME_OVERALL)}")

            editor.putBoolean(
                RemoteConfigConst.NATIVE_LANGUAGE_1, getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_LANGUAGE_2, getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_SURVEY_1, getBoolean(RemoteConfigConst.NATIVE_SURVEY_1)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_SURVEY_2, getBoolean(RemoteConfigConst.NATIVE_SURVEY_2)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_WALKTHROUGH_1,
                getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_WALKTHROUGH_2,
                getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR,
                getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR)
            )
            editor.putBoolean(
                RemoteConfigConst.NATIVE_WALKTHROUGH_3,
                getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3)
            )
            editor.putBoolean(
                RemoteConfigConst.INTERSTITIAL_LETS_START,
                getBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START)
            )

            editor.putBoolean(
                RemoteConfigConst.RESUME_OVERALL,
                getBoolean(RemoteConfigConst.RESUME_OVERALL)
            )

        }
        editor.apply()
        saveAllValuesForInsideAppAds {
            onCompleteSave?.invoke()
        }
    }

    private fun saveAllValuesForInsideAppAds(onComplete: (() -> Unit)? = null) {
        Log.e(
            "Ids Firebase",
            "Fetched SuccessFully : Native :: " + mFirebaseRemoteConfig!!.getString(
                KEYPAD_BANNER_SHOW
            )
        )

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(KEYPAD_BANNER_SHOW).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    KEYPAD_BANNER_SHOW,
                    mFirebaseRemoteConfig!!.getString(KEYPAD_BANNER_SHOW)
                ).apply()
        }

        // ADS REMOTE CONFIG NATIVE
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_WALK_THROUGH).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    ADS_NATIVE_WALK_THROUGH,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_WALK_THROUGH)
                ).apply()
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT)
                ).apply()
        }


        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(NATIVE_TEXT_TRANSLATOR).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    NATIVE_TEXT_TRANSLATOR,
                    mFirebaseRemoteConfig!!.getString(NATIVE_TEXT_TRANSLATOR)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_THEMES_APPLIED).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    BANNER_THEMES_APPLIED,
                    mFirebaseRemoteConfig!!.getString(BANNER_THEMES_APPLIED)
                ).apply()
        }


        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_TRANSLATION).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    ADMOB_BANNER_TRANSLATION,
                    mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_TRANSLATION)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_INSIDE).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    BANNER_INSIDE,
                    mFirebaseRemoteConfig!!.getString(BANNER_INSIDE)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_SINDHI_STATUS).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    ADMOB_BANNER_SINDHI_STATUS,
                    mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_SINDHI_STATUS)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_TEXT_REVERSE).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    BANNER_TEXT_REVERSE,
                    mFirebaseRemoteConfig!!.getString(BANNER_TEXT_REVERSE)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_TEXT_TRANSLATOR).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    BANNER_TEXT_TRANSLATOR,
                    mFirebaseRemoteConfig!!.getString(BANNER_TEXT_TRANSLATOR)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_INSIDE).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    ADMOB_BANNER_INSIDE,
                    mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_INSIDE)
                ).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(NATIVE_OVER_ALL).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putString(
                    NATIVE_OVER_ALL,
                    mFirebaseRemoteConfig!!.getString(NATIVE_OVER_ALL)
                ).apply()
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    BANNER_THEMES_LIST
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putString(
                BANNER_THEMES_LIST,
                mFirebaseRemoteConfig!!.getString(BANNER_THEMES_LIST)
            ).apply()
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SUBSCRIPTION_EXIT).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SUBSCRIPTION_EXIT,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SUBSCRIPTION_EXIT)
                )
            }
        }


        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    INTERSTITIAL_STICKER_ENTER
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_STICKER_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_STICKER_ENTER)
                )
            }
        }


 if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    ADS_NATIVE_THEMES_APPLY
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_THEMES_APPLY,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_THEMES_APPLY)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_CONVERSATION).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_CONVERSATION,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_CONVERSATION)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_HISTORY).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_HISTORY,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_HISTORY)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_POETRY).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(BANNER_POETRY, mFirebaseRemoteConfig!!.getString(BANNER_POETRY))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_HOME).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(ADS_NATIVE_HOME, mFirebaseRemoteConfig!!.getString(ADS_NATIVE_HOME))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_EXIT).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(ADS_NATIVE_EXIT, mFirebaseRemoteConfig!!.getString(ADS_NATIVE_EXIT))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_THEMES).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(ADS_NATIVE_THEMES, mFirebaseRemoteConfig!!.getString(ADS_NATIVE_THEMES))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(NATIVE_TEXT_REVERSE).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(NATIVE_TEXT_REVERSE, mFirebaseRemoteConfig!!.getString(NATIVE_TEXT_REVERSE))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(AD_ID_NATVE_SURVAY).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    AD_ID_NATVE_SURVAY,
                    mFirebaseRemoteConfig!!.getString(AD_ID_NATVE_SURVAY)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADS_NATIVE_THEMES_APPLIED_TEST).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_THEMES_APPLIED_TEST,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_THEMES_APPLIED_TEST)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(AD_ID_NATIVE_UNINSTAL).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    AD_ID_NATIVE_UNINSTAL,
                    mFirebaseRemoteConfig!!.getString(AD_ID_NATIVE_UNINSTAL)
                )
            }
        }


        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(NATIVE_INSIDE).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    NATIVE_INSIDE,
                    mFirebaseRemoteConfig!!.getString(NATIVE_INSIDE)
                )
            }
        }




        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_THEMES).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADMOB_BANNER_THEMES,
                    mFirebaseRemoteConfig!!.getString(ADMOB_BANNER_THEMES)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_POETRY).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(ADS_NATIVE_POETRY, mFirebaseRemoteConfig!!.getString(ADS_NATIVE_POETRY))
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADS_NATIVE_POETRY_INSIDE).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_POETRY_INSIDE,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_POETRY_INSIDE)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SELECT_KEYBOARD).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_SELECT_KEYBOARD,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SELECT_KEYBOARD)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SETTINGS).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_SETTINGS,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SETTINGS)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SPEECHTOTEXT).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_SPEECHTOTEXT,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_SPEECHTOTEXT)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADS_NATIVE_TRANSLATION_HOME).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_TRANSLATION_HOME,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_TRANSLATION_HOME)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(ADS_NATIVE_TRANSLATION_HOME).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_NATIVE_TRANSLATION_HOME,
                    mFirebaseRemoteConfig!!.getString(ADS_NATIVE_TRANSLATION_HOME)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_HOME).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(COLLAPSIBLE_HOME, mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_HOME))
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_TRANSLATION).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    COLLAPSIBLE_TRANSLATION,
                    mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_TRANSLATION)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_SELECT_KEYBOARD).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    COLLAPSIBLE_SELECT_KEYBOARD,
                    mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_SELECT_KEYBOARD)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_SETTINGS).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    COLLAPSIBLE_SETTINGS,
                    mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_SETTINGS)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_BANNER_THEMES_TEST).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_BANNER_THEMES_TEST,
                    mFirebaseRemoteConfig!!.getString(ADS_BANNER_THEMES_TEST)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(ADS_BANNER_HISTORY).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    ADS_BANNER_HISTORY,
                    mFirebaseRemoteConfig!!.getString(ADS_BANNER_HISTORY)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_CONVERSATION).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    COLLAPSIBLE_CONVERSATION,
                    mFirebaseRemoteConfig!!.getString(COLLAPSIBLE_CONVERSATION)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_THEME_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_THEME_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_THEME_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_THEME_APPLIED).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_THEME_APPLIED,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_THEME_APPLIED)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SINDHI_STATUS_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SINDHI_STATUS_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SINDHI_STATUS_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SINDHI_STATUS_POETRY_CLICK)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_TRANSLATION_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_TRANSLATION_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_TRANSLATION_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SETTINGS_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SETTINGS_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SETTINGS_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_CONVERSATION_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_CONVERSATION_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_CONVERSATION_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_CONVERSATION_SAVE).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_CONVERSATION_SAVE,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_CONVERSATION_SAVE)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SPEECH_TO_TEXT_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SPEECH_TO_TEXT_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SPEECH_TO_TEXT_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(BANNER_STICKER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    BANNER_STICKER,
                    mFirebaseRemoteConfig!!.getString(BANNER_STICKER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(NATIVE_STICKERS).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    NATIVE_STICKERS,
                    mFirebaseRemoteConfig!!.getString(NATIVE_STICKERS)
                )
            }
        }


        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_SINDHI_STATUS_SHOW).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    BANNER_SINDHI_STATUS_SHOW,
                    mFirebaseRemoteConfig!!.getString(BANNER_SINDHI_STATUS_SHOW)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(NATIVE_STICKERS_DETAILS).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    NATIVE_STICKERS_DETAILS,
                    mFirebaseRemoteConfig!!.getString(NATIVE_STICKERS_DETAILS)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(BANNER_STICKER_DETAILS).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    BANNER_STICKER_DETAILS,
                    mFirebaseRemoteConfig!!.getString(BANNER_STICKER_DETAILS)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    INTERSTITIAL_SPEECH_TO_TEXT_3_CLICK_ENABLE
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_SPEECH_TO_TEXT_3_CLICK_ENABLE,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_SPEECH_TO_TEXT_3_CLICK_ENABLE)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(INTERSTITIAL_HISTORY_ENTER).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_HISTORY_ENTER,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_HISTORY_ENTER)
                )
            }
        }

        if (!TextUtils.isEmpty(
                mFirebaseRemoteConfig!!.getString(
                    INTERSTITIAL_HISTORY_TO_CONVERSATION
                ).trim()
            )
        ) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    INTERSTITIAL_HISTORY_TO_CONVERSATION,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_HISTORY_TO_CONVERSATION)
                )
            }
        }

        // ADS REMOTE CONFIG INTERSTITIAL
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(OPEN_AD_ENABLE_KEYBOARD).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    OPEN_AD_ENABLE_KEYBOARD,
                    mFirebaseRemoteConfig!!.getString(OPEN_AD_ENABLE_KEYBOARD)
                )
            }
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(OPEN_AD_INSIDE_APP).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit {
                putString(
                    OPEN_AD_INSIDE_APP,
                    mFirebaseRemoteConfig!!.getString(OPEN_AD_INSIDE_APP)
                )
            }
        }

        onComplete?.invoke()
    }*/

    private fun getSharedPreferencesValues(): HashMap<String, Any> {
        val remoteConfigHashMap: HashMap<String, Any> = HashMap()
        remoteConfigHashMap.apply {
            this["RESUME_INTER_SPLASH"] =
                "${prefs.getString(RemoteConfigConst.RESUME_INTER_SPLASH, "Empty")}"
            this["BANNER_SPLASH"] = prefs.getBoolean(BANNER_SPLASH, false)
            this["NATIVE_LANGUAGE_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1, false)
            this["NATIVE_LANGUAGE_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2, false)
            this["NATIVE_SURVEY_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_1, false)
            this["NATIVE_SURVEY_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_2, false)
            this["RESUME_OVERALL"] = prefs.getBoolean(RemoteConfigConst.RESUME_OVERALL, false)
            this["NATIVE_WALKTHROUGH_1"] =
                prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1, false)
            this["NATIVE_WALKTHROUGH_2"] =
                prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2, false)
            this["NATIVE_WALKTHROUGH_FULLSCR"] =
                prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR, false)
            this["NATIVE_WALKTHROUGH_3"] =
                prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3, false)
            this["INTERSTITIAL_LETS_START"] =
                prefs.getBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START, false)
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
    /*private fun checkAppUpdate(context: Context) {
        val prefs = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val savedVersion = prefs.getInt("last_version_code", 88)
        val currentVersion = getCurrentVersionCode(context)
        Log.e("version", "saved $savedVersion")
        Log.e("version", "currentversion$currentVersion")
        if (currentVersion != savedVersion) {
            PrefHelper(this).putBoolean("StartScreens", value = false)
            prefs.edit().putInt("last_version_code", currentVersion)
                .putBoolean("is_first", true) // reset onboarding
                .apply()
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            packageInfo.versionCode
        }
    }*/

}