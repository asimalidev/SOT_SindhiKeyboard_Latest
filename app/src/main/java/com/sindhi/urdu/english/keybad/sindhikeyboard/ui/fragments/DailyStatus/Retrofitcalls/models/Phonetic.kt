package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models

import androidx.annotation.Keep


@Keep
data class Phonetic(
    val audio: String,
    val sourceUrl: String,
    val text: String
)