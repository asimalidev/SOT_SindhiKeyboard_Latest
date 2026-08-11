package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.ibadat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.sindhi.urdu.english.keybad.R

@Keep
data class HadithModel(
    @SerializedName("id")
    val id: Int,

    @SerializedName("arabic")
    val arabic: String,

    @SerializedName("urdu")
    val urdu: String,

    @SerializedName("reference")
    val reference: String
)

class HadithCardAdapter(private val hadithList: List<HadithModel>) : RecyclerView.Adapter<HadithCardAdapter.HadithViewHolder>() {
    class HadithViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHadithNumber: TextView = view.findViewById(R.id.tvHadithNumber)
        val tvArabic: TextView = view.findViewById(R.id.tvArabic)
        val tvUrdu: TextView = view.findViewById(R.id.tvUrdu)
        val tvReference: TextView = view.findViewById(R.id.tvReference)

        // Add the ImageView references for your buttons
        val btnCopy: ImageView = view.findViewById(R.id.btnCopy)
        val btnShare: ImageView = view.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HadithViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hadith_card, parent, false)
        return HadithViewHolder(view)
    }

    override fun onBindViewHolder(holder: HadithViewHolder, position: Int) {
        val hadith = hadithList[position]

        // Bind basic text data
        holder.tvHadithNumber.text = "Hadess # ${hadith.id}"
        holder.tvArabic.text = hadith.arabic
        holder.tvUrdu.text = hadith.urdu

        // Format reference for the UI (stacking it with a line break)
        val formattedReference = hadith.reference
        holder.tvReference.text = formattedReference

        // Prepare the formatted text string used for both Copy and Share
        val contentToShare = "${hadith.arabic}\n\n${hadith.urdu}\n\n${hadith.reference}"

        holder.btnCopy.setOnClickListener {
            val context = holder.itemView.context
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Hadith Data", contentToShare)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        holder.btnShare.setOnClickListener {
            val context = holder.itemView.context
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, contentToShare)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Hadith via..."))
        }
    }

    override fun getItemCount() = hadithList.size
}

class HadithPagerAdapter(
    private val categories: List<String>,
    private val dataMap: Map<String, List<HadithModel>>
) : RecyclerView.Adapter<HadithPagerAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hadith_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val categoryName = categories[position]
        val listForCategory = dataMap[categoryName] ?: emptyList()

        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        // Optimization: since the card layout structure doesn't change size dramatically
        holder.recyclerView.setHasFixedSize(true)

        holder.recyclerView.adapter = HadithCardAdapter(listForCategory)
    }

    override fun getItemCount() = categories.size
}