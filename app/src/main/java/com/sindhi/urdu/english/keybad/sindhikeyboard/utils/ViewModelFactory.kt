package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel

class ViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        HistoryViewModel::class.java -> HistoryViewModel(application)
        else -> throw IllegalArgumentException("Unknown ViewModel class")
    } as T
}