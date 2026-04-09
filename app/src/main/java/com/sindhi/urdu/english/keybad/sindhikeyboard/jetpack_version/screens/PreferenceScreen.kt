package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import com.manual.mediation.library.sotadlib.activities.LanguageScreenOne
import com.manual.mediation.library.sotadlib.utilsGoogleAdsConsent.GoogleMobileAdsConsentManager
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.SettingItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.SettingsCategory
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.ToggleSwitch
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.moreApps
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.privacyPolicy
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.rateUs
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.shareApp
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.openAppInPlayStore
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.composeFeedBackEmail
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import androidx.core.net.toUri
import com.manual.mediation.library.sotadlib.utils.MyLocaleHelper
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.MyPrefHelper
import java.util.Locale

@Composable
fun PreferenceScreen(
    context: Context,
    requireActivity: Activity
) {
    val myPreferences = MyPreferences(context)
    val scrollState = rememberScrollState()
    val myContext = LocalContext.current
    val activity = myContext as NavigationActivity

    val languageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Logic taken exactly from your SettingFragment
        val currentLocale: Locale = context.resources.configuration.locale
        val language = currentLocale.language

        // Assuming MyPrefHelper and MyLocaleHelper are accessible here
        // If MyPreferences is the same as MyPrefHelper, use myPreferences instead
        MyPrefHelper(context).setSelectedLanguageCode(language)
        MyLocaleHelper.setLocale(context, language)

        requireActivity.apply {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            recreate()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context = context)

    Scaffold() { paddingValues ->
        Column {
            Column(
                modifier = Modifier
                    .fillMaxHeight(1f)
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(scrollState)) {

                SettingsCategory(stringResource(R.string.label_keyboard_settings))
                ToggleSwitch(
                    isChecked = myPreferences.getShowNumbersRow(),
                    text = stringResource(id = R.string.number_row),
                    drawableResource = R.drawable.ic_num_row) { isChecked ->
                    MyPreferences(context = context).setShowNumbersRow(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.showNumbersRow, isChecked) }*/
                }

                ToggleSwitch(
                    isChecked = myPreferences.getVibration(),
                    text = stringResource(id = R.string.vibration),
                    drawableResource = R.drawable.ic_vibrate_keypress
                ) { isChecked ->
                    MyPreferences(context = context).setVibration(isChecked)
                }

                ToggleSwitch(
                    isChecked = myPreferences.getSound(),
                    text = stringResource(id = R.string.soundOnKeyPress),
                    drawableResource = R.drawable.ic_sound_keypress) { isChecked ->
                    MyPreferences(context = context).setKeyPressSounds(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.playSound, isChecked) }*/
                }

                ToggleSwitch(
                    isChecked = myPreferences.getShowPopUp(),
                    text = stringResource(id = R.string.showPopUp),
                    drawableResource = R.drawable.ic_show_popup) { isChecked ->
                    MyPreferences(context = context).setShowPopup(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.showPopup, isChecked) }*/
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                ToggleSwitch(
                    isChecked = myPreferences.getLongPressForSymbols(),
                    text = stringResource(id = R.string.longPressForSymbols),
                    drawableResource = R.drawable.ic_long_press) { isChecked ->
                    MyPreferences(context = context).setLongPressForSymbols(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.longPressForSymbols, isChecked) }*/
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                ToggleSwitch(
                    isChecked = myPreferences.getSwipeToDelete(),
                    text = stringResource(id = R.string.gesture_delete),
                    drawableResource = R.drawable.ic_delete_swipe) { isChecked ->
                    MyPreferences(context = context).setSwipeToDelete(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.swipeToDelete, isChecked) }*/
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                ToggleSwitch(
                    isChecked = myPreferences.getVoiceTyping(),
                    text = stringResource(id = R.string.enableVoiceTyping),
                    drawableResource = R.drawable.ic_voice_typing) { isChecked ->
                    MyPreferences(context = context).setVoiceTyping(isChecked)
                    /*Preferences.edit { putBoolean(Preferences.voiceTyping, isChecked) }*/
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)



                SettingItem(
                    drawableResource = R.drawable.ic_manage_subscrption,
                    text = stringResource(R.string.label_manage_subscrptions)
                ) {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/account/subscriptions".toUri()
                    )
                    context.startActivity(browserIntent)
                }


                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsCategory(stringResource(R.string.shareFb))

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)


                SettingItem(
                    text = stringResource(id = R.string.label_app_language),
                    drawableResource = R.drawable.ic_language_updated
                ) {
                    // Create Intent
                    val intent = Intent(context, LanguageScreenOne::class.java)
                    intent.putExtra("comeFrom", "AppSettings")

                    // Launch using the launcher created at the top
                    languageLauncher.launch(intent)
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                SettingItem(
                    text = stringResource(id = R.string.label_app_update),
                    drawableResource = R.drawable.ic_update){
                    openAppInPlayStore(context)
                }


                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingItem(drawableResource = R.drawable.ic_share_new,
                    text = stringResource(R.string.label_share)) {
                    shareApp(context)
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingItem(drawableResource = R.drawable.ic_more_apps,
                    text = stringResource(R.string.more_apps)) {
                    moreApps(context)
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingItem(drawableResource = R.drawable.ic_feedback,
                    text = stringResource(R.string.menu_feedback)) {
                    composeFeedBackEmail(context)
                }

                if (googleMobileAdsConsentManager.isUserInConsentRequiredRegion()) {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingItem(drawableResource = R.drawable.ic_ads_consent,
                        text = stringResource(R.string.ads_consent),
                        onCheck = {
                            googleMobileAdsConsentManager.showPrivacyOptionsForm(requireActivity) { formError ->
                                if (formError != null) {
                                    Toast.makeText(context, formError.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingItem(drawableResource = R.drawable.ic_privacy_policy,
                    text = stringResource(R.string.privacy_policy)) {
                    privacyPolicy(activity)
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingItem(drawableResource = R.drawable.ic_rate_us,
                    text = stringResource(R.string.rate_us)) {
                    rateUs(context)
                }
            }
        }
    }
}
