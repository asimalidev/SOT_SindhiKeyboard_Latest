package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.Conversation
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtensionTemp
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.WordEntity

@Dao
interface WordDao {

    @Insert
    fun word(word: WordEntity)

    @Query("DELETE FROM history_tb WHERE word = :word AND def = :def")
    fun deleteWordWhere(word: String?, def: String?)

    @Insert
    fun insertConvoTemp(conversationExtTemp: ConversationExtensionTemp)

    @Insert
    fun insertConversationReFormulated(conversationExt: ConversationExtension)

    @Query("Select * from history_tb")
    fun getWord(): LiveData<List<WordEntity>>

    @Query("Delete From history_tb")
    fun deleteAll()

    @Query("Delete From convo_ext_tb_temp")
    fun deleteFromTemporaryDB()

    @Query("DELETE FROM convo_ext_tb WHERE conversationName = :conversationName")
    fun deleteSelectedConversationHistory(conversationName: String)

    @Query("Select * from convo_tb")
    fun getfrom(): LiveData<List<Conversation>>

    @Query("Select * from convo_ext_tb_temp")
    fun getConversationTemporary(): LiveData<List<ConversationExtensionTemp>>

    @Query("Select * from convoto_tb")
    fun getto(): LiveData<List<ConversationTo>>

    @Query("Select * from convo_ext_tb")
    fun getConversationExtReFormulated(): LiveData<List<ConversationExtension>>
}