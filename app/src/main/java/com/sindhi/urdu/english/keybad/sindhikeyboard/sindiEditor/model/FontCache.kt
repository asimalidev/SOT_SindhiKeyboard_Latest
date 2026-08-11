package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model

import android.content.Context
import android.graphics.Typeface
import android.util.Log

object FontCache {
    var fontCache: HashMap<String, Typeface?> = HashMap()

    // 1. ADD THIS LINE to keep track of Urdu fonts
    var urduFontNames: HashSet<String> = HashSet()

    fun loadAllTypefacesFromAssets(context: Context) {
        try {
            val assets = context.assets
            val myUrduFontFiles = assets.list("urdu_fonts_inside_app")?.filter { it.endsWith(".ttf") } ?: emptyList()
            val myEnglishFontFiles = assets.list("english_fonts_inside_app")?.filter { it.endsWith(".ttf") } ?: emptyList()

            // 2. ADD THIS LINE to save the names into the set
            urduFontNames.addAll(myUrduFontFiles)

            for (fontName in myUrduFontFiles) {
                if (!fontCache.containsKey(fontName)) {
                    try {
                        val typeface = Typeface.createFromAsset(assets, "urdu_fonts_inside_app/$fontName")
                        fontCache[fontName] = typeface
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fontCache[fontName] = null
                    }
                }
            }

            for (fontName in myEnglishFontFiles) {
                if (!fontCache.containsKey(fontName)) {
                    try {
                        val typeface = Typeface.createFromAsset(assets, "english_fonts_inside_app/$fontName")
                        fontCache[fontName] = typeface
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fontCache[fontName] = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("TAG", "Error loading fonts from assets", e)
        }
    }

    // 3. ADD THIS HELPER FUNCTION
    fun isUrduFont(fontName: String): Boolean {
        return urduFontNames.contains(fontName)
    }

    fun getTypeface(context: Context,fontName: String): Typeface? {
        if (fontCache.isEmpty()) {
            loadAllTypefacesFromAssets(context)
        }
        return fontCache[fontName]
    }

    fun loadFonts(context: Context): List<String> {
        if (fontCache.isEmpty()) {
            loadAllTypefacesFromAssets(context)
        }
        return fontCache.keys.toList()
    }
}