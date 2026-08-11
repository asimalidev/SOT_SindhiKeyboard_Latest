package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.ViewmodelFactory

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.Baseresponse
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.viewmodel.dictionaryrepository
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.dailystatusResponse
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.dictionary.models.dictionarywordresponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DictionaryViewModel(
    application: Application,
    val navController: NavController,
    val activity: Activity,
    val dictionaryrepository: dictionaryrepository
) : AndroidViewModel(application) {

    val wordslivedata = MutableLiveData<Baseresponse<dictionarywordresponse>>()
    val dailystatuslivedata = MutableLiveData<Baseresponse<dailystatusResponse>>()

    fun getwordsdetails(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = dictionaryrepository.getwordsdetails(word)
                if (result.code() == 200) {
                    wordslivedata.postValue(Baseresponse.Success(result.body()))
                }

            } catch (e: Exception) {
                wordslivedata.postValue(Baseresponse.Error(e.message.toString()))
            }
        }
    }

    fun getdailystatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = dictionaryrepository.getdailystatus()
                if (result.code() == 200) {
                    dailystatuslivedata.postValue(Baseresponse.Success(result.body()))
                }

            } catch (e: Exception) {
                dailystatuslivedata.postValue(Baseresponse.Error(e.message.toString()))
            }
        }
    }
}
