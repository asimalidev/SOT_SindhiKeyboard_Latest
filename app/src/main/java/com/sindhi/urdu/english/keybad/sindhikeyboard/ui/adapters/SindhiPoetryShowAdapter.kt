package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetryItemClickListener


class SindhiPoetryShowAdapter(
    private val poetryList: List<String>,
    private val sindhiPoetryItemClickListener: SindhiPoetryItemClickListener
) : RecyclerView.Adapter<SindhiPoetryShowAdapter.SindhiPoetryShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SindhiPoetryShowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sindhi_poetry, parent, false)
        return SindhiPoetryShowViewHolder(view)
    }

    override fun onBindViewHolder(holder: SindhiPoetryShowViewHolder, position: Int) {
        val poetry = poetryList[position]
        holder.txtQuotes.text = poetry

        // 2. FIXED the Edit button click listener
        holder.btnEditPost.setOnClickListener {
            sindhiPoetryItemClickListener.onPoetryItemEditClicked(poetry)
        }

        holder.copy.setOnClickListener {
            sindhiPoetryItemClickListener.onPoetryItemCopyClicked(poetry)
        }

        holder.share.setOnClickListener {
            sindhiPoetryItemClickListener.onPoetryItemShareClicked(poetry)
        }

        holder.whatsapp.setOnClickListener {
            sindhiPoetryItemClickListener.onPoetryItemWhatsappClicked(poetry)
        }
    }

    override fun getItemCount(): Int {
        return poetryList.size
    }

    inner class SindhiPoetryShowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Note: If you changed btnEditPost to a TextView in your XML (as per the previous UI design),
        // change AppCompatButton to TextView here.
        val btnEditPost: AppCompatButton = itemView.findViewById(R.id.btnEditPost)
        val txtQuotes: TextView = itemView.findViewById(R.id.txtQuotes)
        val copy: ImageView = itemView.findViewById(R.id.btnCopy)
        val share: ImageView = itemView.findViewById(R.id.btnShare)
        val whatsapp: ImageView = itemView.findViewById(R.id.btnWhatsapp)
    }
}