package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models

import androidx.annotation.Keep


@Keep
data class dictionarywordresponseItem(
    val license: License,
    val meanings: List<Meaning>,
    val phonetic: String,
    val phonetics: List<Phonetic>,
    val sourceUrls: List<String>,
    val word: String
)