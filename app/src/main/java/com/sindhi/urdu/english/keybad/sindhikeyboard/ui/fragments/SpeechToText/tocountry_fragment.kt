package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sindhi.newvoicetyping.ui.Speechtotext.CountryCountry
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentTocountryFragmentBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.TinyDB
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import java.util.Locale

class tocountry_fragment : Fragment() {
    private var _binding: FragmentTocountryFragmentBinding? = null
    private val binding get() = _binding!!
    var navController: NavController? = null
    var isPurchased: Boolean? = null

    lateinit var adapter: countryrvadapter
    val args: tocountry_fragmentArgs by navArgs()
    var bundle = Bundle()

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTocountryFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        isPurchased = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(PURCHASE,false)
        isNavControllerAdded()
        bundle.putString("LanguageChangeFragment","LanguageChangeFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_language_change", bundle)
        val mSharedPreferences = android.preference.PreferenceManager
            .getDefaultSharedPreferences(requireContext())
        val editor = mSharedPreferences.edit()
        adapter = countryrvadapter {
            if (navController != null) {
                TinyDB.getInstance(requireContext())!!.getString("targetFromCode",it.countryName)
                TinyDB.getInstance(requireContext())!!.getInt("targetFromCodeFlag",it.arImage)
                if (args.fragmentname=="stt") {
                    editor.putString("toLangSTT", it.countryName )
                    editor.putInt("toFlagSTT",it.arImage)
                    editor.apply()
                    val action = tocountry_fragmentDirections.actionTocountryFragmentToSpeechFragment(args.fromcountryname,
                        args.fromcountryflag,
                        it.countryName,
                        it.arImage)
                    navController?.navigate(action)
                } else if(args.fragmentname=="convo") {
                    editor.putString("tolangconvo", it.countryName )
                    editor.putInt("toflagconvo",it.arImage)
                    editor.apply()
                    val action = tocountry_fragmentDirections.actionTocountryFragmentToConversationFragment(args.fromcountryname,
                        args.fromcountryflag,
                        it.countryName,
                        it.arImage)
                    navController?.navigate(action)
                }
            } else {
                isNavControllerAdded()
            }
        }
        addcountries()

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navController != null) {
                        if (args.fragmentname=="stt") {
                            val action = tocountry_fragmentDirections.actionTocountryFragmentToSpeechFragment(args.fromcountryname,
                                args.fromcountryflag,
                                args.tocountryname,
                                args.tocountryflag)
                            navController?.navigate(action)
                        } else if (args.fragmentname=="convo") {
                            val action = tocountry_fragmentDirections.actionTocountryFragmentToConversationFragment(args.fromcountryname,
                                args.fromcountryflag,
                                args.tocountryname,
                                args.tocountryflag)
                            navController?.navigate(action)
                        }
                    } else {
                        isNavControllerAdded()
                    }
                }
            })

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext()) &&
                requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
                    ?.getBoolean(IS_PURCHASED, false) == true
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE).getString(
                    Preferences.ADS_NATIVE_SELECT_LANGUAGE_SPEECH_TO_TEXT,"ON").equals("ON",true)) {
                val pref =requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId  =if (!BuildConfig.DEBUG){
                    pref.getString(NATIVE_OVER_ALL,"ca-app-pub-3747520410546258/1702944653")
                }
                else{
                    resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
                }
                    NewNativeAdClass.checkAdRequestAdmob(
                        mContext = requireActivity(),
                        adId = adId!!,//getString(R.string.admob_native),
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

        binding.searchViewtocountry.queryHint = "Search Language"
        binding.searchViewtocountry.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
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

    @SuppressLint("ClickableViewAccessibility")
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
                        if (navController != null) {
                            if (args.fragmentname=="stt") {
                                val action = tocountry_fragmentDirections.actionTocountryFragmentToSpeechFragment(args.fromcountryname,
                                    args.fromcountryflag,
                                    args.tocountryname,
                                    args.tocountryflag)
                                navController?.navigate(action)
                            } else if (args.fragmentname=="convo") {
                                val action = tocountry_fragmentDirections.actionTocountryFragmentToConversationFragment(args.fromcountryname,
                                    args.fromcountryflag,
                                    args.tocountryname,
                                    args.tocountryflag)
                                navController?.navigate(action)
                            }
                            return@setOnTouchListener true
                        } else {
                            isNavControllerAdded()
                        }
                    }
                }
                false
            }
        }
    }

    private fun filter(text: String) {
        val filteredList: java.util.ArrayList<CountryCountry> = ArrayList()

        for (item in AllCountryListMe.getCountryList().indices) {
            Log.d("Something", "Something")
            if (AllCountryListMe.getCountryList()[item].countryName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(AllCountryListMe.getCountryList()[item])
            }
        }
        adapter.getcountryinfo(filteredList)

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No Language Found...", Toast.LENGTH_SHORT).show()
        }
    }

    fun addcountries() {
        adapter.getcountryinfo(AllCountryListMe.getCountryList())
        binding.rvtocountry.layoutManager = LinearLayoutManager(requireContext())
        binding.rvtocountry.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}