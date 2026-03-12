package com.sindhi.urdu.english.keybad.sindhikeyboard.ads

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mbridge.msdk.MBridgeSDK
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mbridge.msdk.out.SDKInitStatusListener
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.DictionaryObject.suggestionList
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ACTION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION3
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.FROM_SHORTCUT
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.KEYPAD_AD_MEDIATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.KEYPAD_AD_VISIBILITY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ApplicationClass : Application() {
    companion object {
        lateinit var firebaseAnalyticsEventsLog: FirebaseAnalytics
        lateinit var resumeAdInstance: ResumeAd
        lateinit var applicationClass: ApplicationClass
        var selectedTheme: CustomTheme? = null
        const val logTagAdmob = "NativeAdKeyboardAdmob"
        var imageUriUrduEditorBackgrounds: Uri? = null
        const val logTagMintegral = "NativeAdKeyboardMintegral"
        const val ACTION_CONFIG_CHANGED = "com.sindhi.urdu.english.keybad.CONFIG_CHANGED"
        private const val TAG = "ApplicationClass"
    }

    override fun onCreate() {
        super.onCreate()
        applicationClass = this

        try {
            WebView(this)
            Log.d(TAG, "Chromium engine pre-warmed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pre-warm WebView", e)
        }
        initShortCut()

        // 1. Keep only the absolute fastest, required synchronous initializations here
        FirebaseApp.initializeApp(this)

        // Note: Make sure ResumeAd doesn't do disk I/O! If it does, move it down.
        resumeAdInstance = ResumeAd(this)


        // 2. Evacuate all heavy lifting to the IO Dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Move the massive database copy off the main thread
                DataBaseCopyOperationsKt.initializeAndCopy(this@ApplicationClass)

                // Firebase singletons can cause strict mode disk reads, initialize them here
                firebaseAnalyticsEventsLog = FirebaseAnalytics.getInstance(this@ApplicationClass)

                // Fetch remote config in the background
                setupRemoteConfig()

                // Load suggestions (this was already well-written as a suspend function)
                loadSuggestions()

            } catch (e: Exception) {
                Log.e(TAG, "Error during background initialization", e)
            }
        }

        // 3. Keep SDK initializations in the background
        CoroutineScope(Dispatchers.Default).launch {
            initMintegralSdk()
        }
    }
    private fun initShortCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val manager = getSystemService(ShortcutManager::class.java)
            try {
                manager.removeAllDynamicShortcuts()

                // 1. Junk Cleaner
                /*val junkCleanerShortCut = ShortcutInfo.Builder(this, DESTINATION1)
                    .setShortLabel(getString(R.string.sindhi_editor))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_sindi_editor))
                    .setIntent(Intent(this, FOFStartActivity::class.java).apply {
                        action = Intent.ACTION_VIEW // <-- ADDED THIS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(ACTION, DESTINATION1)
                    })
                    .setRank(1)
                    .build()*/

                // 2. AI Clean
                val aiCleanShortCut = ShortcutInfo.Builder(this, "action_ai_clean")
                    .setShortLabel(getString(R.string.sindhi_stickers))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_sindhi_stickers))
                    .setIntent(Intent(this, FOFStartActivity::class.java).apply {
                        action = Intent.ACTION_VIEW // <-- ADDED THIS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(ACTION, DESTINATION2)
                    })
                    .setRank(1)
                    .build()

                // 3. Battery Info
                val batteryShortCut = ShortcutInfo.Builder(this, DESTINATION3)
                    .setShortLabel(getString(R.string.change_theme))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_change_theme))
                    .setIntent(Intent(this, FOFStartActivity::class.java).apply {
                        action = Intent.ACTION_VIEW // <-- ADDED THIS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(ACTION, DESTINATION3)
                    })
                    .setRank(2)
                    .build()

                // 4. Uninstall Shortcut (Ranked last)
                val uninstallShortCut = ShortcutInfo.Builder(this, "ACTION_OPEN_UNINSTALL")
                    .setShortLabel(getString(R.string.uninstall))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_uninstall))
                    .setIntent(Intent(this, FOFStartActivity::class.java).apply {
                        action = "android.intent.action.SHORTCUT_UNINSTALL_APP" // This one was already fine!
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(FROM_SHORTCUT, "ACTION_OPEN_UNINSTALL")
                    })
                    .setRank(3)
                    .build()

                // Add all 4 to the list
                manager.dynamicShortcuts = listOf(
                    aiCleanShortCut,
                    batteryShortCut,
                    uninstallShortCut
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(KEYPAD_AD_MEDIATION) ||
                    configUpdate.updatedKeys.contains(KEYPAD_AD_VISIBILITY)
                ) {
                    remoteConfig.activate().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Remote config updated and activated.")
                            notifyConfigChanged()
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update failed with code: ${error.code}", error)
            }
        })
    }

    private fun notifyConfigChanged() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_CONFIG_CHANGED))
    }

    private fun initMintegralSdk() {
        try {
            val sdk: MBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
            val map = sdk.getMBConfigurationMap(
                resources.getString(R.string.MINTEGRAL_APP_ID),
                resources.getString(R.string.MINTEGRAL_APP_KEY)
            )
            sdk.init(map, this, object : SDKInitStatusListener {
                override fun onInitSuccess() {
                    Log.d(logTagMintegral, "Mintegral SDK init success")
                }

                override fun onInitFail(errorMsg: String) {
                    Log.e(logTagMintegral, "Mintegral SDK init failed: $errorMsg")
                }
            })
        } catch (e: Exception) {
            Log.e(logTagMintegral, "Exception during Mintegral init", e)
        }
    }

    private suspend fun loadSuggestions() {
        try {
            withContext(Dispatchers.IO) {
                if (suggestionList.isEmpty()) {
                    suggestionList =
                        DataBaseCopyOperationsKt.getAllItems() as MutableList<SuggestionItems>
                    Log.d(
                        "DataBaseCopyOperations",
                        "Loaded suggestions. Size: ${suggestionList.size}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DataBaseCopyOperations", "Error loading suggestions", e)
        }
    }
}