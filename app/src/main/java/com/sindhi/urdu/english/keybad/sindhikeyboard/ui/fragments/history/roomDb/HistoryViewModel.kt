package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.history_fragment.Companion.conversationExtensionForwardList
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.ConversationTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.invoke

class HistoryViewModel(context: Application) : AndroidViewModel(context) {

    var allWords: LiveData<List<WordEntity>>
    var conversationExtReFormulated: LiveData<List<ConversationExtension>>
    private val repository: HistoryRepo

    var conversationExtensionTemp: LiveData<List<ConversationExtensionTemp>>

    private val _conversationExtensionPermanent = MutableLiveData<List<ConversationExtension>>()
    val conversationExtensionPermanent: LiveData<List<ConversationExtension>> = _conversationExtensionPermanent

    var convoto: LiveData<List<ConversationTo>>

    var myContext: Context

    init {
        myContext = context
        val dao = HistoryDb.invoke(context).getwordDao()
        repository = HistoryRepo(dao)
        allWords = repository.searchedWords

        conversationExtensionTemp = MutableLiveData<List<ConversationExtensionTemp>>().apply {
            value = emptyList()
        }

        conversationExtReFormulated = repository.getConversationExtReFormulated
        convoto = repository.convoto
    }

    fun updateTheList(fromTemp: Boolean) {
        if (fromTemp) {
            conversationExtensionTemp = repository.conversationTemporary
        } else {
            _conversationExtensionPermanent.postValue(conversationExtensionForwardList)
        }
    }

    fun insertWords(wordEntity: WordEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWord(wordEntity)
        }
    fun deleteWord(wordEntity: WordEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWord(wordEntity)
        }

    fun deleteSearchedHistory() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteData()
    }

    fun insertConvoTemp(conversationExtTemp: ConversationExtensionTemp) = viewModelScope.launch(
        Dispatchers.IO) {
        repository.insertConvoTemp(conversationExtTemp)
    }

    fun insertConversationReFormulated(conversationExt: ConversationExtension) = viewModelScope.launch(
        Dispatchers.IO) {
        repository.insertConversationReFormulated(conversationExt)
    }

    fun deleteFromTemporaryDB() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteFromTemporaryDB()
    }

    fun deleteSelectedConversationHistory(conversationName: String) = viewModelScope.launch(
        Dispatchers.IO) {
        repository.deleteSelectedConversationHistory(conversationName)
    }
}