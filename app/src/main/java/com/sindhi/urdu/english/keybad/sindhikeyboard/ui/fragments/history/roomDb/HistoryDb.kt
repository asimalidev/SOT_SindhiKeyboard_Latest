package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.WordDao

@Keep
@Database(entities = [WordEntity::class, Conversation::class,
    Conversationto::class, ConversationExtension::class, ConversationExtensionTemp::class], version = 4)
@TypeConverters(DateConverter::class)
abstract class HistoryDb : RoomDatabase() {

    abstract fun getwordDao(): WordDao

    companion object {
        @Volatile
        private var instance: HistoryDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                HistoryDb::class.java,
                "HistoryDb.db")
                .fallbackToDestructiveMigration().build()
    }
}
