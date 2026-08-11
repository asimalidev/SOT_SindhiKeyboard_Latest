package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sindhi.newvoicetyping.ui.Speechtotext.CountryCountry

import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.CountryModel

//class countryrvadapter(val clicklistner:(CountryCountry) -> Unit):RecyclerView.Adapter<countryrvadapter.countiesrvviewholder>() {
//
//    lateinit var context: Context
//    val countrieslist=ArrayList<CountryCountry>()
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): countiesrvviewholder {
//        context=parent.context
//        val view=LayoutInflater.from(parent.context).inflate(R.layout.countriesrvitem,parent,false)
//
//        val countiesrvviewholder = countiesrvviewholder(view)
//
//        return countiesrvviewholder
//    }
//
//    override fun onBindViewHolder(holder: countiesrvviewholder, position: Int) {
//        val currentcountry=countrieslist[position]
//        holder.countryname.text=currentcountry.countryName
//        Glide.with(context).load(currentcountry.arImage).into(holder.countryflag)
//        holder.itemView.setOnClickListener {
//                clicklistner.invoke(currentcountry)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return countrieslist.size
//    }
//
//    fun getcountryinfo(items:ArrayList<CountryCountry>) {
//        countrieslist.clear()
//        countrieslist.addAll(items)
//        countrieslist.sortBy { it.countryName }
//        notifyDataSetChanged()
//    }
//
//    class countiesrvviewholder(itemView: View):RecyclerView.ViewHolder(itemView) {
//        val countryflag:ImageView=itemView.findViewById(R.id.ivcountryflag)
//        val countryname:TextView=itemView.findViewById(R.id.tvcountryname)
//
//    }
//}

class countryrvadapter(val clickListener: (CountryModel) -> Unit) :
    RecyclerView.Adapter<countryrvadapter.CountiesViewHolder>() {

    lateinit var context: Context
    private val countriesList = ArrayList<CountryModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountiesViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.country_item, parent, false)
        return CountiesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountiesViewHolder, position: Int) {
        val currentCountry = countriesList[position]
        holder.countryName.text = currentCountry.countryName
        Glide.with(context).load(currentCountry.arImage).into(holder.countryFlag)
        holder.itemView.setOnClickListener {
            clickListener.invoke(currentCountry)
        }
    }

    override fun getItemCount(): Int {
        return countriesList.size
    }

    fun getCountryInfo(items: ArrayList<CountryModel>) {
        countriesList.clear()
        countriesList.addAll(items)
        countriesList.sortBy { it.countryName }
        notifyDataSetChanged()
    }

    class CountiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryFlag: ImageView = itemView.findViewById(R.id.ivCountryFlag)
        val countryName: TextView = itemView.findViewById(R.id.tvCountryName)

    }
}