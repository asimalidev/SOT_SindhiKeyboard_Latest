package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb

import androidx.lifecycle.LiveData
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.ConversationTo
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.WordDao

class HistoryRepo(private val wordDao: WordDao) {

    val searchedWords: LiveData<List<WordEntity>> = wordDao.getWord()

    val getConversationExtReFormulated: LiveData<List<ConversationExtension>> = wordDao.getConversationExtReFormulated()
    val conversationTemporary: LiveData<List<ConversationExtensionTemp>> = wordDao.getConversationTemporary()

    val convoto: LiveData<List<ConversationTo>> = wordDao.getto()

    suspend fun insertWord(p: WordEntity) {
        wordDao.word(p)
    }

    fun deleteWord(wordEntity: WordEntity) {
        wordDao.deleteWordWhere(wordEntity.word, wordEntity.def)
    }

    suspend fun deleteData() {
        wordDao.deleteAll()
    }

    suspend fun deleteFromTemporaryDB() {
        wordDao.deleteFromTemporaryDB()
    }

    suspend fun insertConvoTemp(conversationExtTemp: ConversationExtensionTemp) {
        wordDao.insertConvoTemp(conversationExtTemp)
    }

    suspend fun insertConversationReFormulated(conversationExt: ConversationExtension) {
        wordDao.insertConversationReFormulated(conversationExt)
    }

    suspend fun deleteSelectedConversationHistory(conversationName: String) {
        wordDao.deleteSelectedConversationHistory(conversationName)
    }
}