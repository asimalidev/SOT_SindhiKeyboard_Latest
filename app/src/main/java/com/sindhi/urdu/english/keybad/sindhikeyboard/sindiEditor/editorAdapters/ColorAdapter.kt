package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.ColorItem


class ColorAdapter(
    private val ctx: Context,
    private val colorList: MutableList<ColorItem>,
    private val onItemClickListener: (color: Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition: Int = colorList.indexOfFirst { it.isSelected }

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById(R.id.colorView)
        val selectedBorder: View = itemView.findViewById(R.id.selectedBorder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val item = colorList[position]

        // 1. Set the main color
        holder.colorView.backgroundTintList = ColorStateList.valueOf(item.color)

        // 2. Handle the stroke visibility and color
        if (item.isSelected) {
            holder.selectedBorder.visibility = View.VISIBLE
            // Tint the stroke crisp white so it stands out against the color
            holder.selectedBorder.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        } else {
            holder.selectedBorder.visibility = View.INVISIBLE
        }

        // 3. Handle Clicks
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION || currentPos == selectedPosition) {
                return@setOnClickListener
            }

            // Deselect the previously selected item
            if (selectedPosition != -1 && selectedPosition < colorList.size) {
                colorList[selectedPosition].isSelected = false
                notifyItemChanged(selectedPosition)
            }

            // Select the newly clicked item
            colorList[currentPos].isSelected = true
            selectedPosition = currentPos
            notifyItemChanged(currentPos)

            onItemClickListener(item.color)
        }
    }

    override fun getItemCount(): Int = colorList.size
}