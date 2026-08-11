package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.databinding.ItemPosterGalleryBinding

class PostersAdapter(
    private val onItemClick: (Uri) -> Unit
) : RecyclerView.Adapter<PostersAdapter.PosterViewHolder>() {

    private val posterUris = mutableListOf<Uri>()

    fun submitList(newList: List<Uri>) {
        posterUris.clear()
        posterUris.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val binding = ItemPosterGalleryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PosterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        holder.bind(posterUris[position])
    }

    override fun getItemCount(): Int = posterUris.size

    inner class PosterViewHolder(private val binding: ItemPosterGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            // Using standard setImageURI. 
            // Tip: If loading takes time, replace this line with Glide or Picasso: Glide.with(itemView).load(uri).into(binding.ivThumbnail)
            binding.ivThumbnail.setImageURI(uri)

            binding.root.setOnClickListener {
                onItemClick(uri)
            }
        }
    }
}