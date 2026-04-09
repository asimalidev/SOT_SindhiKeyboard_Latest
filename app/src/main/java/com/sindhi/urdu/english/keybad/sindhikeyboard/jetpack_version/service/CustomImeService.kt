package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.inputmethodservice.InputMethodService
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mbridge.msdk.out.BannerAdListener
import com.mbridge.msdk.out.BannerSize
import com.mbridge.msdk.out.MBBannerView
import com.mbridge.msdk.out.MBridgeIds
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.logTagAdmob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.logTagMintegral
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.KeyboardState
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_ARABIC
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_BANGLA
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_COPY
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DELETE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DOC_END
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DOC_START
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DONE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DOWN
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_ENGLISH
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_NEPALI
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_NEXT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_PASHTO
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_PASTE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_PREVIOUS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SELECT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SELECT_All
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SINDHI
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SPACE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_UP
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_URDU
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.NUM_CHAR_BEFORE_CURSOR
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.getSelectedLanguage
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.LayoutState
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.selectKeyboard
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.MyKeyboard
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.AdObject.nativeAdMobHashMapKeypad
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.ForegroundCheckTask
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.AdObject.keyPadBannerFailedShowTimeAdmob
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.AdObject.keyPadBannerFailedShowTimeMintegral
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.AdObject.nativeMintegralHashMapKeypad
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.KEYPAD_AD_MEDIATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.KEYPAD_AD_VISIBILITY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_KEYPAD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(DelicateCoroutinesApi::class)
class CustomImeService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {

    var imeActionType: Int? = null

    lateinit var myContext: Context

    // THE FIX: Use 'by lazy' to prevent initialization during class creation
    private val serviceScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }

    var mainSuggestionList: MutableList<SuggestionItems> = mutableListOf()

    var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null


    private var adManagerStarted = false
    private val DATABASE_NAME = "dictionary.db"

    private val databasePath: String
        get() = myContext.getDatabasePath(DATABASE_NAME).path

    var currentWordMain = ""

    var filterList: MutableState<List<SuggestionItems>?> = mutableStateOf(null)

    var speechRecognizer: SpeechRecognizer? = null

    var isKeyboardOpen by mutableStateOf(false)
    var getInAppPurchases by mutableIntStateOf(0)
    var getRemoteConfig by mutableIntStateOf(0)
    var getRemoteConfigVisibility by mutableIntStateOf(0)
    var getRemoteConfigAdmob by mutableIntStateOf(0)
    var getRemoteConfigMintegral by mutableIntStateOf(0)

    fun init(context: Context) {
        if (!::myContext.isInitialized) {
            myContext = context.applicationContext
        } else {
            Log.w("DataBaseCopyOperations", "Context is already initialized.")
        }
    }

    private fun validatePackageName(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) {
            return false
        }
        val packageName = editorInfo.packageName ?: return false
        val inputBinding = currentInputBinding
        if (inputBinding == null) {
            Log.e(
                "TAG",
                "inputBinding should not be null here. " + "You are likely to be hitting b.android.com/225029"
            )
            return false
        }
        val packageUid = inputBinding.uid
        val packageManager = packageManager
        val possiblePackageNames = packageManager.getPackagesForUid(packageUid)
        for (possiblePackageName in possiblePackageNames!!) {
            if (packageName == possiblePackageName) {
                return true
            }
        }
        return false
    }

    private var selectionUp: Int = -1
        set(value) {
            if (value >= -1) {
                field = value
            }
        }
    private var selectionPrevious: Int = -1
        set(value) {
            if (value >= -1) {
                field = value
            }
        }
    private var selectionNext: Int = -1
        set(value) {
            if (value >= -1) {
                field = value
            }
        }
    private var selectionDown: Int = -1
        set(value) {
            if (value >= -1) {
                field = value
            }
        }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.i("CustomImeService", "onStartInputView()")

        if (info == null) {
            Log.e("CustomImeService", "EditorInfo is null in onStartInputView")
            return
        }


        if (!adManagerStarted) {
            // USE serviceScope, don't create a new CoroutineScope() here
            serviceScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        Log.e("AdManagerDebug", "Fetching ad config...")
                        fetchAdIDS(this@CustomImeService)
                    }
                    adManagerStarted = true
                } catch (e: Exception) {
                    Log.e("AdManagerDebug", "Error fetching Ad IDs", e)
                }
            }
        }

        Log.e("CustomImeServicePackageName", info.packageName)
        isKeyboardOpen = if (
            info.packageName.lowercase(Locale.ROOT).contains("browser") ||
            info.packageName.lowercase(Locale.ROOT).contains("google")
        ) {
            false
        } else {
            isFromGoogleServices(info.packageName)
        }

        // ✅ New: detect if number input → switch keyboard layout
        val isNumberInput = when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> true

            else -> false
        }

        if (isNumberInput) {
            Log.i("CustomImeService", "Switching to Numbers layout")
            KeyboardState.currentLayout.value = LayoutState.Numbers
        } else {
            KeyboardState.currentLayout.value = LayoutState.Main
        }

        // ✅ Store imeActionType
        imeActionType = info.imeOptions and EditorInfo.IME_MASK_ACTION
    }

//    override fun onCreate() {
//        super.onCreate() // View inflation happens here
//        Log.i("CustomImeService", "onCreate()")
//        try {
//            savedStateRegistryVar.performRestore(null)
//            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
//
//            // MOVED EVERYTHING TO THE BACKGROUND
//            serviceScope.launch(Dispatchers.IO) {
//                try {
//                    // 1. Initialize DB off the main thread
//                    DataBaseCopyOperationsKt.init(this@CustomImeService)
//                    MobileAds.initialize(this@CustomImeService) { initializationStatus ->
//                        Log.i("AdManagerDebug", "AdMob pre-warmed in background")
//                    }
//
//                    // 2. Read values from DB/Disk
//                    val iap = DataBaseCopyOperationsKt.getInAppPurchases()
//                    val configVis = DataBaseCopyOperationsKt.getRemoteConfigVisibility()
//                    val configAdmob = DataBaseCopyOperationsKt.getRemoteConfigAdmob()
//                    val configMintegral = DataBaseCopyOperationsKt.getRemoteConfigMintegral()
//
//                    // 3. Update State on Main Thread for Compose
//                    withContext(Dispatchers.Main) {
//                        getInAppPurchases = iap
//                        getRemoteConfigVisibility = configVis
//                        getRemoteConfigAdmob = configAdmob
//                        getRemoteConfigMintegral = configMintegral
//                    }
//                } catch (e: Exception) {
//                    Log.e("CustomImeService", "Error loading config", e)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("CustomImeService", "Error in onCreate", e)
//        }
//    }

    override fun onCreate() {
        super.onCreate() // View inflation happens here
        Log.i("CustomImeService", "onCreate()")
        try {
            savedStateRegistryVar.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

            // 2. BACKGROUND THREAD: Database and Remote Config operations
            serviceScope.launch(Dispatchers.IO) {
                try {
                    DataBaseCopyOperationsKt.init(this@CustomImeService)

                    val iap = DataBaseCopyOperationsKt.getInAppPurchases()
                    val configVis = DataBaseCopyOperationsKt.getRemoteConfigVisibility()
                    val configAdmob = DataBaseCopyOperationsKt.getRemoteConfigAdmob()
                    val configMintegral = DataBaseCopyOperationsKt.getRemoteConfigMintegral()

                    // 3. Update State back on Main Thread for Compose
                    withContext(Dispatchers.Main) {
                        getInAppPurchases = iap
                        getRemoteConfigVisibility = configVis
                        getRemoteConfigAdmob = configAdmob
                        getRemoteConfigMintegral = configMintegral
                    }
                } catch (e: Exception) {
                    Log.e("CustomImeService", "Error loading config", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CustomImeService", "Error in onCreate", e)
        }
    }

    fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            if (NetworkCheck.isNetworkAvailable(applicationContext)) {
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    if (getSelectedLanguage(applicationContext) == "English") {
                        "eng"
                    } else {
                        "ur"
                    }
                )
            } else {
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something")

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                }
                speechRecognizer!!.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            openAppInfo(applicationContext)
        }
    }

    fun sendSticker(context: Context?, file: File?) {
        val editorInfo = currentInputEditorInfo
        if (!validatePackageName(editorInfo)) {
            return
        }

        val contentUri =
            FileProvider.getUriForFile(this, "${context!!.packageName}.fileprovider", file!!)
        val flag: Int
        if (Build.VERSION.SDK_INT >= 25) {
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        } else {
            flag = 0
            try {
                grantUriPermission(
                    editorInfo.packageName,
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: java.lang.Exception) {
                Log.e(
                    "TAG",
                    "grantUriPermission failed packageName=" + editorInfo.packageName + " contentUri=" + contentUri,
                    e
                )
            }
        }

        val inputContentInfoCompat = InputContentInfoCompat(
            contentUri,
            ClipDescription("TestSticker", arrayOf("image/webp.wasticker")),
            null
        )
        InputConnectionCompat.commitContent(
            currentInputConnection,
            currentInputEditorInfo,
            inputContentInfoCompat,
            flag,
            null
        )
    }

    private fun openAppInfo(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:" + context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onCreateInputView(): View {
        val view = ComposeKeyboardView(this)
        window?.window?.decorView?.let {
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeViewModelStoreOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
        }

        view.let {
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeViewModelStoreOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
        }
        return view
    }


    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f
            res.updateConfiguration(config, res.displayMetrics)
        }
        return res
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        Log.e("NativeAdKeyboard", "TimesToTime: setFalse")
        isKeyboardOpen = false
        filterList.value = emptyList()
        val intent = Intent("com.soloftech.RESET_KEYBOARD")
        baseContext.sendBroadcast(intent)
    }


    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        handler.removeCallbacks(deleteCharacterRunnable)
        handler.removeCallbacks(deleteEmojisRunnable)
        handler.removeCallbacks(previousRunnable)
        handler.removeCallbacks(nextRunnable)
        handler.removeCallbacks(upRunnable)
        handler.removeCallbacks(downRunnable)
        handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private var isBackspaceKeyPressed = false
    private var isDownKeyPressed = false
    private var isUpKeyPressed = false
    private var isNextKeyPressed = false
    private var isPreviousKeyPressed = false
    var isSelectionModeActive = false
    private val handler = Handler(Looper.getMainLooper())

    private var deleteCharacterRunnable = object : Runnable {
        override fun run() {
            doSomethingWith(Key(labelMain = LABEL_DELETE, 1f), false)
            handler.postDelayed(this, 40)
        }
    }

    // Runnable for handling backspace key long press for emojis
    private var deleteEmojisRunnable = object : Runnable {
        override fun run() {
            doSomethingWith(
                Key(labelMain = LABEL_DELETE, 1f),
                isLongPressed = false,
                isEmoji = true
            )
            handler.postDelayed(this, 30)
        }
    }

    // Runnable for handling previous key long press
    private var previousRunnable = object : Runnable {
        override fun run() {
            doEditText(Key(labelMain = LABEL_PREVIOUS, 1f), false)
            handler.postDelayed(this, 40)
        }
    }

    // Runnable for handling next key long press
    private var nextRunnable = object : Runnable {
        override fun run() {
            doEditText(Key(labelMain = LABEL_NEXT, 1f), false)
            handler.postDelayed(this, 40)

        }
    }

    // Runnable for handling up key long press
    private var upRunnable = object : Runnable {
        override fun run() {
            doEditText(Key(labelMain = LABEL_UP, 1f), false)
            handler.postDelayed(this, 40)
        }
    }

    // Runnable for handling down key long press
    private var downRunnable = object : Runnable {
        override fun run() {
            doEditText(Key(labelMain = LABEL_DOWN, 1f), false)
            handler.postDelayed(this, 40)
        }
    }

    fun commitEmoji(emoji: String) {
        serviceScope.launch {
            currentInputConnection?.commitText(emoji, 1)
        }
    }

    fun commitHusnEguftar(guftar: String) {
        GlobalScope.launch {
            currentInputConnection?.commitText(guftar, 1)
        }
    }

    fun commitText(
        text: Key,
        isCapsEnabled: Boolean,
        isCapsLockEnabled: Boolean,
        num: Int = 1,
        endsWithSpace: Boolean
    ) {
        GlobalScope.launch {
            val label = if (text.code != null) {
                Char(text.code).toString()
            } else {
                text.labelMain
            }
            val currentInputConn = currentInputConnection
            currentInputConn?.let {
                if (isCapsLockEnabled || isCapsEnabled) {
                    val upperCaseLabel = label.uppercase()
                    if (text.labelMain == "." || text.labelMain == ". ") {
                        text.labelMain = ". "
                        currentInputConn.commitText(text.labelMain.uppercase(), num)
                    } else {
                        currentInputConn.commitText(upperCaseLabel, num)
                    }
                } else {
                    if (text.labelMain == "." || text.labelMain == ". ") {
                        text.labelMain = ". "
                        currentInputConn.commitText(text.labelMain, num)
                    } else {
                        if (endsWithSpace) {
                            currentInputConn.commitText(label.uppercase(), num)
                        } else {
                            currentInputConn.commitText(label, num)
                        }
                    }
                }
            }
        }
    }

    suspend fun getCompleteTextForSuggestionAsync(): String {
        // Shift this IPC call to a background thread!
        return withContext(Dispatchers.IO) {
            try {
                currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString() ?: ""
            } catch (e: Exception) {
                Log.e("CustomImeService", "Failed to extract text", e)
                ""
            }
        }
    }

    // Function to handle EditText actions
    fun doEditText(key: Key, longPressed: Boolean) {
        GlobalScope.launch {
            val currentText = currentInputConnection?.getExtractedText(
                ExtractedTextRequest(),
                0
            )?.text?.toString()
            val textAfterCursor =
                currentInputConnection?.getTextAfterCursor(NUM_CHAR_BEFORE_CURSOR, 0)?.toString()
            val charsAfterCursorSelection = textAfterCursor?.length
            val totalTextLengthSelection = currentText?.length
            val myCursorPositionSelection =
                totalTextLengthSelection?.minus(charsAfterCursorSelection!!) ?: 0

            when (key.labelMain) {
                // Action to perform on clicking on "Copy"
                LABEL_COPY -> {
                    val charsAfterCursor = textAfterCursor?.length
                    val totalTextLength = currentText?.length
                    val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                    val selectedText: CharSequence? = currentInputConnection.getSelectedText(0)
                    if (selectedText != null && !TextUtils.isEmpty(selectedText)) {
                        val clipboard =
                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SelectedText", selectedText)
                        clipboard.setPrimaryClip(clip)
                        if (cursorPosition != null) {
                            currentInputConnection.setSelection(cursorPosition, cursorPosition)
                        }
                    }
                    if (selectionDown > 0 || selectionUp > 0 || selectionNext > 0 || selectionPrevious > 0) {
                        selectionDown = 0
                        selectionUp = 0
                        selectionPrevious = 0
                        selectionNext = 0
                    }
                    if (isSelectionModeActive) {
                        isSelectionModeActive = false
                    }
                }

                // Action to perform on clicking on "Paste"
                LABEL_PASTE -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = clipboardManager.primaryClip

                    if (clipData != null && clipData.itemCount > 0) {
                        val textToPaste = clipData.getItemAt(0).text.toString()
                        currentInputConnection.commitText(textToPaste, 1)
                    }
                    if (selectionDown > 0 || selectionUp > 0 || selectionNext > 0 || selectionPrevious > 0) {
                        selectionDown = 0
                        selectionUp = 0
                        selectionPrevious = 0
                        selectionNext = 0
                    }
                    if (isSelectionModeActive) {
                        isSelectionModeActive = false
                    }
                }

                // Action to perform on clicking on "Select All"
                LABEL_SELECT_All -> {
                    val charsAfterCursor = textAfterCursor?.length
                    val totalTextLength = currentText?.length
                    val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                    if (cursorPosition != null && currentText.isNotEmpty()) {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    }
                    if (selectionDown > 0 || selectionUp > 0 || selectionNext > 0 || selectionPrevious > 0) {
                        selectionDown = 0
                        selectionUp = 0
                        selectionPrevious = 0
                        selectionNext = 0
                    }
                    if (isSelectionModeActive) {
                        isSelectionModeActive = false
                    }
                }

                // Action to perform on clicking on "Previous"
                LABEL_PREVIOUS -> {
                    if (longPressed) {
                        if (!isPreviousKeyPressed) {
                            isPreviousKeyPressed = true
                            previousRunnable.run()
                        }
                    } else {
                        val charsAfterCursor = textAfterCursor?.length
                        val totalTextLength = currentText?.length
                        val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                        if (cursorPosition != null && currentText.isNotEmpty()) {
                            if (selectionPrevious == -1) {
                                selectionPrevious = cursorPosition - 1
                            }
                            if (isSelectionModeActive) {
                                currentInputConnection?.setSelection(
                                    selectionPrevious,
                                    myCursorPositionSelection
                                )
                                selectionPrevious -= 1
                            } else {
                                currentInputConnection?.setSelection(
                                    cursorPosition - 1,
                                    cursorPosition - 1
                                )
                            }
                        }
                    }
                }

                // Action to perform on clicking on "Next"
                LABEL_NEXT -> {
                    if (longPressed) {
                        if (!isNextKeyPressed) {
                            isNextKeyPressed = true
                            nextRunnable.run()
                        }
                    } else {
                        val charsAfterCursor = textAfterCursor?.length
                        val totalTextLength = currentText?.length
                        val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                        if (cursorPosition != null && currentText.isNotEmpty()) {

                            if (selectionNext == -1) {
                                selectionNext = cursorPosition + 1
                            }

                            if (isSelectionModeActive) {
                                currentInputConnection?.setSelection(
                                    myCursorPositionSelection,
                                    selectionNext
                                )
                                selectionNext += 1
                            } else {
                                currentInputConnection?.setSelection(
                                    cursorPosition + 1,
                                    cursorPosition + 1
                                )
                            }
                        }
                    }
                }

                // Action to perform on clicking on "Start"
                LABEL_UP -> {
                    if (longPressed) {
                        if (!isUpKeyPressed) {
                            isUpKeyPressed = true
                            upRunnable.run()
                        }
                    } else {
                        val charsAfterCursor = textAfterCursor?.length
                        val totalTextLength = currentText?.length
                        val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                        if (cursorPosition != null && currentText.isNotEmpty()) {
                            if (selectionUp == -1) {
                                selectionUp = cursorPosition - 40
                            }
                            if (isSelectionModeActive) {
                                currentInputConnection?.setSelection(
                                    selectionUp,
                                    myCursorPositionSelection
                                )
                                selectionUp -= 40
                            } else {
                                currentInputConnection?.setSelection(
                                    cursorPosition - 40,
                                    cursorPosition - 40
                                )
                            }
                        }
                    }
                }

                // Action to perform on clicking on "End"
                LABEL_DOWN -> {
                    if (longPressed) {
                        if (!isDownKeyPressed) {
                            isDownKeyPressed = true
                            downRunnable.run()
                        }
                    } else {
                        val charsAfterCursor = textAfterCursor?.length
                        val totalTextLength = currentText?.length
                        val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                        if (cursorPosition != null && currentText.isNotEmpty()) {
                            if (selectionDown == -1) {
                                selectionDown = cursorPosition + 40
                            }
                            if (isSelectionModeActive) {
                                currentInputConnection?.setSelection(
                                    myCursorPositionSelection,
                                    selectionDown
                                )
                                selectionDown += 40
                            } else {
                                currentInputConnection?.setSelection(
                                    cursorPosition + 40,
                                    cursorPosition + 40
                                )
                            }
                        }
                    }
                }

                // Action to perform on clicking on "Start"
                LABEL_DOC_START -> {
                    val charsAfterCursor = textAfterCursor?.length
                    val totalTextLength = currentText?.length
                    val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                    if (cursorPosition != null && currentText.isNotEmpty()) {

                        currentInputConnection?.setSelection(0, 0)

                    }
                }

                //Action to perform on clicking on "End"
                LABEL_DOC_END -> {
                    val charsAfterCursor = textAfterCursor?.length
                    val totalTextLength = currentText?.length
                    val cursorPosition = totalTextLength?.minus(charsAfterCursor!!)

                    if (cursorPosition != null && currentText.isNotEmpty()) {
                        if (isSelectionModeActive) {
                            currentInputConnection?.setSelection(cursorPosition, totalTextLength)
                        } else {
                            currentInputConnection?.setSelection(totalTextLength, totalTextLength)
                        }
                    }
                }

                //Action to perform on clicking on "Select"
                LABEL_SELECT -> {
                    isSelectionModeActive = !isSelectionModeActive
                    if (selectionDown > 0 || selectionUp > 0 || selectionNext > 0 || selectionPrevious > 0) {
                        selectionDown = 0
                        selectionUp = 0
                        selectionPrevious = 0
                        selectionNext = 0
                    }
                }
            }
        }
    }

    fun doSomethingWith(
        key: Key,
        isLongPressed: Boolean,
        isEmoji: Boolean = false,
        isSymbolEnabled: Boolean = true
    ) {
        try {
            GlobalScope.launch {
                val label = if (key.code != null) {
                    Char(key.code).toString()
                } else {
                    ""
                }
                val labelSecondary = if (key.codeSecondary != null) {
                    Char(key.codeSecondary).toString()
                } else {
                    ""
                }
                if (key.isLanguageKey && isLongPressed) {
                    selectKeyboard(context = baseContext)
                } else {
                    when (key.labelMain) {
                        LABEL_SPACE, LABEL_ENGLISH, LABEL_URDU, LABEL_PASHTO, LABEL_ARABIC, LABEL_SINDHI, LABEL_NEPALI, LABEL_BANGLA -> {
                            if (!isLongPressed) {
                                currentInputConnection?.commitText(label, 1)
                            }
                        }

                        LABEL_DONE -> {
                            currentInputConnection?.let {
                                it.sendKeyEvent(
                                    KeyEvent(
                                        KeyEvent.ACTION_DOWN,
                                        KeyEvent.KEYCODE_ENTER
                                    )
                                )
                            } ?: Log.e(
                                "CustomImeService",
                                "InputConnection is null when trying to send DONE key event"
                            )
                        }

                        LABEL_DELETE -> {
                            if (isLongPressed) {
                                if (!isBackspaceKeyPressed) {
                                    isBackspaceKeyPressed = true
                                    if (isEmoji) {
                                        deleteEmojisRunnable.run()
                                    } else {
                                        deleteCharacterRunnable.run()
                                    }
                                }
                            } else {
                                val inputConnection = currentInputConnection
                                if (inputConnection != null) {
                                    val selectedText: CharSequence? =
                                        inputConnection.getSelectedText(0)
                                    if (selectedText != null && !TextUtils.isEmpty(selectedText)) {
                                        inputConnection.commitText("", 1)
                                    } else {
                                        inputConnection.let {
                                            if (isEmoji) {
                                                inputConnection.deleteSurroundingText(2, 0)
                                            } else {
                                                inputConnection.deleteSurroundingText(1, 0)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("check", "InputConnection is null")
                                }
                            }

                            if (selectionDown > 0 || selectionUp > 0 || selectionNext > 0 || selectionPrevious > 0) {
                                selectionDown = 0
                                selectionUp = 0
                                selectionPrevious = 0
                                selectionNext = 0
                            }
                            if (isSelectionModeActive) {
                                isSelectionModeActive = false
                            }
                        }

                        else -> {
                            val inputConnection = currentInputConnection
                            if (inputConnection != null) {
                                if (isSymbolEnabled) {
                                    inputConnection.commitText(labelSecondary, 1)
                                } else {
                                    inputConnection.commitText(label, 1)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun longPressedStops(key: Key) {
        GlobalScope.launch {
            when (key.labelMain) {
                LABEL_DELETE -> {
                    // Stop character deletions when the key is released
                    isBackspaceKeyPressed = false
                    handler.removeCallbacks(deleteCharacterRunnable)
                    handler.removeCallbacks(deleteEmojisRunnable)
                }

                LABEL_UP -> {
                    isUpKeyPressed = false
                    handler.removeCallbacks(upRunnable)
                }

                LABEL_DOWN -> {
                    isDownKeyPressed = false
                    handler.removeCallbacks(downRunnable)
                }

                LABEL_NEXT -> {
                    isNextKeyPressed = false
                    handler.removeCallbacks(nextRunnable)
                }

                LABEL_PREVIOUS -> {
                    isPreviousKeyPressed = false
                    handler.removeCallbacks(previousRunnable)
                }
            }
        }
    }

    fun deleteEmoji() {
        GlobalScope.launch {
            val selectedText: CharSequence? = currentInputConnection.getSelectedText(0)
            if (selectedText != null && !TextUtils.isEmpty(selectedText)) {
                currentInputConnection.commitText("", 1)
            } else {
                currentInputConnection.deleteSurroundingText(2, 0)
            }
        }
    }

    fun deleteText() {
        GlobalScope.launch {
            val selectedText: CharSequence? = currentInputConnection.getSelectedText(0)
            if (selectedText != null && !TextUtils.isEmpty(selectedText)) {
                // Delete selected text
                currentInputConnection.commitText("", 1)
            } else {
                // Delete one character before cursor
                currentInputConnection.deleteSurroundingText(1, 0)
            }
        }
    }

    fun isTextFieldCompletelyEmpty(): Boolean {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest().apply {
            flags = ExtractedText.FLAG_SELECTING
        }, 0)

        return extractedText?.text?.isEmpty() ?: true
    }

    fun selectTextGesture(dragAmount: Int) {
        GlobalScope.launch {
            val currentText =
                currentInputConnection.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString()
            if (currentText != null && dragAmount < 0) {
                val textAfterCursor =
                    currentInputConnection.getTextAfterCursor(NUM_CHAR_BEFORE_CURSOR, 0)?.toString()
                val charsAfterCursor = textAfterCursor?.length ?: 0
                val startPosition = currentText.length
                val charsToSelect = (dragAmount / 8)
                val charsToSelectPositive = abs(charsToSelect)
                val selectionStart = startPosition - charsToSelectPositive
                val selectionEnd = startPosition - charsAfterCursor
                val effectiveSelectionStart = max(0, selectionStart)
                val effectiveSelectionEnd = min(selectionEnd, selectionEnd)

                if (effectiveSelectionStart < effectiveSelectionEnd) {
                    currentInputConnection.setSelection(
                        effectiveSelectionStart,
                        effectiveSelectionEnd
                    )
                }
            }
            if (isSelectionModeActive) {
                isSelectionModeActive = false
            }
        }
    }

    private val savedStateRegistryVar = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryVar.savedStateRegistry

    //Lifecycle Methods
    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private fun handleLifecycleEvent(event: Lifecycle.Event) =
        lifecycleRegistry.handleLifecycleEvent(event)

    //ViewModelStore Methods
    private val store = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = store


    private fun isFromGoogleServices(packageName: String): Boolean {
        return when (packageName) {
            "com.google.android.youtube",
            "com.android.chrome",
            "com.google.android.googlequicksearchbox",
            "com.google.android.gm",
            "com.google.android.gm.lite",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.mango",
            "com.google.android.apps.classroom",
            "com.google.android.apps.youtube.creator",
            "com.google.android.apps.translate",
            "com.google.android.apps.meetings",
            "com.google.android.apps.photos",
            "com.google.android.apps.tachyon",
            "com.google.android.contacts",
            "com.google.android.music",
            "com.google.android.apps.docs.editors.docs",
            "com.google.android.talk",
            "com.google.earth",
            "com.google.android.apps.searchlite",
            "com.google.android.apps.googleassistant",
            "com.google.android.apps.dynamite",
            "com.google.android.wearable.app",
            "com.google.android.apps.jam",
            "com.google.android.apps.docs.editors.slides",
            "com.google.android.apps.chromecast.app",
            "com.google.android.apps.pdfviewer",
            "com.google.android.apps.mapslite",
            "com.google.android.apps.messaging",
            "com.google.android.apps.forscience.whistlepunk",
            "com.google.android.apps.photos.scanner",
            "com.google.ar.lens",
            "com.google.android.ims",
            "com.google.android.calendar",
            "com.google.android.apps.adwords",
            "com.google.android.apps.kids.familylink",
            "com.google.android.apps.enterprise.dmagent",
            "com.google.android.street",
            "com.google.android.apps.vega",
            "com.google.chromeremotedesktop",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            "com.android.browser",
            "com.UCMobile.intl",
            "com.sec.android.app.sbrowser",
            "com.samsung.android.browser",
            "com.android.chrome.beta",
            "com.android.chrome.dev",
            "com.torchbrowser.torch",
            "org.mozilla.firefox_beta",
            "org.mozilla.firefox_nightly",
            "com.mozilla.klar",
            "com.mi.globalbrowser",
            "com.miui.browser",
            "com.oppo.browser",
            "com.vivo.browser",
            "com.motorola.android.browser",
            "com.huawei.browser",
            "com.google.android.apps.drive",
            "com.google.android.apps.keep",
            "com.apple.safari",
            "com.opera.mini.native",
            "com.microsoft.bing",
            "com.android.vending" -> false

            else -> true
        }
    }

    private fun fetchAdIDS(context: Context) {
        if (NetworkCheck.isNetworkAvailable(context)) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings =
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
            mFirebaseRemoteConfig?.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig?.fetchAndActivate()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig?.activate()
                    // Push the database writes to the background!
                    CoroutineScope(Dispatchers.IO).launch {
                        saveAllRemoteConfigs()
                    }
                } else {
                    if (context.resources.getString(R.string.ShowPopups) == "true") {
                        Log.e("NativeAdKeyboard", "RemoteConfig Failed")
                    }
                }
            }
        }
    }


    private fun saveAllRemoteConfigs() {
        getInAppPurchases = DataBaseCopyOperationsKt.getInAppPurchases()
        val adVisibility = mFirebaseRemoteConfig?.getBoolean(KEYPAD_AD_VISIBILITY)
        val adMediationNetwork = mFirebaseRemoteConfig?.getString(KEYPAD_AD_MEDIATION)?.trim()

        if (adVisibility == true) {
            DataBaseCopyOperationsKt.updateRemoteConfigVisibility(1)
        } else {
            DataBaseCopyOperationsKt.updateRemoteConfigVisibility(0)
        }
        when (adMediationNetwork) {
            "ADMOB", "admob", "Admob" -> {
                DataBaseCopyOperationsKt.updateRemoteConfigAdmob(1)
                DataBaseCopyOperationsKt.updateRemoteConfigMintegral(0)
            }

            "MINTEGRAL", "mintegral", "Mintegral" -> {
                DataBaseCopyOperationsKt.updateRemoteConfigAdmob(0)
                DataBaseCopyOperationsKt.updateRemoteConfigMintegral(1)
            }

            else -> {
                DataBaseCopyOperationsKt.updateRemoteConfigAdmob(0)
                DataBaseCopyOperationsKt.updateRemoteConfigMintegral(0)
            }
        }
        getRemoteConfigVisibility = DataBaseCopyOperationsKt.getRemoteConfigVisibility()
        getRemoteConfigAdmob = DataBaseCopyOperationsKt.getRemoteConfigAdmob()
        getRemoteConfigMintegral = DataBaseCopyOperationsKt.getRemoteConfigMintegral()
    }

}


@SuppressLint("ViewConstructor", "CoroutineCreationDuringComposition")
class ComposeKeyboardView(private val imeService: CustomImeService) :
    AbstractComposeView(imeService) {

    private var isNetworkAvailable by mutableStateOf(false)
    private var continuousLooper by mutableStateOf(true)
    var reloadAdRunnable: Runnable
    var handlerAd: Handler
    private val logTag = "NativeAdKeyboard"
    private val networkChangeReceiver = NetworkChangeReceiver()
    private var isReceiverRegistered by mutableStateOf(false)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Load network availability asynchronously
        checkNetworkAvailabilityAsync()

        handlerAd = Handler(Looper.getMainLooper())
        reloadAdRunnable = Runnable {
            Log.e(logTag, "Run: ")
            continuousLooper = !continuousLooper
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerNetworkReceiver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterNetworkReceiver()
        coroutineScope.cancel()
        handlerAd.removeCallbacksAndMessages(null)
    }

    private fun registerNetworkReceiver() {
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                context.registerReceiver(networkChangeReceiver, filter)
                isReceiverRegistered = true
                Log.e(logTag, "Network receiver registered")
            } catch (e: Exception) {
                Log.e(logTag, "Failed to register network receiver", e)
            }
        }
    }

    private fun unregisterNetworkReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(networkChangeReceiver)
                isReceiverRegistered = false
                Log.e(logTag, "Network receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e(logTag, "Receiver not registered", e)
            } catch (e: Exception) {
                Log.e(logTag, "Failed to unregister network receiver", e)
            }
        }
    }

    private fun checkNetworkAvailabilityAsync() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val available = NetworkCheck.isNetworkAvailable(context)
                withContext(Dispatchers.Main) {
                    isNetworkAvailable = available
                }
            } catch (e: Exception) {
                Log.e(logTag, "Network check failed", e)
                withContext(Dispatchers.Main) {
                    isNetworkAvailable = false
                }
            }
        }
    }

    @Composable
    override fun Content() {
        TriggerCode()
    }


    @Composable
    fun TriggerCode() {
        val context = LocalContext.current
        val showKeyboardBannerAd = isNetworkAvailable
        val isKeyboardOpen = imeService.isKeyboardOpen
        val isPurchase =
            imeService.getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
                .getBoolean(Preferences.IS_PURCHASED, false)

        // Keep these explicitly if you need them to force recomposition,
        // otherwise they might not be needed if only used in the if-check.
        val continuousLooperState = continuousLooper

        Column {
            if (showKeyboardBannerAd && isKeyboardOpen) {
                // moved logs outside the composition-critical path
                Log.e(
                    logTagAdmob,
                    "TriggerCode: Network: " + NetworkCheck.isNetworkAvailable(imeService.applicationContext)
                )

                val isNetworkReady = NetworkCheck.isNetworkAvailable(imeService.applicationContext)
                val isNoIAP = imeService.getInAppPurchases == 0
                val isRemoteConfigVisible = imeService.getRemoteConfigVisibility == 1
                // Warning: .get() on main thread can cause ANRs.
                val isForeground = try {
                    !ForegroundCheckTask().execute(context).get()
                } catch (e: Exception) {
                    false
                }

                if (isNetworkReady && isNoIAP && !isPurchase && isRemoteConfigVisible && isForeground) {
                    if (imeService.getRemoteConfigAdmob == 1 && minutesPassedAdmob()) {
                        // Use the new specialized container
                        AdmobSafeContainer(imeService)
                    } else if (imeService.getRemoteConfigMintegral == 1 && minutesPassedMintegral()) {
                        // Use the new specialized container
                        MintegralSafeContainer(imeService)
                    }
                }
            }

            MyKeyboard(
                imeService = imeService,
                vibrator = vibrator,
                context = imeService,
                inputConnection = imeService.currentInputConnection
            )
        }
    }


    @Composable
    private fun AdmobSafeContainer(imeService: CustomImeService) {
        val context = LocalContext.current
        val rootContainer = remember(context) { FrameLayout(context) }
        LaunchedEffect(Unit) {
            // Optional: Yield the thread briefly to let the keyboard fully pop up
            delay(100)

            // Now inflate the view. Because we pre-warmed MobileAds in onCreate,
            // this will no longer trigger heavy disk I/O.
            val adCardView = View.inflate(context, R.layout.card_view_ad_keypad_admob, null) as CardView
            removeFromParent(adCardView)

            rootContainer.addView(
                adCardView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            // Start checking cache and loading the ad
            nativeAdCacheCheckAdmob(imeService, adCardView)
        }

        DisposableEffect(Unit) {
            onDispose {
                // Your cleanup code remains the same
                handlerAd.removeCallbacks(reloadAdRunnable)
                Log.e("NativeAdKeyboard", "AdMob Container Disposed - Handler Cleared")
            }
        }

        AndroidView(factory = { rootContainer }, modifier = Modifier.fillMaxWidth())
    }

    @Composable
    private fun MintegralSafeContainer(imeService: CustomImeService) {
        val context = LocalContext.current
        val rootContainer = remember(context) { FrameLayout(context) }

        DisposableEffect(rootContainer) {
            val adCardView =
                View.inflate(context, R.layout.card_view_ad_keypad_mintegral, null) as CardView

            removeFromParent(adCardView)
            rootContainer.addView(
                adCardView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            val job = CoroutineScope(Dispatchers.Main).launch {
                bannerAdCacheCheckMintegral(imeService, adCardView)
            }

            onDispose {
                job.cancel()
                rootContainer.removeAllViews()
            }
        }

        AndroidView(
            factory = { rootContainer },
            modifier = Modifier.fillMaxWidth()
        )
    }

    private fun removeFromParent(view: View?) {
        try {
            val parent = view?.parent as? ViewGroup
            parent?.removeView(view)
        } catch (e: Exception) {
            Log.e("AdViewHelper", "Error removing view from parent", e)
        }
    }

    private fun nativeAdCacheCheckAdmob(mContext: CustomImeService?, adContainer: CardView?) {
        if (mContext == null || adContainer == null) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val defaultAdviewBanner: NativeAdView = adContainer.findViewById(R.id.nativead)
                val shimmerBanner: ShimmerFrameLayout = adContainer.findViewById(R.id.shimmerLayout)

                val shouldReload = withContext(Dispatchers.IO) {
                    if (nativeAdMobHashMapKeypad!!.containsKey(mContext::class.java.name)) {
                        val (nativeAd, timestamp) = nativeAdMobHashMapKeypad!![mContext::class.java.name]!!
                        System.currentTimeMillis() - timestamp >= 1500000
                    } else {
                        true
                    }
                }

                withContext(Dispatchers.Main) {
                    if (shouldReload) {
                        Log.e(logTagAdmob, "Reloading Admob ad")
                        reloadAdAdmob(mContext, shimmerBanner, defaultAdviewBanner)
                    } else {
                        val (nativeAd, _) = nativeAdMobHashMapKeypad!![mContext::class.java.name]!!
                        CustomFirebaseEvents.nativeKeypadAdEvent(
                            mContext,
                            "AdmobPreviousAdIsLoaded"
                        )
                        Log.e(logTagAdmob, "Previous ad is loaded")
                        populateNativeAdmob(nativeAd, shimmerBanner, defaultAdviewBanner)
                    }
                }
            } catch (e: Exception) {
                Log.e(logTagAdmob, "Ad cache check failed", e)
            }
        }
    }



    private fun reloadAdAdmob(
        mContext: CustomImeService?,
        shimmerBanner: ShimmerFrameLayout,
        defaultAdviewBanner: NativeAdView
    ) {
        if (mContext == null) return
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.e(logTagAdmob, "AdMob ad loading timeout")
            shimmerBanner.stopShimmer()
            shimmerBanner.visibility = View.GONE
            defaultAdviewBanner.visibility = View.GONE
        }
        timeoutHandler.postDelayed(timeoutRunnable, 15000) // 15 sec timeout
        // 1. Launch a Coroutine to handle the heavy setup in the background
        CoroutineScope(Dispatchers.Main).launch {

            // 2. Offload the SharedPreferences Disk Read to the IO Thread
            val adId = withContext(Dispatchers.IO) {
                val defBanner = "ca-app-pub-3747520410546258/9389862986"
                val pref = mContext.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                if (!BuildConfig.DEBUG) {
                    pref.getString(NATIVE_KEYPAD, defBanner) ?: defBanner
                } else {
                    mContext.resources.getString(R.string.admob_native_home)
                }
            }

            // 3. Offload building the AdRequest to the IO Thread (can sometimes be heavy)
            val adRequest = withContext(Dispatchers.IO) {
                AdRequest.Builder().build()
            }

            Log.e("nativeAd", "Requesting AdMob ad with ID: $adId")
            CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "AdmobRequestNewAd")

            // 4. Safely back on the Main Thread strictly for AdMob UI requirements
            try {
                val builder = AdLoader.Builder(mContext.applicationContext, adId)

                builder.forNativeAd { mNativeAd: NativeAd ->
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    handlerAd.removeCallbacks(reloadAdRunnable)

                    val currTime = System.currentTimeMillis()
                    nativeAdMobHashMapKeypad?.set(mContext::class.java.name, Pair(mNativeAd, currTime))

                    handlerAd.postDelayed(reloadAdRunnable, 1500000) // 25 min
                    populateNativeAdmob(mNativeAd, shimmerBanner, defaultAdviewBanner)

                    Log.e(logTagAdmob, "ReloadAd: ${nativeAdMobHashMapKeypad?.size}")
                }

                val adOptions = NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
                builder.withNativeAdOptions(adOptions)

                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: LoadAdError) {
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        Log.e(logTagAdmob, "Admob Native Failed to Load: $errorCode")
                        CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "AdmobOnAdFailedToLoad")

                        shimmerBanner.stopShimmer()
                        shimmerBanner.visibility = View.GONE
                        defaultAdviewBanner.visibility = View.GONE

                        nativeAdMobHashMapKeypad?.remove(mContext::class.java.name)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "AdmobOnAdClicked")
                        nativeAdMobHashMapKeypad?.remove(mContext::class.java.name)
                    }

                    override fun onAdLoaded() {
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        super.onAdLoaded()
                        CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "AdmobOnAdLoaded")
                        Log.e(logTagAdmob, "Admob Native Loaded..")

                        if (BuildConfig.DEBUG) {
                            Toast.makeText(mContext, "Keypad :: AdMob :: Loaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }).build()

                // 5. Load the ad using the background-built request
                adLoader.loadAd(adRequest)

                if (BuildConfig.DEBUG) {
                    Toast.makeText(mContext, "Keypad :: AdMob :: Request", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Failsafe just in case the Binder/IPC crashes completely
                Log.e(logTagAdmob, "Critical exception building AdLoader", e)
                timeoutHandler.removeCallbacks(timeoutRunnable)
            }
        }
    }


    private fun populateNativeAdmob(
        nativeAd: NativeAd,
        shimmerBanner: ShimmerFrameLayout,
        adView: NativeAdView
    ) {
        try {
            shimmerBanner.stopShimmer()
            shimmerBanner.visibility = View.INVISIBLE

            val headlineView = adView.findViewById<TextView>(R.id.adHeadline)
            val bodyView = adView.findViewById<TextView>(R.id.adBody)
            val callToActionView = adView.findViewById<Button>(R.id.adCallToAction)
            val iconView = adView.findViewById<ImageView>(R.id.adAppIcon)
            val iconCard = adView.findViewById<CardView>(R.id.adIconCard)
            val guideline = adView.findViewById<Guideline>(R.id.glNativeAdmobNormal1)

            adView.headlineView = headlineView
            adView.bodyView = bodyView
            adView.callToActionView = callToActionView
            adView.iconView = iconView

            headlineView?.text = nativeAd.headline

            if (nativeAd.body == null) {
                bodyView?.visibility = View.INVISIBLE
            } else {
                bodyView?.visibility = View.VISIBLE
                bodyView?.text = nativeAd.body
            }

            if (nativeAd.callToAction == null) {
                callToActionView?.visibility = View.INVISIBLE
            } else {
                callToActionView?.visibility = View.VISIBLE
                callToActionView?.text = nativeAd.callToAction
            }

            if (nativeAd.icon == null) {
                guideline?.setGuidelinePercent(0f)
                iconCard?.visibility = View.GONE
                iconView?.visibility = View.GONE
            } else {
                guideline?.setGuidelinePercent(0.15f)
                iconCard?.visibility = View.VISIBLE
                iconView?.setImageDrawable(nativeAd.icon?.drawable)
                iconView?.visibility = View.VISIBLE
            }

            adView.setNativeAd(nativeAd)
            adView.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(logTagAdmob, "Failed to populate native ad", e)
        }
    }

    private fun saveCurrentTimeAdmob() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                DataBaseCopyOperationsKt.updateKeyPadBannerFailedShowTimeAdmob("" + currentTime)
                Log.e(
                    logTagAdmob,
                    "CurrentTime Saved : toString(): " + DataBaseCopyOperationsKt.getKeyPadBannerFailedShowTimeAdmob()
                )
            } catch (e: Exception) {
                Log.e(logTagAdmob, "Failed to save current time", e)
            }
        }
    }

    private fun minutesPassedAdmob(): Boolean {
        return try {
            keyPadBannerFailedShowTimeAdmob =
                DataBaseCopyOperationsKt.getKeyPadBannerFailedShowTimeAdmob().toLong()
            if (keyPadBannerFailedShowTimeAdmob != 0L) {
                val currentTime = System.currentTimeMillis()
                val minutesInMillis = 45 * 60 * 1000 // 45 minutes in milliseconds
                (currentTime - keyPadBannerFailedShowTimeAdmob) >= minutesInMillis
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(logTagAdmob, "Failed to check minutes passed", e)
            true
        }
    }

    private fun bannerAdCacheCheckMintegral(mContext: CustomImeService?, adContainer: CardView?) {
        if (mContext == null || adContainer == null) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val defaultAdviewBanner: MBBannerView = adContainer.findViewById(R.id.mbBannerView)

                val shouldReload = withContext(Dispatchers.IO) {
                    if (nativeMintegralHashMapKeypad!!.containsKey(mContext::class.java.name)) {
                        val (nativeAd, timestamp) = nativeMintegralHashMapKeypad!![mContext::class.java.name]!!
                        System.currentTimeMillis() - timestamp >= 480000
                    } else {
                        true
                    }
                }

                withContext(Dispatchers.Main) {
                    if (shouldReload) {
                        reloadAdMintegral(mContext, adContainer, defaultAdviewBanner)
                    } else {
                        val (nativeAd, _) = nativeMintegralHashMapKeypad!![mContext::class.java.name]!!
                        CustomFirebaseEvents.nativeKeypadAdEvent(
                            mContext,
                            "MintegralPreviousAdIsLoaded"
                        )
                        Log.e(logTagMintegral, "Previous ad is loaded")
                        populateNativeMintegral(nativeAd, adContainer)
                    }
                }
            } catch (e: Exception) {
                Log.e(logTagMintegral, "Mintegral cache check failed", e)
            }
        }
    }

    private fun reloadAdMintegral(
        mContext: CustomImeService?,
        frameLayout: CardView,
        defaultAdviewBanner: MBBannerView
    ) {
        if (mContext == null) return

        // Set up timeout mechanism
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.e(logTagMintegral, "Mintegral ad loading timeout")
            frameLayout.visibility = View.GONE
        }

        timeoutHandler.postDelayed(timeoutRunnable, 15000) // 15 second timeout

        defaultAdviewBanner.init(
            BannerSize(BannerSize.DEV_SET_TYPE, 320, 50),
            mContext.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[0],
            mContext.getString(R.string.MINTEGRAL_BANNER_KEYPAD).split("-")[1]
        )

        defaultAdviewBanner.setBannerAdListener(object : BannerAdListener {
            override fun onLoadFailed(p0: MBridgeIds?, p1: String?) {
                timeoutHandler.removeCallbacks(timeoutRunnable)
                Log.e(logTagMintegral, "onLoadFailed() : " + p1)
                CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "MintegralOnAdFailedToLoad")

                frameLayout.visibility = View.GONE

                if (nativeMintegralHashMapKeypad!!.containsKey(mContext::class.java.name)) {
                    nativeMintegralHashMapKeypad!!.remove(mContext::class.java.name)
                }

                saveCurrentTimeMintegral()
            }

            override fun onLoadSuccessed(p0: MBridgeIds?) {

                handlerAd.removeCallbacks(reloadAdRunnable)
                val currTime = System.currentTimeMillis()

                // Safe detach and re-attach
                if (defaultAdviewBanner != null) {
                    // Use the helper to safely remove it from whatever old container it might be in
                    removeFromParent(defaultAdviewBanner)

                    nativeMintegralHashMapKeypad!![mContext::class.java.name] =
                        Pair(defaultAdviewBanner, currTime)

                    frameLayout.removeAllViews()
                    frameLayout.addView(defaultAdviewBanner)

                    Log.e(logTagMintegral, "ReloadAd: ${nativeMintegralHashMapKeypad!!.size}")
                }
                handlerAd.postDelayed(reloadAdRunnable, 480000)
            }

            override fun onLogImpression(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "onLogImpression()")
                CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "MintegralOnLogImpression")
            }

            override fun onClick(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "onClick()")
                CustomFirebaseEvents.nativeKeypadAdEvent(mContext, "MintegralOnAdClicked")
                if (nativeMintegralHashMapKeypad!!.containsKey(mContext::class.java.name)) {
                    nativeMintegralHashMapKeypad!!.remove(mContext::class.java.name)
                }
            }

            override fun onLeaveApp(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "onLeaveApp()")
            }

            override fun showFullScreen(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "showFullScreen()")
            }

            override fun closeFullScreen(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "closeFullScreen()")
            }

            override fun onCloseBanner(p0: MBridgeIds?) {
                Log.e(logTagMintegral, "onCloseBanner()")
                if (nativeMintegralHashMapKeypad!!.containsKey(mContext::class.java.name)) {
                    nativeMintegralHashMapKeypad!!.remove(mContext::class.java.name)
                }
            }
        })

        defaultAdviewBanner.load()

        if (BuildConfig.DEBUG) {
            Toast.makeText(
                mContext,
                "Keypad :: Mintegral :: Request",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun populateNativeMintegral(nativeAd: MBBannerView?, defaultAdviewBanner: CardView) {
        try {
            nativeAd?.let { cachedBanner ->
                // Use helper for safe detachment
                removeFromParent(cachedBanner)

                // Ensure destination is clear
                defaultAdviewBanner.removeAllViews()
                defaultAdviewBanner.addView(cachedBanner)
                Log.i(logTagMintegral, "Mintegral: BannerAd : Keypad : Showing cached banner")
            } ?: Log.i(logTagMintegral, "Mintegral: BannerAd : Keypad : No cached banner to show")
        } catch (e: Exception) {
            Log.e(logTagMintegral, "Failed to populate Mintegral ad", e)
        }
    }

    private fun saveCurrentTimeMintegral() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                DataBaseCopyOperationsKt.updateKeyPadBannerFailedShowTimeMintegral("" + currentTime)
                Log.e(
                    logTagMintegral,
                    "CurrentTime Saved : toString(): " + DataBaseCopyOperationsKt.getKeyPadBannerFailedShowTimeMintegral()
                )
            } catch (e: Exception) {
                Log.e(logTagMintegral, "Failed to save current time", e)
            }
        }
    }

    private fun minutesPassedMintegral(): Boolean {
        return try {
            keyPadBannerFailedShowTimeMintegral =
                DataBaseCopyOperationsKt.getKeyPadBannerFailedShowTimeMintegral().toLong()
            if (keyPadBannerFailedShowTimeMintegral != 0L) {
                val currentTime = System.currentTimeMillis()
                val minutesInMillis = 5 * 60 * 1000 // 5 minutes in milliseconds
                (currentTime - keyPadBannerFailedShowTimeMintegral) >= minutesInMillis
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(logTagMintegral, "Failed to check minutes passed", e)
            true
        }
    }

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(context, VibratorManager::class.java)
            vibratorManager?.defaultVibrator ?: getSystemService(context, Vibrator::class.java)
        } else {
            getSystemService(context, Vibrator::class.java)
        } as Vibrator
    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkNetworkAvailabilityAsync()
        }
    }
}