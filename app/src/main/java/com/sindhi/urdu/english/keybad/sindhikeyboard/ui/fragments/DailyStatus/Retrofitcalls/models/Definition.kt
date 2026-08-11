package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models

import androidx.annotation.Keep


@Keep
data class Definition(
    val antonyms: List<Any>,
    val definition: String,
    val example: String,
    val synonyms: List<Any>
)