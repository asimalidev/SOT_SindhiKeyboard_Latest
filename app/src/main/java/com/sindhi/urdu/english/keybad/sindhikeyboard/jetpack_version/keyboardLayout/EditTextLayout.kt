package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout

import android.content.Context
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout.textEditLayout
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.LABEL_DELETE
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.CustomKey
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService

@Composable
fun EditTextLayout(
    keyboardHeight: Dp = 208.dp,
    specialKeys: Array<Array<Array<Key>>>? = textEditLayout,
    imeService: CustomImeService? = null,
    context: Context? = null,
    vibrator: Vibrator? = null
) {
    var isSelectionEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)) {
        specialKeys?.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .weight(if (rowIndex == 0) 3f else 1f)) {
                row.forEachIndexed { _, keys ->
                    val weight = if (rowIndex == 0) 1f else keys[0].weight
                    Column(
                        modifier = Modifier.weight(weight),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        keys.forEachIndexed { _, key ->
                            CustomKey(
                                key = key,
                                onKeyPressed = {
                                    if (key.labelMain == LABEL_DELETE) {
                                        imeService?.doSomethingWith(key, false)
                                    } else {
                                        imeService?.doEditText(key = key, longPressed = false)
                                    }
                                    isSelectionEnabled = imeService?.isSelectionModeActive == true
                                },
                                onDragGestureToSelect = { dragAmount ->
                                    if (key.labelMain == LABEL_DELETE) {
                                        imeService?.selectTextGesture(dragAmount)
                                    }
                                    isSelectionEnabled = imeService?.isSelectionModeActive == true
                                },
                                deleteTextAfterDrag = {
                                    if (key.labelMain == LABEL_DELETE) {
                                        imeService?.deleteText()
                                        Log.e("DeleteText","edit text layout")
                                    }
                                },
                                onLongKeyPressed = {
                                    if (key.labelMain == LABEL_DELETE) {
                                        imeService?.doSomethingWith(key, true)
                                    } else {
                                        imeService?.doEditText(key = key, longPressed = true)
                                    }
                                    isSelectionEnabled = imeService?.isSelectionModeActive == true
                                },
                                onLongKeyPressedEnd = {
                                    imeService?.longPressedStops(key)
                                },
                                modifier = Modifier
                                    .weight(key.weight)
                                    .padding(4.dp),
                                isSelectionEnabled = isSelectionEnabled,
                                imeService = imeService,
                                onLayoutSwitchClick = {},
                                onExtendedSymbolsSwitchClick = {},
                                onNumbersSwitchClick = {},
                                onSymbolsLayoutSwitchClick = {},
                                onSymbolsUrduLayoutSwitchClick = {},
                                onSymbolsSindhiLayoutSwitchClick = {},
                                onHusnEguftarOneLayoutSwitchClick = {},
                                onHusnEguftarTwoLayoutSwitchClick = {},
                                onHusnEguftarThreeLayoutSwitchClick = {},
                                onCapsClick = {},
                                onLanguageSwitchClick = {},
                                onCapsClickToLock = {},
                                context = context,
                                vibrator = vibrator
                            )
                        }
                    }
                }
            }
        }
    }
}
