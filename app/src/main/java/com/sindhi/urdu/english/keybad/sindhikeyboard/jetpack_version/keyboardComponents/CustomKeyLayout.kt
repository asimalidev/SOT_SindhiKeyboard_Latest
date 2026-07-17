package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents

import android.content.Context
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.DOUBLE_TAP_THRESHOLD_MILLIS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_123
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_1by2
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_1by2Sindhi
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_2by2
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_2by2Sindhi
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_ABC
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_CAPS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DELETE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DONE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_EXTENDED_SYMBOLS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_LANGUAGE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_NUMBERS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SELECT
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_SYMBOLS
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.SelectButtonColor
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.onSelectButtonColor
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.onClickSound
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.onClickVibrate
import kotlinx.coroutines.delay


@Composable
fun CustomKey(
    key: Key,
    isCapsEnabled: Boolean = false,
    isCapsLockEnabled: Boolean = false,
    onKeyPressed: () -> Unit,
    onDragGestureToSelect: (Int) -> Unit,
    deleteTextAfterDrag: () -> Unit,
    onLongKeyPressed: () -> Unit,
    onLongKeyPressedEnd: () -> Unit,
    modifier: Modifier,
    isSelectionEnabled: Boolean = false,
    imeService: CustomImeService? = null,
    onLayoutSwitchClick: () -> Unit,
    onExtendedSymbolsSwitchClick: () -> Unit,
    onNumbersSwitchClick: () -> Unit,
    onSymbolsLayoutSwitchClick: () -> Unit,
    onSymbolsUrduLayoutSwitchClick: () -> Unit,
    onSymbolsSindhiLayoutSwitchClick: () -> Unit,
    onLanguageSwitchClick: (String) -> Unit,
    onCapsClick: () -> Unit,
    onCapsClickToLock: () -> Unit,
    context: Context?,
    vibrator: Vibrator? = null,
    iconResourceId: Int = R.drawable.done_icon,
    specialTint: Color = MaterialTheme.colorScheme.errorContainer,
    myTextColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    val myPreferences = context?.let { MyPreferences(it) }

    // State to track the visibility of the pop-up
    val isPopupVisible = remember { mutableStateOf(false) }

    // Safely unwrap myPreferences with a fallback value to prevent null pointer crashes
    val keyTextSize by remember { mutableIntStateOf(myPreferences?.getTextSize() ?: 18) }

    // Determine the background color of the key based on whether it's a special character
    val keyColor = if (key.isSpecialCharacter) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else if ((isSelectionEnabled || imeService?.isSelectionModeActive == true) && key.labelMain == LABEL_SELECT) {
        SelectButtonColor
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    // Determine the text color of the key based on whether it's a selection button
    val textColor by remember {
        mutableStateOf(
            if (isSelectionEnabled && key.labelMain == LABEL_SELECT)
                onSelectButtonColor
            else myTextColor
        )
    }

    // Define the shape of the key based on its label
    val keyShape by remember {
        mutableStateOf(
            if (key.shouldBeRounded) {
                RoundedCornerShape(100)
            } else {
                RoundedCornerShape(6.dp)
            }
        )
    }

    // Calculate the main label of the key, considering Caps and Caps Lock state
    val labelMain = remember(key.labelMain, isCapsEnabled, isCapsLockEnabled) {
        if (isCapsEnabled || isCapsLockEnabled) {
            key.labelMain.uppercase()
        } else {
            key.labelMain
        }
    }

    // Determine the icon to display on the key
    val icon = when (key.labelMain) {
        LABEL_CAPS -> {
            if (isCapsEnabled) {
                R.drawable.caps_lock_on
            } else if (isCapsLockEnabled) {
                R.drawable.caps_lock_on_locked
            } else {
                R.drawable.caps_lock_off
            }
        }
        LABEL_DONE -> iconResourceId
        else -> key.icon
    }

    val keyboardName = when (myPreferences?.getKeyboard()) {
        "Eng" -> "Eng"
        "Urdu" -> "اردو"
        "Arabic" -> "العربية"
        "Pashto" -> "پښتو"
        "Nepali" -> "Nepali"
        "Sindhi" -> "سنڌي"
        "Bangla" -> "Bangla"
        else -> "Eng"
    }

    // State to track long press
    var isLongPressed by remember { mutableStateOf(false) }

    // Determine the label for the pop-up
    val popUpLabel = if (isLongPressed) key.labelSecondary ?: key.labelMain else key.labelMain

    // Interaction source for detecting gestures
    val interactionSource = remember { MutableInteractionSource() }
    var lastCapsLockTapTime by remember { mutableLongStateOf(0L) }

    // Using Box instead of ConstraintLayout for massive performance gains in a keyboard
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(keyShape)
            .background(color = keyColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClickSound(context = context, myPreferences = myPreferences)
                        onClickVibrate(context = context, isLongPressed = true, myPreferences = myPreferences)
                        when (key.labelMain) {
                            LABEL_CAPS -> {
                                val currentTime = System.currentTimeMillis()
                                val timeDiff = currentTime - lastCapsLockTapTime
                                if (timeDiff < DOUBLE_TAP_THRESHOLD_MILLIS) {
                                    onCapsClickToLock()
                                } else {
                                    onCapsClick()
                                }
                                lastCapsLockTapTime = currentTime
                            }
                            LABEL_ABC -> onLayoutSwitchClick()
                            LABEL_123 -> onSymbolsLayoutSwitchClick()
                            LABEL_LANGUAGE -> onLanguageSwitchClick("")
                            LABEL_EXTENDED_SYMBOLS -> onExtendedSymbolsSwitchClick()
                            LABEL_SYMBOLS -> onSymbolsLayoutSwitchClick()
                            LABEL_1by2 -> onSymbolsUrduLayoutSwitchClick()
                            LABEL_2by2 -> onLayoutSwitchClick()
                            LABEL_1by2Sindhi -> onSymbolsSindhiLayoutSwitchClick()
                            LABEL_2by2Sindhi -> onLayoutSwitchClick()
                            LABEL_NUMBERS -> onNumbersSwitchClick()
                            else -> onKeyPressed()
                        }
                    },
                    onLongPress = {
                        isLongPressed = true
                        if (key.labelMain == LABEL_CAPS) {
                            onCapsClickToLock()
                        } else {
                            onLongKeyPressed()
                        }
                        onClickSound(context = context, myPreferences = myPreferences)
                        onClickVibrate(context = context, isLongPressed = true, myPreferences = myPreferences)
                    },
                    onPress = { offset ->
                        isPopupVisible.value = true
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        onLongKeyPressedEnd()
                        isLongPressed = false
                        interactionSource.emit(PressInteraction.Release(press))
                        delay(20)
                        isPopupVisible.value = false

                        if (key.labelMain !in listOf("۱/۲ ", "۲/۲ ", "۱/۲", "۲/۲")) {
                            if (key.isSymbolLayout) {
                                onLayoutSwitchClick()
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { deleteTextAfterDrag() }
                ) { change, _ ->
                    onDragGestureToSelect(change.position.x.toInt())
                }
            }
    ) {
        // Main key content
        if (key.shouldShowIcon) {
            // Cache the painter so it's only loaded once per key
            val painter = painterResource(id = icon)

            Icon(
                painter = painter,
                contentDescription = key.contentDescription,
                tint = if (key.labelMain == LABEL_CAPS ||
                    key.labelMain == LABEL_123 ||
                    key.labelMain == LABEL_DELETE) specialTint else textColor
            )
        } else {
            if (key.shouldShowLanguageName) {
                AutoResizedText(
                    text = keyboardName,
                    color = textColor
                )
            } else if (key.shouldBeResized) {
                AutoResizedText(
                    text = key.labelMain,
                    color = textColor
                )
            } else {
                Text(
                    text = labelMain,
                    color = textColor,
                    fontSize = keyTextSize.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
        }

        // Secondary label (Hint text)
        if (myPreferences?.getLongPressForSymbols() == true) {
            key.labelSecondary?.let {
                Text(
                    text = it,
                    color = textColor,
                    fontSize = dimensionResource(id = R.dimen.key_textHintSize).value.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                )
            }
        }

        // Popup Screen
        if (myPreferences?.getShowPopUp() == true && key.shouldShowPopUp) {
            PopupScreen(
                label = popUpLabel,
                offset = IntOffset(x = 0, y = -130),
                isVisible = isPopupVisible.value,
                onClick = { onKeyPressed() }
            )
        }
    }
}


