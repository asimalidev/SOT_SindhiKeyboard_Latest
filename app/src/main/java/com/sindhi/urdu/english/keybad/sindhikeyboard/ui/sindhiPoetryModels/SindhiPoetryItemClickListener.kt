package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels

interface SindhiPoetryItemClickListener {
    fun onPoetryItemCopyClicked(poetry: String)
    fun onPoetryItemShareClicked(poetry: String)
    fun onPoetryItemWhatsappClicked(poetry: String)
    fun onPoetryItemMessengerClicked(poetry: String)
    fun onPoetryItemTwitterClicked(poetry: String)
    fun onPoetryItemEditClicked(poetry: String) // <-- New Method
}