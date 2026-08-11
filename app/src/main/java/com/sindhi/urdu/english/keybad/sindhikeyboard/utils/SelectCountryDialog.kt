package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sindhi.urdu.english.keybad.databinding.SelectCountryDialogBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountryListMe
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.countryrvadapter
import java.util.Locale

class SelectCountryDialog : DialogFragment() {
    private var _binding: SelectCountryDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: countryrvadapter
    private var listener: CountrySelectionListener? = null

    interface CountrySelectionListener {
        fun onCountrySelected(country: CountryModel)
    }

    fun setCountrySelectionListener(listener: CountrySelectionListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = SelectCountryDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = countryrvadapter { country ->
            listener?.onCountrySelected(country)
            dismiss()
        }

        binding.searchViewfromcountry.hint = "Search Language"
        binding.searchViewfromcountry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })
        /*binding.searchViewfromcountry.setOnQueryTextListener(object : SearchView.OnQueryTextListener, androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })*/

        addCountries()

        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<CountryModel> = ArrayList()
        for (item in AllCountryListMe.getCountryList().indices) {
            if (AllCountryListMe.getCountryList()[item].countryName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(AllCountryListMe.getCountryList()[item])
            }
        }
        adapter.getCountryInfo(filteredList)

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No Language Found...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCountries() {
        adapter.getCountryInfo(AllCountryListMe.getCountryList())
        binding.rvcountry.layoutManager = LinearLayoutManager(requireContext())
        binding.rvcountry.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}