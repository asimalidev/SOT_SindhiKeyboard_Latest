package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.ibadat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.sindhi.urdu.english.keybad.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sindhi.urdu.english.keybad.databinding.FragmentIbadatBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import java.io.InputStreamReader

class IbadatFragment : Fragment() {
    private lateinit var binding: FragmentIbadatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIbadatBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBackPress.setOnClickListener {
            val action = IbadatFragmentDirections.actionNavIbadatToHome()
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = IbadatFragmentDirections.actionNavIbadatToHome()
                    findNavController().navigate(action)
                }
            })
        setupDataAndViewPager()
    }

    override fun onResume() {
        super.onResume()
        (requireContext() as NavigationActivity).hidetoolbar()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as NavigationActivity).showtoolbar()
    }

    private fun setupDataAndViewPager() {
        val inputStream = requireContext().resources.openRawResource(R.raw.sindhi_hadith_data)
        val reader = InputStreamReader(inputStream)
        val mapType = object : TypeToken<Map<String, List<HadithModel>>>() {}.type
        val hadithDataMap: Map<String, List<HadithModel>> = Gson().fromJson(reader, mapType)
        val categories = hadithDataMap.keys.toList()
        val pagerAdapter = HadithPagerAdapter(categories, hadithDataMap)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }
}