package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText

import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object TranslatorCall {
    fun callUrlAndParseResult(str: String, inputCode: String?, outPutLanguageCode: String?): String? {
        var str = str
        val str2 = "&"
        var result: String? = ""
        val contains = str.contains(str2)
        val str3 = IOUtils.LINE_SEPARATOR_UNIX
        if (contains || str.contains(str3)) {
            str = str.trim { it <= ' ' }.replace(str2, "^~^").trim { it <= ' ' }.replace(str3, "~~")
        }
        val sb = StringBuilder()
        sb.append("https://translate.googleapis.com/translate_a/single?client=dict-chrome-ex&sl=")
        //   sb.append("inputLangCode");
        sb.append(inputCode)
        sb.append("&tl=")
        //    sb.append("outputLangCode");
        sb.append(outPutLanguageCode)
        sb.append("&dt=t&q=")
        sb.append(str.trim { it <= ' ' }.replace(" ", "%20"))
        sb.append("&ie=UTF-8&oe=UTF-8")
        val sb2 = sb.toString()
        val sb3 = StringBuilder()
        try {
            val httpURLConnection = URL(sb2).openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
            while (true) {
                val readLine = bufferedReader.readLine() ?: break
                sb3.append(readLine)
            }
            bufferedReader.close()
//            result = parseResult(sb3.toString())
//            if (result == null) {
//                result = ""
//            }
            result = parseResult(sb3.toString())
            try {
                result = result.replace("~~",System.lineSeparator())
            }catch (e : Exception){
            }


        } catch (e: MalformedURLException) {
            result = ""
        } catch (e2: IOException) {
            result = ""
        }
        return result
    }

    fun callUrlForDictionary(inputCode: String?,str: String): String? {
//        var str = str
//        val str2 = "&"
        var result: String? = ""
//        val contains = str.contains(str2)
//        val str3 = IOUtils.LINE_SEPARATOR_UNIX
//        if (contains || str.contains(str3)) {
//            str = str.trim { it <= ' ' }.replace(str2, "^~^").trim { it <= ' ' }.replace(str3, "~~")
//        }
        val sb = StringBuilder()
        sb.append("https://api.dictionaryapi.dev/api/v1/entries/")
        sb.append(inputCode)
        sb.append("/")
        sb.append(str)
        val sb2 = sb.toString()
        val sb3 = StringBuilder()
        result = try {
            val httpURLConnection = URL(sb2).openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
            while (true) {
                val readLine = bufferedReader.readLine() ?: break
                sb3.append(readLine)
            }
            bufferedReader.close()
            sb3.toString()
        } catch (e: MalformedURLException) {
            ""
        } catch (e2: IOException) {
            ""
        }
        return result
    }

    fun parseResult(str: String): String {
        var str2 = ""
        // val str3 = "Something went Wrong"
        try {
            val jSONArray = JSONArray(str)[0] as JSONArray
            var i = 0
            while (i < jSONArray.length()) {
                try {
                    val jSONArray2 = jSONArray[i] as JSONArray
                    val sb = StringBuilder()
                    sb.append(str2)
                    sb.append(jSONArray2[0].toString())
                    str2 = sb.toString()
                    i++
                } catch (e: JSONException) {
                    return str2
                } catch (e2: Exception) {
                    return str2
                }
            }
        } catch (e3: JSONException) {
            return str2
        } catch (e4: Exception) {
            return str2
        }
        return str2
    }
}