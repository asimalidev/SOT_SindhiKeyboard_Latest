package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Vibrator
import android.util.Log
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.arabicLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.bangla_layout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.extendedSymbolsLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.husneguftar
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.nepali_layout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.numbersLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.pashto_layout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.qwertyLayout_english
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.sindhi_layout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.symbolsLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.symbolsSindhiLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.symbolsUrduLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.textEditLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.urdu_layout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.CUSTOM_ACTION
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DELETE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.KeyboardVisibilityProvider
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.LayoutState
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.CustomKey
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.AppTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.KeyboardTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.getIconResource
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.getSelectedLanguage
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.DictionaryObject.suggestionList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.KeyboardState
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key

@Composable
fun MyKeyboard(imeService: CustomImeService? = null, vibrator: Vibrator? = null, context: Context?, inputConnection: InputConnection? = null) {

    val coroutineScope = rememberCoroutineScope()

    val myPreferences = remember(context) { context?.let { MyPreferences(it) } }

    var defaultLanguage by remember {
        mutableStateOf(myPreferences?.getKeyboard() ?: "English")
    }

    LaunchedEffect(defaultLanguage) {
        myPreferences?.setKeyboard(defaultLanguage)
    }

    // MEMORY LEAK FIX: Remember the ViewModel so it isn't recreated on every keystroke
    val stickerViewModel = remember { StickerViewModel() }

    // MEMORY LEAK FIX: Remember the sticker list and safely use context instead of imeService!!
    val downloadedStickersPackList = remember {
        context?.applicationContext?.let {
            stickerViewModel.fetchDownloadStickers(it)
        } ?: mutableListOf()
    }

    var themeMode by remember { mutableStateOf(myPreferences?.getTheme() ?: "AUTO") }
    var showNumbersRow by remember { mutableStateOf(myPreferences?.getShowNumbersRow() ?: false) }

    val currentLanguage = when (defaultLanguage) {
        "English" -> qwertyLayout_english
        "Urdu" -> urdu_layout
        "Arabic" -> arabicLayout
        "Nepali" -> nepali_layout
        "Pashto" -> pashto_layout
        "Bangla" -> bangla_layout
        "Sindhi" -> sindhi_layout
        else -> qwertyLayout_english
    }

    var isSuggestionsEnabled by remember { mutableStateOf(false) }

    val view = LocalView.current

    val isGestureNavigationEnabled = remember {
        val resources = view.context.resources
        val resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        if (resourceId > 0) {
            resources.getInteger(resourceId) == 2
        } else {
            false
        }
    }

    var isVoiceInputClicked by remember { mutableStateOf(false) }
    var isEmojiButtonClicked by remember { mutableStateOf(true) }
    var isStickerButtonClicked by remember { mutableStateOf(false) }

    var isCapsEnabled by remember { mutableStateOf(true) }
    var isCapsLockEnabled by remember { mutableStateOf(false) }

    var icon by remember(imeService?.imeActionType) {
        mutableIntStateOf(getIconResource(imeService?.imeActionType ?: 0))
    }

    LaunchedEffect(imeService?.imeActionType) {
        icon = getIconResource(imeService?.imeActionType ?: 0)
    }

    var currentLayout by KeyboardState.currentLayout
    var showBackIcon by remember { mutableStateOf(false) }

    val languagePreferences = listOf(
        "English" to MyPreferences::getEnglish,
        "Urdu" to MyPreferences::getUrdu,
        "Arabic" to MyPreferences::getArabic,
        "Sindhi" to MyPreferences::getSindhi,
        "Pashto" to MyPreferences::getPashto,
        "Bangla" to MyPreferences::getBangla,
        "Nepali" to MyPreferences::getNepali
    )

    val enabledLanguages = languagePreferences.filter { (_, preferenceGetter) ->
        myPreferences?.let { preferenceGetter.invoke(it) } == true
    }.map { (language, _) -> language }.toList()

    var currentLanguageIndex by remember {
        mutableIntStateOf(enabledLanguages.indexOf(defaultLanguage).coerceAtLeast(0))
    }

    LaunchedEffect(defaultLanguage) {
        currentLanguageIndex = enabledLanguages.indexOf(defaultLanguage).coerceAtLeast(0)
    }

    val keyboardHeight = if (showNumbersRow) 298.dp else 268.dp

    // MEMORY LEAK FIX: Cache the layout array so it only recalculates when the layout or language ACTUALLY changes
    val keys = remember(currentLayout, defaultLanguage, showNumbersRow) {
        val layoutKeys = when (currentLayout) {
            LayoutState.Main -> currentLanguage
            LayoutState.Symbols -> symbolsLayout
            LayoutState.SymbolsUrdu -> symbolsUrduLayout
            LayoutState.SymbolsSindhi -> symbolsSindhiLayout
            LayoutState.ExtendedSymbols -> extendedSymbolsLayout
            LayoutState.Numbers -> numbersLayout
            else -> null
        }

        val shouldDropNumberRow = myPreferences?.getShowNumbersRow() == false &&
                currentLayout == LayoutState.Main &&
                (defaultLanguage == "English" || defaultLanguage == "Arabic")

        layoutKeys?.drop(if (shouldDropNumberRow) 1 else 0)?.toTypedArray()
    }

    // Add suspend and Dispatchers.IO
    suspend fun isTextFieldEmpty(inputConnection: InputConnection?): Boolean {
        if (inputConnection == null) return true

        return withContext(Dispatchers.IO) { // <-- Forces IPC off the main thread
            try {
                val beforeCursor = inputConnection.getTextBeforeCursor(1, 0)
                val afterCursor = inputConnection.getTextAfterCursor(1, 0)

                beforeCursor.isNullOrEmpty() && afterCursor.isNullOrEmpty()
            } catch (t: Throwable) {
                Log.e("KeyboardLayout", "Stale InputConnection: Failed to read text", t)
                false
            }
        }
    }

    // Also update your variant if you are using it elsewhere
    suspend fun isTextFieldEmptyVariant(inputConnection: InputConnection?): Boolean {
        return isTextFieldEmpty(inputConnection)
    }

    fun resetKeyboard() {
        currentLayout = LayoutState.Main
        isCapsLockEnabled = false

        // Launch in the Compose coroutine scope
        coroutineScope.launch {
            if (isTextFieldEmpty(inputConnection)) {
                isCapsEnabled = true
            }
        }
    }

    var isKeyboardVisible by remember { mutableStateOf(false) }
    KeyboardVisibilityProvider { isVisible ->
        isKeyboardVisible = isVisible
        if (!isKeyboardVisible) {
            showNumbersRow = context?.getSharedPreferences("SR_keyboard", Context.MODE_PRIVATE)
                ?.getBoolean(Preferences.showNumbersRow, false) == true
            themeMode = context?.getSharedPreferences("SR_keyboard", Context.MODE_PRIVATE)
                ?.getString(Preferences.themeKey, "AUTO") ?: "AUTO"
        }
    }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                try {
                    resetKeyboard()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter(CUSTOM_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context?.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context?.registerReceiver(receiver, filter, null, null)
        }

        onDispose {
            context?.unregisterReceiver(receiver)
        }
    }

    val specialKeys = if (currentLayout == LayoutState.TextEdit) textEditLayout else null
    val specialKeysHusneGuftar = if (currentLayout == LayoutState.HusnEguftar) husneguftar else null

    val onLayoutSwitchClick = { currentLayout = LayoutState.Main }
    val onNumbersSwitchClick = { currentLayout = LayoutState.Numbers }
    val onSymbolsLayoutSwitchClick = { currentLayout = LayoutState.Symbols }
    val onSymbolsUrduLayoutSwitchClick = { currentLayout = LayoutState.SymbolsUrdu }
    val onSymbolsSindhiLayoutSwitchClick = { currentLayout = LayoutState.SymbolsSindhi }
    val onExtendedSymbolsSwitchClick = { currentLayout = LayoutState.ExtendedSymbols }

    val onLanguageSwitchClick: (String) -> Unit = { languageChangedFromTop ->
        if (languageChangedFromTop.isNotEmpty()) {
            defaultLanguage = languageChangedFromTop
            Log.e("Language", "Language changed from top: $languageChangedFromTop")
        } else if (enabledLanguages.isNotEmpty()) {
            currentLanguageIndex = (currentLanguageIndex + 1) % enabledLanguages.size
            defaultLanguage = enabledLanguages[currentLanguageIndex]
            Log.e("Language", "Cycling languages - Current: $defaultLanguage, Index: $currentLanguageIndex")
        }
        resetKeyboard()
    }

    val onEditTextSwitchClick = { currentLayout = LayoutState.TextEdit; showBackIcon = true }
    val onHusnEguftarClick = { currentLayout = LayoutState.HusnEguftar; showBackIcon = true }
    val onEmojiSwitchClick = { currentLayout = LayoutState.Emojis; showBackIcon = true }

    val onBackClick = {
        currentLayout = LayoutState.Main
        showBackIcon = false
        isSuggestionsEnabled = false
        imeService?.filterList?.value = emptyList()
    }

    val onCapsClick = {
        if (isCapsLockEnabled) isCapsLockEnabled = false else isCapsEnabled = !isCapsEnabled
    }

    val onCapsClickToLock = {
        isCapsLockEnabled = !isCapsLockEnabled
        isCapsEnabled = false
    }

    val layoutLabel =
        if (currentLayout == LayoutState.TextEdit) stringResource(id = R.string.edit_layout)
        else if (currentLayout == LayoutState.HusnEguftar) stringResource(id = R.string.HusnEguftar_layout)
        else stringResource(id = R.string.emoji_layout)

    KeyboardTheme(
        theme = when (themeMode) {
            "AUTO" -> AppTheme.AUTO
            "LIGHT" -> AppTheme.LIGHT
            "Dark" -> AppTheme.DARK
            "SolidSimple" -> AppTheme.SolidSimple
            "Gradient1" -> AppTheme.Gradient1
            "Gradient2" -> AppTheme.Gradient2
            "Gradient3" -> AppTheme.Gradient3
            "Gradient4" -> AppTheme.Gradient4
            "Gradient5" -> AppTheme.Gradient5
            "Gradient6" -> AppTheme.Gradient6
            "Gradient7" -> AppTheme.Gradient7
            "Gradient8" -> AppTheme.Gradient8
            "Gradient9" -> AppTheme.Gradient9
            "Gradient10" -> AppTheme.Gradient10
            "Gradient11" -> AppTheme.Gradient11
            "Gradient12" -> AppTheme.Gradient12
            "SOLID1_LIGHT" -> AppTheme.SOLID_LIGHT_1
            "SOLID2_LIGHT" -> AppTheme.SOLID_LIGHT_2
            "SOLID3_LIGHT" -> AppTheme.SOLID_LIGHT_3
            "SOLID4_LIGHT" -> AppTheme.SOLID_LIGHT_4
            "SOLID5_LIGHT" -> AppTheme.SOLID_LIGHT_5
            "SOLID6_LIGHT" -> AppTheme.SOLID_LIGHT_6
            "SOLID7_LIGHT" -> AppTheme.SOLID_LIGHT_7
            "SOLID8_LIGHT" -> AppTheme.SOLID_LIGHT_8
            "SOLID9_LIGHT" -> AppTheme.SOLID_LIGHT_9
            "SOLID10_LIGHT" -> AppTheme.SOLID_LIGHT_10
            "SOLID11_LIGHT" -> AppTheme.SOLID_LIGHT_11
            "SOLID12_LIGHT" -> AppTheme.SOLID_LIGHT_12
            "SOLID1_DARK" -> AppTheme.SOLID_DARK_1
            "SOLID2_DARK" -> AppTheme.SOLID_DARK_2
            "SOLID3_DARK" -> AppTheme.SOLID_DARK_3
            "SOLID4_DARK" -> AppTheme.SOLID_DARK_4
            "SOLID5_DARK" -> AppTheme.SOLID_DARK_5
            "SOLID6_DARK" -> AppTheme.SOLID_DARK_6
            "SOLID7_DARK" -> AppTheme.SOLID_DARK_7
            "SOLID8_DARK" -> AppTheme.SOLID_DARK_8
            "SOLID9_DARK" -> AppTheme.SOLID_DARK_9
            "SOLID10_DARK" -> AppTheme.SOLID_DARK_10
            "SOLID11_DARK" -> AppTheme.SOLID_DARK_11
            "SOLID12_DARK" -> AppTheme.SOLID_DARK_12
            else -> AppTheme.AUTO
        }
    ) {

        Log.e("DataBaseCopyOperationsMyKeyboardLayout", "TheSize: 1: " + suggestionList.size)

        // SAFE NULL CHECK FIX
        if (imeService?.mainSuggestionList?.isNotEmpty() == true && suggestionList.isNotEmpty()) {
            imeService.mainSuggestionList = suggestionList
        }

        isSuggestionsEnabled = imeService?.filterList?.value?.isNotEmpty() == true

        val background = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.inverseSurface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(bottom = if (isGestureNavigationEnabled) 14.dp else 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyboardHeight)
                    .background(background)
            ) {
                MyCandidateView(
                    context = context,
                    imeService = imeService,
                    onEditTextSwitchClick = onEditTextSwitchClick,
                    onHusnEguftarSwitchClick = onHusnEguftarClick,
                    onEmojiSwitchClick = onEmojiSwitchClick,
                    onEmojiButtonClick = {
                        isEmojiButtonClicked = true
                        isStickerButtonClicked = false
                    },
                    onStickerButtonClick = {
                        isStickerButtonClicked = true
                        isEmojiButtonClicked = false
                    },
                    onBackClick = { onBackClick() },
                    onDragGestureToSelect = { dragAmount ->
                        imeService?.selectTextGesture(dragAmount)
                    },
                    deleteTextAfterDrag = {
                        imeService?.deleteText()

                        // Launch coroutine to check text field state
                        coroutineScope.launch {
                            if (isTextFieldEmpty(inputConnection) && !isCapsLockEnabled) {
                                isCapsEnabled = true
                            }
                        }
                    },
                    onLongKeyPressed = { isEmoji ->
                        imeService?.doSomethingWith(
                            key = Key(LABEL_DELETE, 1f),
                            isLongPressed = true,
                            isEmoji = isEmoji
                        )
                        if (!isCapsLockEnabled && isCapsEnabled) {
                            isCapsEnabled = false
                        }
                    },
                    onLongKeyPressedEnd = {
                        imeService?.longPressedStops(key = Key(LABEL_DELETE, 1f))
                    },
                    onSuggestedWordSelected = {
                        onBackClick()
                    },
                    onDelete = { imeService?.deleteEmoji() },
                    onChangeLanguageBtnClick = { languageChangedFromTop ->
                        onLanguageSwitchClick(languageChangedFromTop)
                    },
                    currentLayout = currentLayout,
                    myPreferences = myPreferences,
                    layoutLabel = layoutLabel,
                    onVoiceInputClicked = { isClicked ->
                        isVoiceInputClicked = isClicked
                    },
                    isShowingSuggestions = isSuggestionsEnabled,
                    isVoiceInputClicked = isVoiceInputClicked,
                    currentLanguage = defaultLanguage
                )
                when (currentLayout) {
                    LayoutState.TextEdit -> {
                        EditTextLayout(keyboardHeight, specialKeys, imeService, context, vibrator)
                    }

                    LayoutState.HusnEguftar -> {
                        HusnEguftarLayout(
                            keyboardHeight, specialKeysHusneGuftar, imeService, context, vibrator, background,
                            onGuftarClick = {
                                imeService?.commitHusnEguftar(" $it")
                            }
                        )
                    }

                    LayoutState.Emojis -> {
                        EmojiLayout(
                            context,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            isEmojiButtonClicked,
                            isStickerButtonClicked,
                            downloadedStickersPackList,
                            onEmojiClick = {
                                imeService?.commitEmoji(it)
                            },
                            sendStickerTest = { file ->
                                imeService?.sendSticker(context, file)
                            }
                        )
                    }

                    else -> {
                        key(keys) {
                            keys?.forEachIndexed { rowIndex, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = if (defaultLanguage == "English") {
                                                if (showNumbersRow) {
                                                    if (rowIndex == 2 && currentLayout == LayoutState.Main) 18.dp else dimensionResource(id = R.dimen.key_marginH).value.dp
                                                } else {
                                                    if (rowIndex == 1 && currentLayout == LayoutState.Main) 18.dp else dimensionResource(id = R.dimen.key_marginH).value.dp
                                                }
                                            } else {
                                                dimensionResource(id = R.dimen.key_marginH).value.dp
                                            },
                                            vertical = if (showNumbersRow && ((currentLayout == LayoutState.Symbols) || (currentLayout == LayoutState.ExtendedSymbols))) 7.dp else dimensionResource(id = R.dimen.key_marginV).value.dp
                                        )
                                        .weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.forEach { key ->
                                        CustomKey(
                                            key = key,
                                            isCapsEnabled = isCapsEnabled,
                                            isCapsLockEnabled = isCapsLockEnabled,
                                            onKeyPressed = {
                                                // COROUTINE LEAK FIX: Using local coroutine scope
                                                coroutineScope.launch {
                                                    val getCompleteTextForSuggestion = withContext(Dispatchers.IO) {
                                                        imeService?.getCompleteTextForSuggestionAsync() ?: ""
                                                    }

                                                    if (key.isCharacter) {
                                                        val endsWithSpace = false
                                                        val fullText = getCompleteTextForSuggestion + key.labelMain

                                                        imeService?.currentWordMain = returnTypedWordForSuggestion(fullText, "character")

                                                        if (imeService?.currentWordMain?.isNotEmpty() == true) {
                                                            try {
                                                                val suggestions = withContext(Dispatchers.IO) {
                                                                    searchForSuggestion(imeService, context, imeService.currentWordMain)
                                                                }
                                                                imeService.filterList.value = suggestions
                                                                isSuggestionsEnabled = suggestions.isNotEmpty()
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                        } else {
                                                            isSuggestionsEnabled = imeService?.filterList?.value?.isNotEmpty() == true
                                                        }

                                                        imeService?.commitText(
                                                            text = key,
                                                            isCapsEnabled = isCapsEnabled,
                                                            isCapsLockEnabled = isCapsLockEnabled,
                                                            endsWithSpace = endsWithSpace
                                                        )

                                                        if (fullText.endsWith(".")) {
                                                            isCapsEnabled = true
                                                        } else if (!isCapsLockEnabled && isCapsEnabled) {
                                                            isCapsEnabled = false
                                                        }

                                                    } else {
                                                        var isEmoji = false
                                                        if (key.labelMain == LABEL_DELETE) {
                                                            val fullText = getCompleteTextForSuggestion
                                                            imeService?.currentWordMain = returnTypedWordForSuggestion(fullText, "delete")

                                                            isEmoji = if (fullText.isNotEmpty() && fullText.last() == ' ') {
                                                                false
                                                            } else {
                                                                checkEmoji(ctx = context!!, fullText)
                                                            }

                                                            if (imeService?.currentWordMain?.isNotEmpty() == true) {
                                                                try {
                                                                    imeService.filterList.value = withContext(Dispatchers.IO) {
                                                                        searchForSuggestion(imeService, context, imeService.currentWordMain)
                                                                    }
                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                }
                                                            }

                                                            val willBeEmpty = fullText.length <= 1
                                                            if (willBeEmpty && !isCapsLockEnabled) {
                                                                isCapsEnabled = true
                                                            }
                                                        }

                                                        if (getCompleteTextForSuggestion.length == 1) {
                                                            isSuggestionsEnabled = false
                                                            imeService?.filterList?.value = emptyList()
                                                        }

                                                        imeService?.doSomethingWith(key, false, isEmoji = isEmoji)

                                                        if (key.labelMain == LABEL_DELETE && isTextFieldEmpty(imeService?.currentInputConnection) && !isCapsLockEnabled) {
                                                            isCapsEnabled = true
                                                        }
                                                    }
                                                }
                                            },
                                            onDragGestureToSelect = { dragAmount ->
                                                if (key.labelMain == LABEL_DELETE && myPreferences?.getSwipeToDelete() == true) {
                                                    imeService?.selectTextGesture(dragAmount)
                                                }
                                            },
                                            deleteTextAfterDrag = {
                                                if (key.labelMain == LABEL_DELETE) {
                                                    imeService?.filterList?.value = emptyList()
                                                    imeService?.deleteText()

                                                    // COROUTINE FIX: Using local coroutine scope
                                                    coroutineScope.launch {
                                                        delay(100)
                                                        if (imeService?.isTextFieldCompletelyEmpty() == true && !isCapsLockEnabled) {
                                                            isCapsEnabled = true
                                                        }
                                                    }
                                                }
                                            },
                                            onLongKeyPressed = {
                                                imeService?.filterList?.value = emptyList()
                                                imeService?.doSomethingWith(
                                                    key, true,
                                                    isSymbolEnabled = myPreferences?.getLongPressForSymbols() ?: false
                                                )
                                                if (!isCapsLockEnabled && isCapsEnabled) {
                                                    isCapsEnabled = false
                                                }
                                            },
                                            onLongKeyPressedEnd = {
                                                imeService?.longPressedStops(key = key)

                                                // COROUTINE FIX: Using local coroutine scope
                                                coroutineScope.launch {
                                                    delay(100)
                                                    if (key.labelMain == LABEL_DELETE &&
                                                        imeService?.isTextFieldCompletelyEmpty() == true &&
                                                        !isCapsLockEnabled) {
                                                        isCapsEnabled = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.padding(horizontal = 2.dp).weight(key.weight),
                                            imeService = imeService,
                                            onLayoutSwitchClick = { onLayoutSwitchClick() },
                                            onExtendedSymbolsSwitchClick = { onExtendedSymbolsSwitchClick() },
                                            onLanguageSwitchClick = { onLanguageSwitchClick("") },
                                            onNumbersSwitchClick = { onNumbersSwitchClick() },
                                            onSymbolsLayoutSwitchClick = { onSymbolsLayoutSwitchClick() },
                                            onSymbolsUrduLayoutSwitchClick = { onSymbolsUrduLayoutSwitchClick() },
                                            onSymbolsSindhiLayoutSwitchClick = { onSymbolsSindhiLayoutSwitchClick() },
                                            onCapsClick = { onCapsClick() },
                                            onCapsClickToLock = { onCapsClickToLock() },
                                            context = context,
                                            vibrator = vibrator,
                                            iconResourceId = icon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Ensure these functions remain unchanged at the bottom of your file:
fun checkEmoji(ctx: Context, character: String): Boolean {
    val emojiLists = ctx.getSharedPreferences("androidx.emoji2.emojipicker.preferences", Context.MODE_PRIVATE)?.
    getString("pref_key_custom_emoji_freq", null)?.
    split(",")?.
    associate { entry -> entry.split("=", limit = 2).
    takeIf { it.size == 2 }?.
    let { it[0] to it[1].toInt() } ?: ("" to 0) }?.
    toMutableMap() ?: mutableMapOf()
    var returnResult = false
    emojiLists.keys.forEach loop@{ element ->
        if (character.endsWith(element)) {
            returnResult = true
            return@loop
        }
    }
    return returnResult
}

fun returnTypedWordForSuggestion(fullTextMain: String, from: String): String {
    val originalWord = fullTextMain
    val lastWhitespaceIndex = originalWord.lastIndexOf(' ')
    val modifiedText = if (lastWhitespaceIndex != -1) {
        originalWord.substring(lastWhitespaceIndex + 1)
    } else {
        originalWord
    }

    if (from == "delete") {
        if (modifiedText.isNotEmpty()) {
            return modifiedText.substring(0, modifiedText.length - 1)
        } else if (lastWhitespaceIndex != -1 && originalWord.isNotEmpty()) {
            val previousWord = originalWord.substring(0, lastWhitespaceIndex)
            if (previousWord.contains(' ')) {
                val words = previousWord.split(' ')
                return words.last()
            }
            return previousWord
        }
    }
    return modifiedText
}

suspend fun searchForSuggestion(imeService: CustomImeService?, context: Context?, searchWord: String): List<SuggestionItems> {
    if (imeService == null || context == null || searchWord.isBlank()) {
        return emptyList()
    }

    DataBaseCopyOperationsKt.init(context)

    val lang = getSelectedLanguage(context)
    return DataBaseCopyOperationsKt.getSuggestionsForWord(searchWord, lang)
}


