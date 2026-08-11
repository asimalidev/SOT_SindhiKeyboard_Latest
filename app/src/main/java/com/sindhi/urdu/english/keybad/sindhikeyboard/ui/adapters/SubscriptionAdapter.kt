package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.itemDs


class SubscriptionAdapter(private val clickListener: (itemDs) -> Unit) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {
    private lateinit var context: Context
    private val subscriptionsList = ArrayList<itemDs>()
    private lateinit var prefHelper: PrefHelper

    class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvRoot: MaterialCardView = itemView.findViewById(R.id.cvRoot) // Cast to MaterialCardView
        val tvSaveTag: TextView = itemView.findViewById(R.id.tvSaveTag)
        val tvPlanName: TextView = itemView.findViewById(R.id.tvPlanName)
        val tvPlanPrice: TextView = itemView.findViewById(R.id.tvPlanPrice)
        val tvBillingCycle: TextView = itemView.findViewById(R.id.tvBillingCycle)
        val tvSubDetails: TextView = itemView.findViewById(R.id.tvSubDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        context = parent.context
        prefHelper = PrefHelper(context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemlst, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val currentItem = subscriptionsList[position]

        val selectedPos = prefHelper.getStringDefault("SubscriberPosition", "0")
        val isSelected = selectedPos == position.toString()
        val priceText = currentItem.formattedPrice ?: ""
        var isWeekly = false

        // Determine Plan Name & Tag logic based on current string data
        if (priceText.contains("Yearly", ignoreCase = true) || priceText.contains("Lifetime", ignoreCase = true)) {
            holder.tvPlanName.text = "Yearly"
            holder.tvSaveTag.visibility = View.VISIBLE
            holder.tvSaveTag.text = "Save 75%"
        } else if (priceText.contains("Month", ignoreCase = true)) {
            holder.tvPlanName.text = "Monthly"
            holder.tvSaveTag.visibility = View.VISIBLE
            holder.tvSaveTag.text = "Save 40%"
        } else {
            holder.tvPlanName.text = "Weekly"
            // Use INVISIBLE instead of GONE so the layout height doesn't shift
            holder.tvSaveTag.visibility = View.INVISIBLE
            isWeekly = true
        }
        val layoutParams = holder.cvRoot.layoutParams as ViewGroup.MarginLayoutParams

        if (isSelected) {
            // Selected: Pink Background, Red Stroke, Red Price Text, Larger
            holder.cvRoot.setCardBackgroundColor(Color.parseColor("#FFD9DB")) // Pink
            holder.cvRoot.strokeColor = Color.parseColor("#D32F2F") // Red
            holder.cvRoot.strokeWidth = 2.dpToPx(context)
            holder.cvRoot.cardElevation = 4f

            holder.tvPlanPrice.setTextColor(Color.parseColor("#D32F2F"))

            // Make margins smaller to make card appear larger
            layoutParams.topMargin = 2.dpToPx(context)
            layoutParams.bottomMargin = 2.dpToPx(context)
        } else {
            // Unselected: White Background, Grey Stroke, Black Price Text, Smaller
            holder.cvRoot.setCardBackgroundColor(Color.WHITE)
            holder.cvRoot.strokeColor = Color.parseColor("#E0E0E0") // Light Grey
            holder.cvRoot.strokeWidth = 1.dpToPx(context)
            holder.cvRoot.cardElevation = 2f

            holder.tvPlanPrice.setTextColor(Color.BLACK)

            // Make margins larger to make card appear slightly smaller
            layoutParams.topMargin = 10.dpToPx(context)
            layoutParams.bottomMargin = 10.dpToPx(context)
        }

        // Apply updated margins
        holder.cvRoot.layoutParams = layoutParams

        // Clean up price (assuming you just want "Rs 500")
        holder.tvPlanPrice.text = priceText.replace(Regex("/.*"), "").trim()

        val lines = currentItem.subsName.split("\n").filter {
            !it.contains("Auto Renewal", ignoreCase = true)
        }

        if (lines.isNotEmpty()) {
            holder.tvBillingCycle.text = lines[0]
        } else {
            holder.tvBillingCycle.text = ""
        }

        holder.tvSubDetails.visibility = View.VISIBLE

        if (isWeekly) {
            holder.tvSubDetails.text = "Starter Plan"
        } else {
            if (lines.size > 1) {
                val details = lines.subList(1, lines.size).joinToString("\n")
                holder.tvSubDetails.text = details
            } else {
                holder.tvSubDetails.text = " "
            }
        }

        // Click Listener logic
        holder.itemView.setOnClickListener {
            val oldPos = prefHelper.getStringDefault("SubscriberPosition", "0")?.toInt() ?: 0
            prefHelper.putString("SubscriberPosition", position.toString())

            if (oldPos != position) {
                notifyItemChanged(oldPos)
            }
            notifyItemChanged(position)

            clickListener.invoke(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return subscriptionsList.size
    }

    fun setSubscriptionList(items: ArrayList<itemDs>) {
        subscriptionsList.clear()
        subscriptionsList.addAll(items)
        notifyDataSetChanged()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}