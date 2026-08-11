package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.FontCache

class FontSpinnerAdapter(
    private val context: Context,
    private val fontList: List<String>,
    private val onItemClickListener: (String) -> Unit
) : RecyclerView.Adapter<FontSpinnerAdapter.FontViewHolder>() {

    private var selectedPosition: Int = -1

    inner class FontViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fontNameTextView: AppCompatTextView = itemView.findViewById(R.id.tvFontName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_font, parent, false)
        return FontViewHolder(view)
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        val fontName = fontList[position]
//        holder.fontNameTextView.text = fontName
        holder.fontNameTextView.text = if (fontName.endsWith("_ur.ttf")) {
            "سندھی کے لیے بہترین کی بورڈ!"
        } else {
            "Best Sindhi Keyboard "
        }

        FontCache.getTypeface(context = context, fontName = fontName)?.let {
            holder.fontNameTextView.typeface = it
        }
        holder.itemView.isSelected = selectedPosition == position

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            onItemClickListener(fontName)
        }
    }

    override fun getItemCount(): Int = fontList.size
}
