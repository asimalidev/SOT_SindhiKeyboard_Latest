package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes

import androidx.annotation.Keep
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sindhi.urdu.english.keybad.R

@Keep
data class Key(
    var labelMain: String,
    val weight: Float,
    val code: Int? = null,
    var labelSecondary: String? = null,
    val codeSecondary: Int? = null,
    val valueMain: String = labelMain,
    val verticalOffset: Dp = 0.dp, // Add this for vertical adjustment
    val valueSecondary: String? = labelSecondary,
    val isSpecialCharacter: Boolean = false,
    val isLanguageKey: Boolean = false,
    val shouldBeResized: Boolean = false,
    val shouldShowLanguageName: Boolean = false,
    val isSelectionKey: Boolean = false,
    val isSymbolLayout: Boolean = false,
    val icon: Int = R.drawable.backspace,
    val contentDescription: String? = null,
    val shouldShowPopUp: Boolean = true,
    val shouldShowIcon: Boolean = false,
    val isCharacter: Boolean = true,
    val shouldBeRounded: Boolean = false
)

