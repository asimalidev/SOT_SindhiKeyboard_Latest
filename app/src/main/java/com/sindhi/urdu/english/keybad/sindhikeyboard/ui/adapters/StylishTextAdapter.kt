package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.R

class StylishTextAdapter : RecyclerView.Adapter<StylishTextAdapter.StylishViewHolder>() {

    private var items = listOf<String>()

    fun updateList(newList: List<String>) {
        items = newList
        notifyDataSetChanged()
    }

    inner class StylishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStyle: TextView = itemView.findViewById(R.id.tvStyledText)
        val btnCopy: ImageView = itemView.findViewById(R.id.btnCopy)
        val btnShare: ImageView = itemView.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StylishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stylish_text, parent, false)
        return StylishViewHolder(view)
    }

    override fun onBindViewHolder(holder: StylishViewHolder, position: Int) {
        val text = items[position]
        holder.tvStyle.text = text

        holder.btnCopy.setOnClickListener {
            val clipboard = holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Styled Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(holder.itemView.context, "Copied", Toast.LENGTH_SHORT).show()
        }

        holder.btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            holder.itemView.context.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    override fun getItemCount(): Int = items.size
}