package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout

import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key

val urdu_layout = arrayOf(

    arrayOf(
        Key("۱", 1f),
        Key("۲", 1f),
        Key("۳", 1f),
        Key("۴", 1f),
        Key("۵", 1f),
        Key("٦", 1f),
        Key("۷", 1f),
        Key("۸", 1f),
        Key("۹", 1f),
        Key("۰", 1f)
    ),

    arrayOf(
        Key("ق",1f),
        Key("و",1f),
        Key("ع",1f),
        Key("ر",1f),
        Key("ت",1f),
        Key("ے",1f),
        Key("ء",1f),
        Key("ی",1f),
        Key("ه",1f),
        Key("پ",1f)
    ),

    arrayOf(
        Key("ا", 1f),
        Key("س", 1f),
        Key("د", 1f),
        Key("ف", 1f),
        Key("گ", 1f),
        Key("ھ", 1f),
        Key("ج", 1f),
        Key("ک", 1f),
        Key("ل", 1f)
    ),

    arrayOf(
        Key("۱/۲", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false, shouldBeRounded = false),
        Key("ز",1f),
        Key("ش",1f),
        Key("چ",1f),
        Key("ط",1f),
        Key("ب",1f),
        Key("ن",1f),
        Key("م",1f),
        Key("Delete", 1.5f, isSpecialCharacter = true, icon = R.drawable.backspace, shouldShowPopUp = false, isCharacter = false, shouldShowIcon = true, contentDescription = "Delete")
    ),

    arrayOf(
        Key("?123", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false, shouldBeRounded = true),
        Key(labelMain = "Languages", weight = 2f, icon = R.drawable.ic_sindhi_txt, shouldShowPopUp = false, shouldShowIcon = true, isCharacter = false, contentDescription = "Languages", shouldBeRounded = true),
        Key("Space", 5f, code = 32, icon = R.drawable.space, shouldShowPopUp = false, shouldShowIcon = true, isCharacter = false, isLanguageKey = true, contentDescription = "Space"),
        Key("۔", 1f),
        Key(labelMain = "Done", weight = 1.5f, isSpecialCharacter = true, icon = R.drawable.done_icon, shouldShowPopUp = false, shouldShowIcon = true, isCharacter = false, contentDescription = "Done", shouldBeRounded = true)
    )
)

