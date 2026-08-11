package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.layout

import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key

//val symbolsUrduLayout = arrayOf(
//
//    arrayOf(
//        Key("؟", 0.9f, isSymbolLayout = true),
//        Key("ي", 0.9f, isSymbolLayout = true),
//        Key("ِ ", 0.9f, isSymbolLayout = true),
//        Key("َ ", 0.9f, isSymbolLayout = true),
//        Key("،", 0.9f, isSymbolLayout = true),
//        Key("“", 0.9f, isSymbolLayout = true),
//        Key("”", 0.9f, isSymbolLayout = true),
//        Key("ّ ", 0.9f, isSymbolLayout = true),
//        Key("ؓ ", 0.9f, isSymbolLayout = true),
//        Key("ؒ ", 0.9f, isSymbolLayout = true),
//
//        ),
//
//    arrayOf(
//        Key("ص", 0.9f, isSymbolLayout = true),
//        Key("ژ", 0.9f, isSymbolLayout = true),
//        Key("ڑ", 0.9f, isSymbolLayout = true),
//        Key("ذ", 0.9f, isSymbolLayout = true),
//        Key("ڈ", 0.9f, isSymbolLayout = true),
//        Key("خ", 0.9f, isSymbolLayout = true),
//        Key("ح", 0.9f, isSymbolLayout = true),
//        Key("ث", 0.9f, isSymbolLayout = true),
//        Key("ٹ", 0.9f, isSymbolLayout = true),
//        Key("آ", 0.9f, isSymbolLayout = true),
//    ),
//
//    arrayOf(
//        Key("أ", 0.9f, isSymbolLayout = true),
//        Key("ں", 0.9f, isSymbolLayout = true),
//        Key("ة", 0.9f, isSymbolLayout = true),
//        Key("ٶ", 0.9f, isSymbolLayout = true),
//        Key("ۓ", 0.9f, isSymbolLayout = true),
//        Key("ٸ", 0.9f, isSymbolLayout = true),
//        Key("غ", 0.9f, isSymbolLayout = true),
//        Key("ظ", 0.9f, isSymbolLayout = true),
//        Key("ض", 0.9f, isSymbolLayout = true),
//    ),
//
//    arrayOf(
//        Key("۲/۲", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false),
//        Key("ٗ ",1f, isSymbolLayout = true),
//        Key("ً ",1f, shouldBeResized = true),
//        Key("ٍ ",1f, shouldBeResized = true),
//        Key("ُ ",1f, shouldBeResized = true),
//        Key("ٰ ",1f, shouldBeResized = true),
//        Key("إ",1f, isSymbolLayout = true),
//        Key("؟",1f, isSymbolLayout = true),
//        Key("Delete", 1.5f, isSpecialCharacter = true, icon = R.drawable.backspace, shouldShowPopUp = false, isCharacter = false, shouldShowIcon = true, contentDescription = "Delete", isSymbolLayout = true)
//    ),
//
//    arrayOf(
//        Key("?123", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false, shouldBeRounded = true, isSymbolLayout = true),
//        Key(",", 1f, code = 44, isSymbolLayout = true),
//        Key("اردو", 4f, valueMain = " ", shouldShowPopUp = false, isCharacter = false, shouldBeResized = true, shouldShowLanguageName = true, isLanguageKey = true, code = 32, isSymbolLayout = true),
//        Key("۔", 1f, isSymbolLayout = true),
//        Key(labelMain = "Done", weight = 1.5f, isSpecialCharacter = true, icon = R.drawable.done_icon, shouldShowPopUp = false, shouldShowIcon = true, isCharacter = false, contentDescription = "Done", shouldBeRounded = true, isSymbolLayout = true)
//    )
//)

val symbolsUrduLayout = arrayOf(

    arrayOf(
        Key("؟", 0.9f, isSymbolLayout = true),
        Key("ي", 0.9f, isSymbolLayout = true),
        Key("◌ِ", 0.9f, valueMain = "ِ", shouldBeResized = true),
        Key("◌َ", 0.9f, valueMain = "َ", shouldBeResized = true),
        Key("،", 0.9f, isSymbolLayout = true),
        Key("“", 0.9f, isSymbolLayout = true),
        Key("”", 0.9f, isSymbolLayout = true),
        // FIX: Removed `isSymbolLayout = true`
        Key("◌ّ", 0.9f, valueMain = "ّ", shouldBeResized = true),
        Key("◌ؓ", 1f, valueMain = "ؓ", shouldBeResized = true),
        Key("◌ؒ", 0.9f, valueMain = "ؒ", shouldBeResized = true)
    ),

    arrayOf(
        Key("ص", 0.9f, isSymbolLayout = true),
        Key("ژ", 0.9f, isSymbolLayout = true),
        Key("ڑ", 0.9f, isSymbolLayout = true),
        Key("ذ", 0.9f, isSymbolLayout = true),
        Key("ڈ", 0.9f, isSymbolLayout = true),
        Key("خ", 0.9f, isSymbolLayout = true),
        Key("ح", 0.9f, isSymbolLayout = true),
        Key("ث", 0.9f, isSymbolLayout = true),
        Key("ٹ", 0.9f, isSymbolLayout = true),
        Key("آ", 0.9f, isSymbolLayout = true),
    ),

    arrayOf(
        Key("أ", 0.9f, isSymbolLayout = true),
        Key("ں", 0.9f, isSymbolLayout = true),
        Key("ة", 0.9f, isSymbolLayout = true),
        Key("ٶ", 0.9f, isSymbolLayout = true),
        Key("ۓ", 0.9f, isSymbolLayout = true),
        Key("ٸ", 0.9f, isSymbolLayout = true),
        Key("غ", 0.9f, isSymbolLayout = true),
        Key("ظ", 0.9f, isSymbolLayout = true),
        Key("ض", 0.9f, isSymbolLayout = true),
    ),

    arrayOf(
        Key("۲/۲", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false),
        // FIX: Removed `isSymbolLayout = true` from these diacritics as well
        Key("◌ٗ", 1f, valueMain = "ٗ", shouldBeResized = true),
        Key("◌ً", 1f, valueMain = "ً", shouldBeResized = true),
        Key("◌ٍ", 1f, valueMain = "ٍ", shouldBeResized = true),
        Key("◌ُ", 1f, valueMain = "ُ", shouldBeResized = true),
        Key("◌ٰ", 1f, valueMain = "ٰ", shouldBeResized = true),
        Key("إ", 1f, isSymbolLayout = true),
        Key("؟", 1f, isSymbolLayout = true),
        Key("Delete", 1.5f, isSpecialCharacter = true, icon = R.drawable.backspace, shouldShowPopUp = false, isCharacter = false, shouldShowIcon = true, contentDescription = "Delete", isSymbolLayout = true)
    ),

    arrayOf(
        Key("?123", 1.5f, isCharacter = false, isSpecialCharacter = true, shouldBeResized = true, shouldShowPopUp = false, shouldBeRounded = true, isSymbolLayout = true),
        Key(",", 1f, code = 44, isSymbolLayout = true),
        Key("اردو", 4f, valueMain = " ", shouldShowPopUp = false, isCharacter = false, shouldBeResized = true, shouldShowLanguageName = true, isLanguageKey = true, code = 32, isSymbolLayout = true),
        Key("۔", 1f, isSymbolLayout = true),
        Key(labelMain = "Done", weight = 1.5f, isSpecialCharacter = true, icon = R.drawable.done_icon, shouldShowPopUp = false, shouldShowIcon = true, isCharacter = false, contentDescription = "Done", shouldBeRounded = true, isSymbolLayout = true)
    )
)