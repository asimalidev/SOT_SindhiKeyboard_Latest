package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "convoto_tb")
class ConversationTo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var source: String? = null,
    var translation: String? = null,
    var from:String?=null,
    var time:Long?=null
)