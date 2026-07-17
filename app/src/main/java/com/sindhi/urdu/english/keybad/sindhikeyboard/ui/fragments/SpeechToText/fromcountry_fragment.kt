package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.sindhi.newvoicetyping.ui.Speechtotext.CountryCountry
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentFromcountryFragmentBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.TinyDB
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_CONVERSATION
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import java.util.Locale

class fromcountry_fragment : Fragment() {
    private var _binding: FragmentFromcountryFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var navController: NavController
    var isPurchased:Boolean ?= null
    lateinit var adapter:countryrvadapter
    val args:fromcountry_fragmentArgs by navArgs()
    var bundle = Bundle()

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFromcountryFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        isNavControllerAdded()
        bundle.putString("LanguageChangeFragment","LanguageChangeFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_language_change", bundle)

        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true
        val mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = mSharedPreferences.edit()
        adapter = countryrvadapter {
            TinyDB.getInstance(requireContext())!!.putString("sourceFromCode", it.countryName)
            TinyDB.getInstance(requireContext())!!.getInt("sourceFromCodeFlag", it.arImage)
            if (args.fragmentname == "stt") {
                editor.putString("fromLangSTT", it.countryName)
                editor.putInt("fromFlagSTT", it.arImage)
                editor.apply()
                val action = fromcountry_fragmentDirections.actionFromcountryFragmentToSpeechFragment(it.countryName, it.arImage, args.tocountryname, args.tocountryflag)
                navController.navigate(action)
            } else if (args.fragmentname == "convo") {
                editor.putString("fromlangconvo", it.countryName)
                editor.putInt("fromflagconvo", it.arImage)
                editor.apply()
                val action = fromcountry_fragmentDirections.actionFromcountryFragmentToConversationFragment(it.countryName, it.arImage, args.tocountryname, args.tocountryflag)
                navController.navigate(action)
            }
        }

        addcountries()
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (args.fragmentname=="stt") {
                        val action=fromcountry_fragmentDirections.actionFromcountryFragmentToSpeechFragment(args.fromcountryname,
                            args.fromcountryflag,
                            args.tocountryname,
                            args.tocountryflag)
                        navController.navigate(action)
                    } else if (args.fragmentname=="convo") {
                        val action=fromcountry_fragmentDirections.actionFromcountryFragmentToConversationFragment(args.fromcountryname,
                            args.fromcountryflag,
                            args.tocountryname,
                            args.tocountryflag)
                        navController.navigate(action)
                    }
                }
            })

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            val pref =requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
            val adId  =if (!BuildConfig.DEBUG){
                pref.getString(NATIVE_CONVERSATION, resources.getString(R.string.admob_banner_inside))
            }
            else{
                resources.getString(R.string.admob_banner_inside)
            }

            if (NetworkCheck.isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                    Preferences.ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT,"ON").equals("ON",true)) {
                    NewNativeAdClass.checkAdRequestAdmob(
                        mContext = requireActivity(),
                        adId = adId!!,
                        fragmentName = "SelectLanguageFragment",
                        isMedia = false,
                        adContainer = binding.nativeAdContainerAd,
                        isMediumAd = false,
                        onFailed = {
                            binding.nativeAdContainerAd.visibility = View.GONE
                            binding.shimmerLayout.stopShimmer()
                            binding.shimmerLayout.visibility = View.GONE
                                   },
                        onAddLoaded = {
                            binding.shimmerLayout.stopShimmer()
                            binding.shimmerLayout.visibility = View.GONE
                        })
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }

        binding.searchViewfromcountry.queryHint = "Search Language";
        binding.searchViewfromcountry.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })

        return root
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonConversation).let { it?.visibility = View.INVISIBLE }
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonTTS).let { it?.visibility = View.INVISIBLE }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }
        requireActivity().findViewById<SwitchCompat>(R.id.switchButtonConversation).let { it?.visibility = View.INVISIBLE }

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.back),null,null,null)
            txtSindhiKeyboard.text = resources.getString(R.string.select_lang)
            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx

            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        if (args.fragmentname=="stt") {
                            val action=fromcountry_fragmentDirections.actionFromcountryFragmentToSpeechFragment(args.fromcountryname,
                                args.fromcountryflag,
                                args.tocountryname,
                                args.tocountryflag)
                            navController.navigate(action)
                        } else if (args.fragmentname=="convo") {
                            val action=fromcountry_fragmentDirections.actionFromcountryFragmentToConversationFragment(args.fromcountryname,
                                args.fromcountryflag,
                                args.tocountryname,
                                args.tocountryflag)
                            navController.navigate(action)
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun filter(text: String) {
        val filteredlist: java.util.ArrayList<CountryCountry> = ArrayList<CountryCountry>()
        for (item in  AllCountryListMe.getCountryList().indices) {
            if (AllCountryListMe.getCountryList()[item].countryName.lowercase(Locale.getDefault()).contains(
                    text.lowercase(Locale.getDefault())
                )) {
                filteredlist.add(AllCountryListMe.getCountryList()[item])
            }
        }
        adapter.getcountryinfo(filteredlist)

        if (filteredlist.isEmpty()) {
            Toast.makeText(requireContext(), "No Language Found...", Toast.LENGTH_SHORT).show()
        }

    }
    fun addcountries() {
        adapter.getcountryinfo(AllCountryListMe.getCountryList())
        binding.rvcountry.layoutManager= LinearLayoutManager(requireContext())
        binding.rvcountry.adapter=adapter
    }
}