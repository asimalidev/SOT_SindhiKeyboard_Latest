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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.disableKeyboard
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.MyPrefHelper
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class) // Add this annotation here
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
        MyPrefHelper(context).setSelectedLanguageCode(language)
        MyLocaleHelper.setLocale(context, language)

        requireActivity.apply {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            recreate()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    val screenBackgroundColor = Color(0xFFF2F2F2) // Light gray for headers and empty space
    val itemBackgroundColor = Color.White
    val dividerColor = Color(0xFFE0E0E0)

    val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context = context)

//    Scaffold() { paddingValues ->
//        Column {
//            Column(
//                modifier = Modifier
//                    .fillMaxHeight(1f)
//                    .background(colorResource(id = R.color.home_bg_color))
//                    .padding(paddingValues)
//                    .verticalScroll(scrollState)) {
//
//                SettingsCategory(stringResource(R.string.label_keyboard_settings))
//                ToggleSwitch(
//                    isChecked = myPreferences.getShowNumbersRow(),
//                    text = stringResource(id = R.string.number_row),
//                    drawableResource = R.drawable.ic_num_row) { isChecked ->
//                    MyPreferences(context = context).setShowNumbersRow(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.showNumbersRow, isChecked) }*/
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getVibration(),
//                    text = stringResource(id = R.string.vibration),
//                    drawableResource = R.drawable.ic_vibrate_keypress
//                ) { isChecked ->
//                    MyPreferences(context = context).setVibration(isChecked)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getSound(),
//                    text = stringResource(id = R.string.soundOnKeyPress),
//                    drawableResource = R.drawable.ic_sound_keypress) { isChecked ->
//                    MyPreferences(context = context).setKeyPressSounds(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.playSound, isChecked) }*/
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getShowPopUp(),
//                    text = stringResource(id = R.string.showPopUp),
//                    drawableResource = R.drawable.ic_show_popup) { isChecked ->
//                    MyPreferences(context = context).setShowPopup(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.showPopup, isChecked) }*/
//                }
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getLongPressForSymbols(),
//                    text = stringResource(id = R.string.longPressForSymbols),
//                    drawableResource = R.drawable.ic_long_press) { isChecked ->
//                    MyPreferences(context = context).setLongPressForSymbols(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.longPressForSymbols, isChecked) }*/
//                }
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getSwipeToDelete(),
//                    text = stringResource(id = R.string.gesture_delete),
//                    drawableResource = R.drawable.ic_delete_swipe) { isChecked ->
//                    MyPreferences(context = context).setSwipeToDelete(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.swipeToDelete, isChecked) }*/
//                }
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                ToggleSwitch(
//                    isChecked = myPreferences.getVoiceTyping(),
//                    text = stringResource(id = R.string.enableVoiceTyping),
//                    drawableResource = R.drawable.ic_voice_typing) { isChecked ->
//                    MyPreferences(context = context).setVoiceTyping(isChecked)
//                    /*Preferences.edit { putBoolean(Preferences.voiceTyping, isChecked) }*/
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//
//                SettingItem(
//                    text = stringResource(id = R.string.label_disable_keyboard),
//                    drawableResource = R.drawable.ic_disable_btn){
//
//                    disableKeyboard(context)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//
//
//                SettingItem(
//                    drawableResource = R.drawable.ic_manage_subscrption,
//                    text = stringResource(R.string.label_manage_subscrptions)
//                ) {
//                    val browserIntent = Intent(
//                        Intent.ACTION_VIEW,
//                        "https://play.google.com/store/account/subscriptions".toUri()
//                    )
//                    context.startActivity(browserIntent)
//                }
//
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                SettingsCategory(stringResource(R.string.shareFb))
//
//
//
//                SettingItem(
//                    text = stringResource(id = R.string.label_app_language),
//                    drawableResource = R.drawable.ic_language_updated
//                ) {
//                    // Create Intent
//                    val intent = Intent(context, LanguageScreenOne::class.java)
//                    intent.putExtra("comeFrom", "AppSettings")
//
//                    // Launch using the launcher created at the top
//                    languageLauncher.launch(intent)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                SettingItem(
//                    text = stringResource(id = R.string.label_app_update),
//                    drawableResource = R.drawable.ic_update){
//                    openAppInPlayStore(context)
//                }
//
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//
//                SettingItem(drawableResource = R.drawable.ic_share_settings,
//                    text = stringResource(R.string.label_share)) {
//                    shareApp(context)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                SettingItem(drawableResource = R.drawable.ic_more_apps,
//                    text = stringResource(R.string.more_apps)) {
//                    moreApps(context)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                SettingItem(drawableResource = R.drawable.ic_feedback,
//                    text = stringResource(R.string.menu_feedback)) {
//                    composeFeedBackEmail(context)
//                }
//
//                if (googleMobileAdsConsentManager.isUserInConsentRequiredRegion()) {
//                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                    SettingItem(drawableResource = R.drawable.ic_ads_consent,
//                        text = stringResource(R.string.ads_consent),
//                        onCheck = {
//                            googleMobileAdsConsentManager.showPrivacyOptionsForm(requireActivity) { formError ->
//                                if (formError != null) {
//                                    Toast.makeText(context, formError.message, Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        })
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                SettingItem(drawableResource = R.drawable.ic_privacy_policy,
//                    text = stringResource(R.string.privacy_policy)) {
//                    privacyPolicy(activity)
//                }
//
//                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
//                SettingItem(drawableResource = R.drawable.ic_rate_us,
//                    text = stringResource(R.string.rate_us)) {
//                    rateUs(context)
//                }
//            }
//        }
//    }

    Scaffold{ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 1. Set the MAIN background to light gray
                .background(screenBackgroundColor)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            // --- KEYBOARD SETTINGS SECTION ---
            SettingsCategory(stringResource(R.string.label_keyboard_settings))

            // 2. Group items in a Column with a WHITE background
            Column(modifier = Modifier.background(itemBackgroundColor)) {

                ToggleSwitch(
                    isChecked = myPreferences.getShowNumbersRow(),
                    text = stringResource(id = R.string.number_row),
                    drawableResource = R.drawable.ic_num_row
                ) { isChecked ->
                    myPreferences.setShowNumbersRow(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getVibration(),
                    text = stringResource(id = R.string.vibration),
                    drawableResource = R.drawable.ic_vibrate_keypress
                ) { isChecked ->
                    myPreferences.setVibration(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getSound(),
                    text = stringResource(id = R.string.soundOnKeyPress),
                    drawableResource = R.drawable.ic_sound_keypress
                ) { isChecked ->
                    myPreferences.setKeyPressSounds(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getShowPopUp(),
                    text = stringResource(id = R.string.showPopUp),
                    drawableResource = R.drawable.ic_show_popup
                ) { isChecked ->
                    myPreferences.setShowPopup(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getLongPressForSymbols(),
                    text = stringResource(id = R.string.longPressForSymbols),
                    drawableResource = R.drawable.ic_long_press
                ) { isChecked ->
                    myPreferences.setLongPressForSymbols(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getSwipeToDelete(),
                    text = stringResource(id = R.string.gesture_delete),
                    drawableResource = R.drawable.ic_delete_swipe
                ) { isChecked ->
                    myPreferences.setSwipeToDelete(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                ToggleSwitch(
                    isChecked = myPreferences.getVoiceTyping(),
                    text = stringResource(id = R.string.enableVoiceTyping),
                    drawableResource = R.drawable.ic_voice_typing
                ) { isChecked ->
                    myPreferences.setVoiceTyping(isChecked)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    text = stringResource(id = R.string.label_disable_keyboard),
                    drawableResource = R.drawable.ic_disable_btn
                ) {
                    disableKeyboard(context)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                // Add History and Favourite here to match the image if they exist in your app

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
            }


            // --- SHARE & FEEDBACK SECTION ---
            SettingsCategory(stringResource(R.string.shareFb))

            // 3. Group the next set of items in another WHITE container
            Column(modifier = Modifier.background(itemBackgroundColor)) {

                SettingItem(
                    text = stringResource(id = R.string.label_app_language),
                    drawableResource = R.drawable.ic_language_updated
                ) {
                    val intent = Intent(context, LanguageScreenOne::class.java)
                    intent.putExtra("comeFrom", "AppSettings")
                    languageLauncher.launch(intent)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    text = stringResource(id = R.string.label_app_update),
                    drawableResource = R.drawable.ic_update
                ) {
                    openAppInPlayStore(context)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    drawableResource = R.drawable.ic_share_settings,
                    text = stringResource(R.string.label_share)
                ) {
                    shareApp(context)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    drawableResource = R.drawable.ic_rate_us,
                    text = stringResource(R.string.rate_us)
                ) {
                    rateUs(context)
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    drawableResource = R.drawable.ic_feedback,
                    text = stringResource(R.string.menu_feedback)
                ) {
                    composeFeedBackEmail(context)
                }

                if (googleMobileAdsConsentManager.isUserInConsentRequiredRegion()) {
                    Divider(color = dividerColor, thickness = 1.dp)
                    SettingItem(
                        drawableResource = R.drawable.ic_ads_consent,
                        text = stringResource(R.string.ads_consent),
                        onCheck = {
                            googleMobileAdsConsentManager.showPrivacyOptionsForm(requireActivity) { formError ->
                                if (formError != null) {
                                    Toast.makeText(context, formError.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                Divider(color = dividerColor, thickness = 1.dp)

                SettingItem(
                    drawableResource = R.drawable.ic_privacy_policy,
                    text = stringResource(R.string.privacy_policy)
                ) {
                    privacyPolicy(activity)
                }
            }

            // Add some padding at the bottom so the last item isn't flush against the screen edge
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

