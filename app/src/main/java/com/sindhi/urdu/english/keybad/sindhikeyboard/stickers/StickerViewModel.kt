package com.sindhi.urdu.english.keybad.sindhikeyboard.stickers

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.downloadInProgressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.progressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.StickersViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zeroturnaround.zip.ZipUtil
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import kotlin.text.set

class StickerViewModel : ViewModel() {
    private val repository = StickerRepository()

    private val _stickerPackData = MutableLiveData<StickerPackData?>()
    val stickerPackData: MutableLiveData<StickerPackData?> = _stickerPackData

    private val _downloadProgress = MutableLiveData<Pair<String, String>>()
    val downloadProgress: LiveData<Pair<String, String>> = _downloadProgress

    private val _stickerPackList = MutableLiveData<List<StickerPackInfo>>()
    val stickerPackList: LiveData<List<StickerPackInfo>> = _stickerPackList

    fun fetchStickers() {
        viewModelScope.launch {
            try {
                val data = repository.getStickers()
                if (data != null) {
                    _stickerPackData.value = data
                    StickerDataCache.stickerPackData = data
                } else {
                    Log.e("StickerViewModel", "Failed to fetch stickers: Response is null")
                }
            } catch (e: Exception) {
                Log.e("StickerViewModel", "Error fetching stickers", e)
            }
        }
    }

    fun fetchDownloadStickers(context: Context): MutableList<StickerPackInfo> {
        val stickerPacks = mutableListOf<StickerPackInfo>()
        viewModelScope.launch {
            val stickerPackDirectory = File(context.filesDir,"stickers")
            if (stickerPackDirectory.exists()) {
                stickerPackDirectory.listFiles()?.forEach { packFolder ->
                    packFolder.listFiles()?.forEach { insidePackFolder ->
                        val stickerFiles = insidePackFolder.listFiles()?.toList() ?: emptyList()
                        if (stickerFiles.isNotEmpty()) {
                            Log.i("fetchDownloadStickers","Name ::"+ insidePackFolder.name + " Size :: " +stickerFiles.size)
                            stickerPacks.add(StickerPackInfo(folderName = insidePackFolder.name, stickerFiles = stickerFiles))
                        }
                    }
                }
            } else {
                return@launch
            }
        }
        return stickerPacks
    }

    fun updateDownloadProgress(packName: String, progress: String) {
        _downloadProgress.postValue(Pair(packName, progress))
    }

    fun downloadStickerPack(stickerPack: StickerPack, context: Context) {
        viewModelScope.launch {
            val destinationPath = "${context.filesDir.absolutePath}/stickers/${stickerPack.name}"
            try {
                val success = downloadFile(
                    destinationPath,
                    stickerPack.name,
                    stickerPack.publisher_website
                )
                if (success) {
                    val zipFile = File(destinationPath, "${stickerPack.name}.zip")
                    if (zipFile.exists()) {
                        unzipFile(zipFile, destinationPath)
                        _downloadProgress.postValue(Pair(stickerPack.name, "Download Complete"))
                        progressMap[stickerPack.name] = "Download Complete"
                        if (context is StickersViewActivity) {
                            context.stickersPackAdapter.markDownloadComplete(stickerPack.name)
                        }
                    } else {
                        _downloadProgress.postValue(Pair(stickerPack.name, "Download Failed"))
                    }
                } else {
                    _downloadProgress.postValue(Pair(stickerPack.name, "Download Failed"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadProgress.postValue(Pair(stickerPack.name, "Download Failed"))
            }
        }
    }

    private fun unzipFile(zipFile: File, destinationFolder: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    ZipUtil.unpack(zipFile, File(destinationFolder))
                    zipFile.delete()
                    Log.d("DownloadWork", "Unzipping successful")
                } catch (e: Exception) {
                    Log.e("DownloadWork", "Error unzipping: ${e.message}")
                }
            }
        }
    }

    private suspend fun downloadFile(destinationFolder: String, folderName: String, url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection: URLConnection = URL(url).openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val input: InputStream = BufferedInputStream(URL(url).openStream(), 8192)
                val directory = File(destinationFolder)
                if (!directory.exists()) directory.mkdirs()
                val localFile = File(directory, "$folderName.zip")
                if (localFile.exists()) localFile.delete()
                localFile.createNewFile()
                val output: OutputStream = FileOutputStream(localFile)
                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    val progress = (total * 100 / lengthOfFile).toInt()
                    updateDownloadProgress(folderName, "Progress $progress%") // Update progress
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()

                return@withContext true
            } catch (e: Exception) {
                Log.e("DownloadError", "Download failed: ${e.message}")
                return@withContext false
            }
        }
    }

    fun loadStickerPacks(context: Context) {
        viewModelScope.launch {
            val stickerPackDirectory = File(context.filesDir, "stickers")
            val stickerPacks = mutableListOf<StickerPackInfo>()

            withContext(Dispatchers.IO) {
                if (stickerPackDirectory.exists()) {
                    stickerPackDirectory.listFiles()?.forEach { packFolder ->
                        if (packFolder.isDirectory) {
                            progressMap[packFolder.name] = "Download Complete"
                            downloadInProgressMap[packFolder.name] = true
                            packFolder.listFiles()?.forEach { insidePackFolder ->
                                if (insidePackFolder.isDirectory) {
                                    val stickerFiles = insidePackFolder.listFiles { file -> file.extension == "webp" }?.toList() ?: emptyList()
                                    if (stickerFiles.isNotEmpty()) {
                                        stickerPacks.add(StickerPackInfo(folderName = packFolder.name, stickerFiles = stickerFiles))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            _stickerPackList.postValue(stickerPacks)
        }
    }
}