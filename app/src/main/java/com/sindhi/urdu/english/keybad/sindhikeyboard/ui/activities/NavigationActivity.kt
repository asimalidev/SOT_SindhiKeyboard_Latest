package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.manual.mediation.library.sotadlib.activities.AppCompatBaseActivity
import com.manual.mediation.library.sotadlib.utilsGoogleAdsConsent.GoogleMobileAdsConsentManager
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityNavigationBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.google.firebase.messaging.FirebaseMessaging
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.HomeFragmentDirections
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.ACTION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION1
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION2
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.DESTINATION3
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_EXIT_APP
import kotlin.system.exitProcess

class NavigationActivity : AppCompatBaseActivity() {

    private lateinit var binding: ActivityNavigationBinding

    private lateinit var navController: NavController

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private var isPurchased: Boolean? = null

    private var action: String? = null
    private var updateDialog: AlertDialog? = null
    private lateinit var appUpdateManager: AppUpdateManager
    private var isNativeUpdatePrompted = false

    private val updateFlowLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            showMandatoryUpdateDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 1. Use PADDING for the top so the background color extends behind the time bar
            v.setPadding(0, insets.top, 0, 0)

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = 0 // 2. Remove the top margin!
            }
            WindowInsetsCompat.CONSUMED
        }
        hideSystemUI()


        appUpdateManager = AppUpdateManagerFactory.create(this)
        action = intent?.getStringExtra(ACTION)
        Log.d("action", "action NavigationActivity:$action ")
        binding.inclToolBar.clSubDiv.let { subDiv ->
            ViewCompat.setOnApplyWindowInsetsListener(subDiv) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        isPurchased = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PURCHASE, false)

        // Set navigation graph start destination
        navController = findNavController(R.id.nav_host_fragment_content_navigation)
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        val moveTo = intent?.getStringExtra("MoveTo")

        val startDest = when {
            action == DESTINATION1 -> R.id.nav_editor
            action == DESTINATION3 -> R.id.themesFragment
            moveTo == "Settings" -> R.id.settingsFragment
            moveTo == "themes" -> R.id.themesFragment
            moveTo == "sindhi_status" -> R.id.navSindhiStatus
            moveTo == "sindhi_editor" -> R.id.nav_editor
            moveTo == "text_translator" -> R.id.navTextTranslator
            moveTo =="speech_to_text" -> R.id.speechFragment
            moveTo =="ibadat" -> R.id.navIbadatFragment
            else -> R.id.nav_home
        }

        Log.d("action", "Setting Start Destination to: $startDest")
        navGraph.setStartDestination(startDest)
        navController.graph = navGraph
        intent?.removeExtra(ACTION)
        intent?.removeExtra("MoveTo")
        action = null
        navController.graph = navGraph
    }



    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars()) // changed to show()
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    fun hidetoolbar(){
        binding.inclToolBar.clMain.visibility=View.GONE
    }

    fun showtoolbar(){
        binding.inclToolBar.clMain.visibility=View.VISIBLE
    }

    private fun showMandatoryUpdateDialog() {
        if (updateDialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_update, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(false) // Prevents clicking outside to dismiss

        updateDialog = builder.create()

        // Crucial: Remove default background so rounded corners work
        updateDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnOk.setOnClickListener {
            openPlayStore()
            updateDialog?.dismiss()
        }

        btnCancel.setOnClickListener {
            updateDialog?.dismiss()
            finishAffinity()
            exitProcess(0)
        }

        updateDialog?.show()
        val displayMetrics = resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.90).toInt()
        updateDialog?.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume native update if it was already downloading
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateFlowLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } catch (e: Exception) {
                    Log.e("AppUpdate", "Resume update flow failed", e)
                }
            }
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (!isNativeUpdatePrompted) {
                    // Show Native Google Play Update FIRST
                    isNativeUpdatePrompted = true
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateFlowLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build() // Full Screen Immediate
                        )
                    } catch (e: Exception) {
                        // Fallback if native fails to launch
                        showMandatoryUpdateDialog()
                    }
                } else {
                    // They already bypassed the native update during this session.
                    // Keep them locked in the custom dialog.
                    showMandatoryUpdateDialog()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.enabledInputMethodList.toString().contains(packageName)) {
                if (checkIfKeyboardEnabled()) {
                    if (!isInputMethodEnabled()) {
                        startActivity(
                            Intent(
                                this@NavigationActivity,
                                KeyboardSelectionActivity::class.java
                            )
                        )
                    }
                }
            }
        }
    }

}





