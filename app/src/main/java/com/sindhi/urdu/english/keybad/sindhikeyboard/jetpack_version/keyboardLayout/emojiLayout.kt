package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.ColorStateList
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.emoji2.emojipicker.RecentEmojiProviderAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomRecentEmojiProvider
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.ForegroundCheckTask
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPackInfo
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.FOFStartActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.StickersRowGridAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.StickersRowLinearAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


@Composable
fun EmojiLayout(
    ctx: Context?,
    color1: Color? = MaterialTheme.colorScheme.onSurfaceVariant,
    color2: Color? = MaterialTheme.colorScheme.onSurfaceVariant,
    isEmojiButtonClicked: Boolean,
    isStickerButtonClicked: Boolean,
    stickerViewModel: MutableList<StickerPackInfo>,
    onEmojiClick: (String) -> Unit,
    sendStickerTest: (file: File) -> Unit
) {

    val stickersView = remember {
        LayoutInflater.from(ctx).inflate(R.layout.custom_sticker_picker, null) as ConstraintLayout
    }
    val emojiPickerView =
        remember { stickersView.findViewById<EmojiPickerView>(R.id.emojiPickerView) }
    val clKeypadStickers =
        remember { stickersView.findViewById<ConstraintLayout>(R.id.clKeypadStickers) }
    val clNoStickersAdded =
        remember { stickersView.findViewById<ConstraintLayout>(R.id.clNoStickersAdded) }
    val addMoreStickers = remember { stickersView.findViewById<ImageView>(R.id.addMoreStickers) }

    val ivNoStickersAdded =
        remember { stickersView.findViewById<ImageView>(R.id.ivNoStickersAdded) }
    val tvNoStickersAdded =
        remember { stickersView.findViewById<AppCompatTextView>(R.id.tvNoStickersAdded) }
    val btnAddMoreStickers = remember { stickersView.findViewById<Button>(R.id.btnAddMoreStickers) }
    val rvStickersKeypadTrayIcon =
        remember { stickersView.findViewById<RecyclerView>(R.id.rvStickersKeypadTrayIcon) }
    val rvStickersKeypadGridIcon =
        remember { stickersView.findViewById<RecyclerView>(R.id.rvStickersKeypad) }

    AndroidView(
        factory = { stickersView },
        update = { view ->
            ivNoStickersAdded.background?.let { bgDrawable ->
                DrawableCompat.setTint(bgDrawable, color2!!.toArgb())
                ivNoStickersAdded.background = bgDrawable
            }

            ivNoStickersAdded.drawable?.let { srcDrawable ->
                DrawableCompat.setTint(srcDrawable, color1!!.toArgb())
                ivNoStickersAdded.setImageDrawable(srcDrawable)
            }

            tvNoStickersAdded.setTextColor(color2!!.toArgb())
            btnAddMoreStickers.setTextColor(color2.toArgb())
            btnAddMoreStickers.backgroundTintList = ColorStateList.valueOf(color1!!.toArgb())
            if (isEmojiButtonClicked) {
                Log.i("EmojiLayout", "isEmojiButtonClicked")
                emojiPickerView.visibility = View.VISIBLE
                clKeypadStickers.visibility = View.INVISIBLE
                clNoStickersAdded.visibility = View.INVISIBLE

                try {
                    emojiPickerView.setRecentEmojiProvider(
                        RecentEmojiProviderAdapter(
                            CustomRecentEmojiProvider(view.context)
                        )
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        view.context,
                        "Device storage is full. Please free up space.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                emojiPickerView.setOnEmojiPickedListener { emoji ->
                    if (!emoji.emoji.isNullOrBlank()) {
                        onEmojiClick(emoji.emoji)
                    }
                }
            } else if (isStickerButtonClicked) {
                Log.i("EmojiLayout", "isStickerButtonClicked")
                emojiPickerView.visibility = View.GONE
                clKeypadStickers.visibility = View.VISIBLE
                addMoreStickers.bringToFront()
                addMoreStickers.isClickable = true
                addMoreStickers.isFocusable = true
                addMoreStickers.isFocusableInTouchMode = true
                addMoreStickers.elevation = 10f
                addMoreStickers.viewTreeObserver.addOnGlobalLayoutListener {
                    addMoreStickers.bringToFront()
                }

                btnAddMoreStickers.blockingClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val isInForeground = ForegroundCheckTask().execute(ctx).get()
                        withContext(Dispatchers.Main) {
                            if (!isInForeground || isHuaweiDevice()) {
                                val intent = Intent(ctx, FOFStartActivity::class.java).apply {
                                    flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("MoveTo", "sindhi_stickers")
                                }
                                ctx?.startActivity(intent)
                            }
                        }
                    }
                }

                addMoreStickers.blockingClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val isInForeground = ForegroundCheckTask().execute(ctx).get()
                        withContext(Dispatchers.Main) {
                            if (!isInForeground || isHuaweiDevice()) {
                                val intent = Intent(ctx, FOFStartActivity::class.java).apply {
                                    flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("MoveTo", "sindhi_stickers")
                                }
                                ctx?.startActivity(intent)
                            }
                        }
                    }
                }

                val displayMetrics = ctx!!.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val itemWidth =
                    ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._70sdp) + ctx.resources.getDimensionPixelSize(
                        com.intuit.sdp.R.dimen._4sdp
                    ) * 2
                val spanCount = screenWidth / itemWidth

                Log.i("fetchDownloadStickers", "stickerViewModel.size :: ${stickerViewModel.size}")
                val stickersRowAdapterFiles = ArrayList<File>()
                val stickersGridAdapterFiles = ArrayList<File>()

                stickerViewModel.forEachIndexed { index, files ->
                    if (files.stickerFiles.size > 1) {
                        stickersRowAdapterFiles.add(files.stickerFiles[1])
                    }
                }
                if (stickerViewModel.isNotEmpty()) {
                    stickersGridAdapterFiles.addAll(stickerViewModel[0].stickerFiles)
                }

                if (stickerViewModel.size < 1) {
                    clNoStickersAdded.visibility = View.VISIBLE
                } else {
                    clNoStickersAdded.visibility = View.GONE
                }

                rvStickersKeypadTrayIcon.layoutManager =
                    LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                rvStickersKeypadGridIcon.layoutManager =
                    GridLayoutManager(ctx, 4)

                val stickersRowLinearAdapter =
                    StickersRowLinearAdapter(ctx, stickersRowAdapterFiles) { position ->
                        Log.i("fetchDownloadStickers1::", "position ::$position")
                        if (position >= 0) {
                            stickersGridAdapterFiles.clear()
                            stickersGridAdapterFiles.addAll(stickerViewModel[position].stickerFiles)
                            rvStickersKeypadGridIcon.adapter?.notifyDataSetChanged()
                        }
                    }
                val stickersRowGridAdapter =
                    StickersRowGridAdapter(ctx, stickersGridAdapterFiles) { file ->
                        Log.i("fetchDownloadStickers2::", "position ::$file")
                        sendStickerTest.invoke(file)
                    }

                rvStickersKeypadTrayIcon.adapter = stickersRowLinearAdapter
                rvStickersKeypadGridIcon.adapter = stickersRowGridAdapter
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

fun isHuaweiDevice(): Boolean {
    return Build.MANUFACTURER.equals("Huawei", ignoreCase = true)
}
