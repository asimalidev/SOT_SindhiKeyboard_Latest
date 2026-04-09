package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.DictionaryObject.audioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun getSelectedLanguage(context: Context): String {
    val myPreferences = MyPreferences(context)
    return myPreferences.getKeyboard()
}

fun vibrate(vibrator: Vibrator?, isLongPressed: Boolean) {
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            it.vibrate(
                if (isLongPressed) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
            )
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(40)
        }
    }
}



fun isTextFieldEmpty(inputConnection: InputConnection?): Boolean {
    val extractedText = inputConnection?.getExtractedText(ExtractedTextRequest(), 0)
    return extractedText?.text?.isEmpty() ?: true
}

fun getCurrentTextFieldText(inputConnection: InputConnection?): String {
    return inputConnection?.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString() ?: ""
}



fun isTextFieldEmptyVariant(inputConnection: InputConnection?): Boolean {
    inputConnection?.let { it1 ->
        val textBeforeCursor = it1.getTextBeforeCursor(2, 0)?.takeIf { it.isNotEmpty() }
        val textAfterCursor = it1.getTextAfterCursor(1, 0)?.takeIf { it.isNotEmpty() }
        return textBeforeCursor.isNullOrEmpty() && textAfterCursor.isNullOrEmpty()
    }
    return false
}

/*fun isTextFieldEmptyVariant(inputConnection: InputConnection?): Boolean {
    if (inputConnection != null) {
        val textBeforeCursor = inputConnection.getTextBeforeCursor(2, 0)
        val textAfterCursor = inputConnection.getTextAfterCursor(1, 0)
        val result = textBeforeCursor?.length == 1
        return (result || textBeforeCursor.isNullOrEmpty()) && textAfterCursor.isNullOrEmpty()
    }
    return false
}*/

fun selectKeyboard(context: Context) {
    val imm = context.getSystemService(InputMethodService.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showInputMethodPicker()
}

fun enableKeyboard(context: Context) {
    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)

}

fun playClick(label: String, context: Context, resId: Int, volumeFloat: Float) {
    val tap: MediaPlayer = MediaPlayer.create(context, resId)
    /*tap.setVolume(0.1f, 0.1f)
    tap.setVolume(1.0f, 1.0f)*/
    tap.setVolume(volumeFloat / 10, volumeFloat / 10)
    tap.start()
    tap.setOnCompletionListener { mp -> mp.release() }
    /*
    val am: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    when (label) {
        LABEL_SPACE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 1f)
        LABEL_DONE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN, 1f)
        LABEL_DELETE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 1f)
        else -> am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1f)
    }*/
}

/*fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri?) {
    if (uri != null) {
        customTabsIntent.launchUrl(activity, uri)
    }
}*/

fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri?) {
    val packageName = "com.android.chrome"
    val packageManager = activity.packageManager

    if (uri != null) {
        if (isPackageInstalled(packageName, packageManager)) {
            customTabsIntent.intent.setPackage(packageName)
            uri.let {
                try {
                    customTabsIntent.launchUrl(activity, it)
                } catch (e: ActivityNotFoundException) {
                    // Handle the case where launching CustomTab failed
                    val intent = Intent(Intent.ACTION_VIEW, it)
                    activity.startActivity(intent)
                }
            }
        } else {
            // Fallback to default browser
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        }
    }
}

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun rateUs(context: Context) {
    val marketUri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
    marketIntent.flags = FLAG_ACTIVITY_NEW_TASK
    context.startActivity(marketIntent)
}

fun shareApp(context: Context) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(
        Intent.EXTRA_TEXT,
        "https://play.google.com/store/apps/details?id=${context.packageName}"
    )
    sendIntent.type = "text/plain"
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Try New App")
    val myIntent = Intent.createChooser(sendIntent, "Share via")
    myIntent.flags = FLAG_ACTIVITY_NEW_TASK
    context.startActivity(myIntent)
}

fun moreApps(context: Context) {
    val marketUri = Uri.parse("https://play.google.com/store/apps/dev?id=7125744562568053772")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
    marketIntent.flags = FLAG_ACTIVITY_NEW_TASK
    context.startActivity(marketIntent)
}

//fun composeFeedBackEmail(context: Context) {
//    val emailIntent = Intent(Intent.ACTION_SENDTO)
//    emailIntent.data = Uri.parse("mailto:appsol.soloftech@gmail.com")
//    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feed Back")
//    try {
//        context.startActivity(Intent.createChooser(emailIntent, "Send email using..."))
//    } catch (e: Exception) {
//        Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
//    }
//}

 fun composeFeedBackEmail(context: Context) {

    // 1. Get App Name and Version Code
    var appName = "App"
    var versionName: String? = "1.0"
    try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        // Get App Name
        appName = context.applicationInfo.loadLabel(packageManager).toString()
        // Get Version Name
        versionName = packageInfo.versionName
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val manufacturer = android.os.Build.MANUFACTURER // e.g., Samsung
    val model = android.os.Build.MODEL // e.g., SM-G960F
    val androidVersion = android.os.Build.VERSION.RELEASE // e.g., 12
    val sdkVersion = android.os.Build.VERSION.SDK_INT // e.g., 31

    val subject = "Feedback: $appName v$versionName"

    val emailBody = """
    
    
    ------------------------------
    Device Info:
    Device: $manufacturer $model
    OS: Android $androidVersion (SDK $sdkVersion)
    App Version: $versionName
    """.trimIndent()

    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("farihazafarusa@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    try {
        context.startActivity(Intent.createChooser(emailIntent, "Send Feedback via Email"))

    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email clients installed", Toast.LENGTH_SHORT)
            .show()
    }
}

fun privacyPolicy(activity: Activity) {
    val url = "https://sites.google.com/view/privacypolicysoloftechappsol"
    val customIntent = CustomTabsIntent.Builder()
    customIntent.setToolbarColor(ContextCompat.getColor(activity, R.color.maroon_500))
    openCustomTab(activity, customIntent.build(), Uri.parse(url))
}

fun unsubscribe(activity: Activity) {
    val url = "https://support.google.com/googleplay/answer/7018481?co=GENIE.Platform%3DAndroid&hl=en"
    val customIntent = CustomTabsIntent.Builder()
   openCustomTab(
        activity,
        customIntent.build(),
        Uri.parse(url)
    )
}

fun termsOfService(activity: Activity) {
    val url = "https://sites.google.com/view/sindhi-keyboard-tos"
    val customIntent = CustomTabsIntent.Builder()
    openCustomTab(
        activity,
        customIntent.build(),
        Uri.parse(url)
    )
}

fun openAppInPlayStore(context: Context?) {
    val appPackageName =  context?.packageName // or packageName if in Activity
    try {
        // Try to open Play Store app
        context?.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
        )
    } catch (e: Exception) {
        // Fallback to browser
        context?.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

fun openSettings(context: Context?) {
    if (!(ForegroundCheckTask().execute(context).get())) {
        val intent = Intent(context, FOFStartActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MoveTo", "Settings")
        }
        context?.startActivity(intent)
    }
}

fun openThemes(context: Context?) {
    if (!(ForegroundCheckTask().execute(context).get())) {
        val intent = Intent(context, FOFStartActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MoveTo", "themes")
        }
        context?.startActivity(intent)
    }
}

fun onClick(context: Context?, vibrator: Vibrator?, key: Key, isLongPressed: Boolean = false, myPreferences: MyPreferences?) {

    // Handle the click event on the key
    /*if (myPreferences!!.getSound() && context != null) {
        playClick(context = context, label = key.labelMain, resId = myPreferences.getSoundRawFile(), volumeFloat = myPreferences.getVolumeFloat())
    }
    if (myPreferences.getVibration()) {
        vibrate(vibrator, isLongPressed)
    }*/
}

fun getIconResource(imeActionType: Int?): Int {
    return when (imeActionType) {
        EditorInfo.IME_ACTION_DONE -> R.drawable.done_icon
        EditorInfo.IME_ACTION_SEARCH -> R.drawable.search_icon
        EditorInfo.IME_ACTION_NEXT -> R.drawable.next_icon
        EditorInfo.IME_ACTION_SEND -> R.drawable.send_icon
        EditorInfo.IME_ACTION_GO -> R.drawable.go_icon
        EditorInfo.IME_ACTION_NONE -> R.drawable.keyboard_return
        EditorInfo.IME_ACTION_PREVIOUS -> R.drawable.previous_icon
        else -> R.drawable.keyboard_return
    }
}

fun switchToVoiceInputMethod(imeService: CustomImeService?, context: Context?): Boolean {

    imeService ?: return false
    context ?: return false

    imeService.startVoiceInput()
    /*val imm = imeService?.getSystemService(InputMethodManager::class.java) ?: return false
    // val list: List<InputMethodInfo> = imm.enabledInputMethodList
    val list: List<InputMethodInfo> = imm.inputMethodList
    var voiceIMEFound = false
    for (el in list) {
        for (i in 0 until el.subtypeCount) {
            if (el.getSubtypeAt(i).mode == "voice") {
                voiceIMEFound = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    imeService.switchInputMethod(el.id)
                    return true
                } else {
                    imeService.window.window?.let { window ->
                        @Suppress("DEPRECATION")
                        imm.setInputMethod(window.attributes.token, el.id)
                        return true
                    }
                }
            }
        }
    }
    if (!voiceIMEFound) {
        Toast.makeText(context, "No voice input method found. Please enable or install a voice input method.", Toast.LENGTH_SHORT).show()
    }*/
    return false
}

fun View.blockingClickListener(debounceTime: Long = 1500L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            val timeNow = SystemClock.elapsedRealtime()
            val elapsedTimeSinceLastClick = timeNow - lastClickTime
            Log.e("clickTag", """DebounceTime: 
                $debounceTime Time Elapsed: 
                $elapsedTimeSinceLastClick Is within debounce time: 
                ${elapsedTimeSinceLastClick < debounceTime}""".trimIndent())

            if (elapsedTimeSinceLastClick < debounceTime) {
                Log.e("clickTag", "Double click shielded")
                return
            }
            else {
                Log.e("clickTag", "Click happened")
                action()
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun isKeyboardEnabled(context: Context): Boolean {
    val packageName = context.packageName
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val enabledInputMethods: List<InputMethodInfo> = inputMethodManager.enabledInputMethodList ?: return false
    return enabledInputMethods.any { it.packageName == packageName }
}

fun isKeyboardSelected(context: Context): Boolean {
    val packageName = context.packageName
    val selectedInputMethod = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    return selectedInputMethod?.contains(packageName) == true
}

fun onClickSound(context: Context?, myPreferences: MyPreferences?) {
    if (myPreferences?.getSound() == true && context != null) {
        if (audioManager == null) {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
        CoroutineScope(Dispatchers.IO).launch {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5f)
        }
    }
}

fun onClickVibrate(context: Context?, isLongPressed: Boolean = false, myPreferences: MyPreferences?) {
    if (context == null) {
        Log.e("Vibrate", "Context is null")
        return
    }
    if (myPreferences?.getVibration() == true) {
        CoroutineScope(Dispatchers.Main).launch {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator == null) {
                Log.e("Vibrate", "Vibrator is null")
                return@launch
            }
            vibrate(vibrator, isLongPressed)
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> tryOrNull(block: () -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (_: Throwable) {
        null
    }
}

fun interface OnSystemSettingsChangedListener {
    fun onChanged()
}

class SystemSettingsObserver(
    context: Context,
    private val listener: OnSystemSettingsChangedListener,
) : ContentObserver(Handler(context.mainLooper)) {

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    override fun onChange(selfChange: Boolean) {
        listener.onChanged()
    }
}

abstract class AndroidSettingsHelper {
    abstract fun getString(context: Context, key: String): String?

    abstract fun getUriFor(key: String): Uri?

    private fun observe(context: Context, key: String, observer: SystemSettingsObserver) {
        getUriFor(key)?.let { uri ->
            context.contentResolver.registerContentObserver(uri, false, observer)
            observer.dispatchChange(false, uri)
        }
    }

    private fun removeObserver(context: Context, observer: SystemSettingsObserver) {
        context.contentResolver.unregisterContentObserver(observer)
    }

    @Composable
    fun <R> observeAsState(
        key: String,
        foregroundOnly: Boolean = false,
        transform: (String?) -> R,
    ): State<R> {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current.applicationContext
        val state = remember(key) { mutableStateOf(transform(getString(context, key))) }
        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = SystemSettingsObserver(context) {
                state.value = transform(getString(context, key))
            }
            if (foregroundOnly) {
                val eventObserver = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            observe(context, key, observer)
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            removeObserver(context, observer)
                        }

                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(eventObserver)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(eventObserver)
                    removeObserver(context, observer)
                }
            } else {
                observe(context, key, observer)
                onDispose {
                    removeObserver(context, observer)
                }
            }
        }
        return state
    }
}

object AndroidSettings {
    val Secure = object : AndroidSettingsHelper() {
        override fun getString(context: Context, key: String): String? {
            return tryOrNull { Settings.Secure.getString(context.contentResolver, key) }
        }

        override fun getUriFor(key: String): Uri? {
            return tryOrNull { Settings.Secure.getUriFor(key) }
        }
    }
}