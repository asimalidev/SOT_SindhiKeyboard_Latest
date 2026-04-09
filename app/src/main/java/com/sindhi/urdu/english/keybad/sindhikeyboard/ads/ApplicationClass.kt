package com.sindhi.urdu.english.keybad.sindhikeyboard.ads

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.launch
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings



class ApplicationClass : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var applicationClass: ApplicationClass

        val firebaseAnalyticsEventsLog: FirebaseAnalytics by lazy {
            FirebaseAnalytics.getInstance(applicationClass)
        }

        var selectedTheme: CustomTheme? = null
        const val logTagAdmob = "NativeAdKeyboardAdmob"
        var imageUriUrduEditorBackgrounds: Uri? = null
        const val logTagMintegral = "NativeAdKeyboardMintegral"
        const val ACTION_CONFIG_CHANGED = "com.sindhi.urdu.english.keybad.CONFIG_CHANGED"
        private const val TAG = "ApplicationClass"
        var suggestionList = mutableListOf<SuggestionItems>()
    }

    override fun onCreate() {
        super.onCreate()
        applicationClass = this

//        FirebaseApp.initializeApp(this) // <-- RESTORED

        // 2. Use your shared scope to launch background tasks
        applicationScope.launch {
            initShortCut()
            setupRemoteConfig() // <-- RESTORED
            preloadSuggestionListIfNeeded()
        }
    }

    private fun initShortCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                val manager = getSystemService(ShortcutManager::class.java) ?: return
                manager.removeAllDynamicShortcuts()

                // 1. AI Clean
                val aiCleanShortCut = ShortcutInfo.Builder(this@ApplicationClass, "action_ai_clean")
                    .setShortLabel(getString(R.string.sindhi_stickers))
                    .setIcon(Icon.createWithResource(this@ApplicationClass, R.drawable.ic_sindhi_stickers))
                    .setIntent(Intent(this@ApplicationClass, FOFStartActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("ACTION", "DESTINATION2")
                    })
                    .setRank(1)
                    .build()

                // 2. Battery Info
                val batteryShortCut = ShortcutInfo.Builder(this@ApplicationClass, "DESTINATION3")
                    .setShortLabel(getString(R.string.change_theme))
                    .setIcon(Icon.createWithResource(this@ApplicationClass, R.drawable.ic_change_theme))
                    .setIntent(Intent(this@ApplicationClass, FOFStartActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("ACTION", "DESTINATION3")
                    })
                    .setRank(2)
                    .build()

                // 3. Uninstall Shortcut
                val uninstallShortCut = ShortcutInfo.Builder(this@ApplicationClass, "ACTION_OPEN_UNINSTALL")
                    .setShortLabel(getString(R.string.uninstall))
                    .setIcon(Icon.createWithResource(this@ApplicationClass, R.drawable.ic_uninstall))
                    .setIntent(Intent(this@ApplicationClass, FOFStartActivity::class.java).apply {
                        action = "android.intent.action.SHORTCUT_UNINSTALL_APP"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("FROM_SHORTCUT", "ACTION_OPEN_UNINSTALL")
                    })
                    .setRank(3)
                    .build()

                manager.dynamicShortcuts = listOf(aiCleanShortCut, batteryShortCut, uninstallShortCut)

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
                if (configUpdate.updatedKeys.contains("KEYPAD_AD_MEDIATION") || // Replaced constant with string for safety if it was declared outside
                    configUpdate.updatedKeys.contains("KEYPAD_AD_VISIBILITY")
                ) {
                    remoteConfig.activate().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Remote config updated and activated.")
                            LocalBroadcastManager.getInstance(this@ApplicationClass).sendBroadcast(Intent(ACTION_CONFIG_CHANGED))
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update failed with code: ${error.code}", error)
            }
        })
    }



    private suspend fun preloadSuggestionListIfNeeded() {
        if (suggestionList.isEmpty()) {
            try {
                val items = DataBaseCopyOperationsKt.getAllItems()
                withContext(Dispatchers.Main) {
                    suggestionList = items.toMutableList()
                    Log.e("DataBaseCopyOperations", "Loaded SuggestionList: ${suggestionList.size}")
                }
            } catch (e: Exception) {
                Log.e("DataBaseCopyOperations", "Failed to preload suggestions", e)
            }
        }
    }
}


