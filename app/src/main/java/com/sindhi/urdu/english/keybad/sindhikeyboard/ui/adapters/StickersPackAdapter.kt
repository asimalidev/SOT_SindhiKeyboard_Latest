package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ItemStickersViewBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.RewardedClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.downloadInProgressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerDataCache.progressMap
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPack
import com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.StickerPackData
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.StickersViewActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst

class StickersPackAdapter(
    private var context: Context,
    private var stickerPackData: StickerPackData? = null,
    var onStickerDetailsClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<StickersPackAdapter.StickerViewHolder>() {
    val isPurchased =
        context.getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
            .getBoolean(Preferences.IS_PURCHASED, false)
    private var shimmerAnimator: ObjectAnimator? = null

    inner class StickerViewHolder(private val binding: ItemStickersViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stickerPack: StickerPack, position: Int) {
            binding.txtPackName.text = stickerPack.name

            binding.txtSize.text = "Size: ${stickerPack.stickers_list_size}"
            Glide.with(binding.ivSticker.context).load(stickerPack.thumbnail_image_file)
                .placeholder(R.drawable.ic_no_stickers).into(binding.ivSticker)
            val progress = progressMap[stickerPack.name] ?: ""
            val isDownloaded = progress == "Download Complete"
            val isDownloading = (downloadInProgressMap[stickerPack.name] == true) && !isDownloaded

            if (isPurchased) {

                binding.lottiePremiumIcon.visibility = View.GONE
            }

            if (isDownloaded) {
                binding.txtStickerAdded.isSelected = true
                binding.txtStickerAdded.visibility = View.VISIBLE

                binding.btnAddStickerContainer.visibility = View.GONE

            } else if (isDownloading) {

                binding.btnAddStickerContainer.visibility = View.VISIBLE
                binding.txtStickerAdded.visibility = View.GONE

                startLoadingAnimation()

                binding.txtAddStickerText.text =
                    if (progress.isBlank()) "Downloading..."
                    else progress
            } else {
                stopLoadingAnimation("Add")
                binding.btnAddStickerContainer.visibility = View.VISIBLE

                binding.txtStickerAdded.visibility = View.GONE

                // Lottie vs Static Icon Logic
                // Lottie vs Static Icon Logic
                if (stickerPack.premium.equals("yes", ignoreCase = true) && !isPurchased) {
                    // Show Lottie ONLY if it's a premium pack AND the user hasn't purchased
                    binding.ivAddStickerIcon.visibility = View.GONE
                    binding.lottiePremiumIcon.visibility = View.VISIBLE
                    binding.lottiePremiumIcon.playAnimation()
                } else {
                    // Show standard icon for Free packs AND Premium packs (if the user has purchased them)
                    binding.ivAddStickerIcon.visibility = View.VISIBLE
                    binding.lottiePremiumIcon.visibility = View.GONE

                    // Critical: Stop animation for recycled items
                    binding.lottiePremiumIcon.cancelAnimation()
                    binding.lottiePremiumIcon.progress = 0f
                }

                // Click Listener on the unified Container
                binding.btnAddStickerContainer.setOnClickListener {
                    CustomFirebaseEvents.activitiesFragmentEvent(
                        context as StickersViewActivity,
                        "add_sticker_scr"
                    )

                    if (NetworkCheck.isNetworkAvailable(context as StickersViewActivity)) {
                        if (stickerPack.premium.equals("yes", ignoreCase = true)) {
                            if (!isPurchased) {
                                showPremiumUnlockDialog(stickerPack, position)
                            }
                        } else {
                            triggerDownload(stickerPack, position)
                        }
                    } else {
                        Toast.makeText(
                            context as StickersViewActivity,
                            "No Internet!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            // General click listeners for clicking the item itself
            binding.ivSticker.blockingClickListener {
                onStickerDetailsClick?.invoke(position)
            }

            binding.txtPackName.isSelected = true


            binding.txtPackName.blockingClickListener {
                onStickerDetailsClick?.invoke(position)
            }
        }

        private fun startLoadingAnimation() {

            binding.btnAddStickerContainer.setBackgroundResource(R.drawable.bg_loading)

            binding.txtAddStickerText.text = "Downloading..."

            binding.ivAddStickerIcon.visibility = View.GONE
            binding.lottiePremiumIcon.visibility = View.GONE

            shimmerAnimator?.cancel()

            shimmerAnimator = ObjectAnimator.ofFloat(
                binding.btnAddStickerContainer,
                "translationX",
                -10f,
                10f,
                -10f
            ).apply {

                duration = 500
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }

        private fun stopLoadingAnimation(progress: String) {
            shimmerAnimator?.cancel()
            binding.btnAddStickerContainer.translationX = 0f
            binding.btnAddStickerContainer.setBackgroundResource(R.drawable.ic_add_sticker_bg)
            binding.txtAddStickerText.text = progress
            binding.ivAddStickerIcon.visibility = View.VISIBLE
        }

        private fun triggerDownload(stickerPack: StickerPack, position: Int) {
            downloadInProgressMap[stickerPack.name] = true
            notifyItemChanged(position)
            (context as StickersViewActivity).stickerViewModel.downloadStickerPack(
                stickerPack,
                context
            )
        }

        private fun showPremiumUnlockDialog(stickerPack: StickerPack, position: Int) {
            val dialog = android.app.Dialog(context)
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_premium_unlock) // We will create this XML next
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            val btnClose = dialog.findViewById<ImageView>(R.id.ivCloseDialog)
            val btnWatchAd =
                dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnWatchAd)

            btnClose.setOnClickListener {
                dialog.dismiss()
            }


            btnWatchAd.setOnClickListener {
                RewardedClassAdMob.loadAndShowRewardedAd(
                    context = context,
                    onUserEarnedReward = {
                        downloadInProgressMap[stickerPack.name] = true
                        notifyItemChanged(position)
                        (context as StickersViewActivity).stickerViewModel.downloadStickerPack(
                            stickerPack,
                            context
                        )
                    },
                    onAdDismissed = {
                        // Optional: Handle anything when ad is closed without reward (if needed)
                    }
                )
                dialog.dismiss()

            }

            dialog.show()
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val binding =
            ItemStickersViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val stickerPack = stickerPackData?.sticker_packs?.get(position)
        stickerPack?.let {
            holder.bind(it, position)
        }
    }

    override fun getItemCount(): Int = stickerPackData?.sticker_packs?.size ?: 0

    fun updateData(newStickerPackData: StickerPackData?) {
        stickerPackData = newStickerPackData
        notifyDataSetChanged()
    }

    fun updateProgress(packName: String, progress: String) {
        progressMap[packName] = progress
        val position = stickerPackData?.sticker_packs?.indexOfFirst { it.name == packName } ?: -1
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    fun markDownloadComplete(packName: String) {
        progressMap[packName] = "Download Complete"
        val position = stickerPackData?.sticker_packs?.indexOfFirst { it.name == packName } ?: -1
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}