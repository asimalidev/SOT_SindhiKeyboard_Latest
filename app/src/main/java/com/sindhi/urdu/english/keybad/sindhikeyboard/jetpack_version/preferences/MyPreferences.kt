package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.isDirectBootEnabled

class MyPreferences(context: Context) {

    // private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SR_keyboard", Context.MODE_PRIVATE)
    private val sharedPreferences: SharedPreferences

    init {
        val storageContext = if (isDirectBootEnabled(context)) {
            context
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createDeviceProtectedStorageContext() ?: context
            } else {
                context
            }
        }
        sharedPreferences = storageContext.getSharedPreferences("SR_keyboard", Context.MODE_PRIVATE)
    }

    fun getEnglish(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isEnglishEnabled, true)
    }

    fun getUrdu(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isUrduEnabled, false)
    }

    fun getSindhi(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isSindhiEnabled, true)
    }

    fun getArabic(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isArabicEnabled, false)
    }

    fun getPashto(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isPashtoEnabled, false)
    }

    fun getBangla(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isBanglaEnabled, false)
    }

    fun getNepali(): Boolean {
        return sharedPreferences.getBoolean(Preferences.isNepaliEnabled, false)
    }

    fun getShowNumbersRow(): Boolean {
        return sharedPreferences.getBoolean(Preferences.showNumbersRow, false)
    }

    fun getVibration(): Boolean {
        return sharedPreferences.getBoolean(Preferences.vibrate, false)
    }

    fun getVoiceTyping(): Boolean {
        return sharedPreferences.getBoolean(Preferences.voiceTyping, true)
    }

    fun getSound(): Boolean {
        return sharedPreferences.getBoolean(Preferences.playSound, false)
    }

    fun getSwipeToDelete(): Boolean {
        return sharedPreferences.getBoolean(Preferences.swipeToDelete, true)
    }

    fun getLongPressForSymbols(): Boolean {
        return sharedPreferences.getBoolean(Preferences.longPressForSymbols, true)
    }

    fun getShowPopUp(): Boolean {
        return sharedPreferences.getBoolean(Preferences.showPopup, false)
    }

    fun getTheme(): String {
        val currentTheme = sharedPreferences.getString(Preferences.themeKey, "AUTO")!!
        // ADD THIS LOG:
        Log.e("ThemeBug", "3. Prefs: Reading from disk -> $currentTheme")
        return currentTheme
    }

    fun setTheme(theme: CustomTheme) {
        // DO NOT use .apply() here. You must use .commit() so the separate
        // keyboard process can instantly read the updated file from storage!
        Log.e("ThemeBug", "2. Prefs: Writing to disk -> ${theme.name}")
        sharedPreferences.edit().putString(Preferences.themeKey, theme.name).commit()
    }

    fun getKeyboard(): String {
        return sharedPreferences.getString(Preferences.myKeyboard, "English")!!
    }

    //From 20 to 23 to 27 to 30
    fun getTextSize(): Int {
        return sharedPreferences.getInt(Preferences.keyTextSize, 20)
    }

    fun setTextSize(valueInt: Int) {
        sharedPreferences.edit().putInt(Preferences.keyTextSize, valueInt).apply()
    }

    fun setSoundRawFile(valueSoundName: Int) {
        sharedPreferences.edit().putInt(Preferences.soundRawFile, valueSoundName).apply()
    }

    /*fun getSoundRawFile(): Int {
        return sharedPreferences.getInt(Preferences.soundRawFile, R.raw.typewriter)
    }*/

    fun setVolumeFloat(valueFloat: Float) {
        sharedPreferences.edit().putFloat(Preferences.keyPressVolume, valueFloat).apply()
    }

    fun getVolumeFloat(): Float {
        return sharedPreferences.getFloat(Preferences.keyPressVolume, 10.0F)
    }

    fun setKeyboard(keyboardName: String) {
        sharedPreferences.edit().putString(Preferences.myKeyboard, keyboardName).apply()
    }

    // new Lines for removing "Preferences.kt" linkage with the settings and applicationClass

    fun setKeyPressSounds(playSound: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.playSound, playSound).apply()
    }

    fun setShowNumbersRow(showNumbersRow: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.showNumbersRow, showNumbersRow).apply()
    }

    fun setVibration(shouldVibrate: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.vibrate, shouldVibrate).apply()
    }

    fun setShowPopup(shouldShowPopup: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.showPopup, shouldShowPopup).apply()
    }

    fun setLongPressForSymbols(shouldLongPressForSymbols: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.longPressForSymbols, shouldLongPressForSymbols).apply()
    }

    fun setSwipeToDelete(swipeToDelete: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.swipeToDelete, swipeToDelete).apply()
    }

    fun setVoiceTyping(swipeToDelete: Boolean) {
        sharedPreferences.edit().putBoolean(Preferences.voiceTyping, swipeToDelete).apply()
    }
}
