package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models

import androidx.annotation.Keep


@Keep
data class Meaning(
    val antonyms: List<Any>,
    val definitions: List<Definition>,
    val partOfSpeech: String,
    val synonyms: List<String>
)