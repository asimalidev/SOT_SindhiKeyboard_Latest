package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.dictionary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.Baseresponse
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentDictionaryBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.Networkutils
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideKeyboard
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.ViewmodelFactory.DictionaryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.viewmodel.Dictionarydao
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.viewmodel.dictionaryrepository
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.ViewmodelFactory.staffviewmodelsfactory
import java.util.*

class Dictionary : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    lateinit var fromlanguacode: String
    lateinit var tolanguacode: String
    private var tts: TextToSpeech? = null
    lateinit var dictionaryViewModel: DictionaryViewModel
    var isPurchased:Boolean ?= null
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navController = findNavController()
        val BASE_URL="https://api.dictionaryapi.dev"

        val wordsdao = retrofitHelper.getInstance(BASE_URL).create(
            Dictionarydao::class.java
        )
        val dictionaryrepository = dictionaryrepository(wordsdao)
        dictionaryViewModel= ViewModelProvider(this,
            staffviewmodelsfactory(requireActivity().application,requireActivity(),navController,dictionaryrepository)
        )[DictionaryViewModel::class.java]
        isPurchased = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(PURCHASE, false)

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility=View.GONE
        }
        NewNativeAdClass.checkAdRequestAdmob(
            mContext = requireActivity(),
            fragmentName = "",
            adId = getString(R.string.admob_native),
            isMedia = false,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = false,
            onFailed = { binding.nativeAdContainerAd.visibility = View.GONE },
            onAddLoaded = { binding.tvLoadingAdLabeldictionary.visibility = View.GONE })

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = DictionaryDirections.actionDictionaryFragmentToTranslationFragment()
                    navController.navigate(action)
                }
            })

        tts = TextToSpeech(requireContext(), this,"com.google.android.tts")
        binding.animationView.visibility = View.GONE
        binding.cardview.visibility = View.GONE
        binding.etword.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (Networkutils.hasNetwork(requireContext())) {
                    getwordsdetails(query)
                } else {
                    Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        binding.btnsearchword.blockingClickListener {
            val word = binding.etword.query.toString()
            getwordsdetails(word)

        }

        binding.btncancel.blockingClickListener {
            binding.etword.setQuery("", false)
            val word: String = binding.etword.query.toString()
            if (word == "") {
                binding.cardview.visibility = View.GONE
            }
        }

        binding.linear1.blockingClickListener {
            tts!!.stop()
        }

        binding.btnshare.blockingClickListener {
            if (binding.tvSentence1.text.isEmpty()) {
                Toast.makeText(requireContext(), "Nothing to share", Toast.LENGTH_SHORT).show()
            } else {
                val s = binding.tvSentence1.text.toString().trim() + binding.tvSentence2.text.toString().trim()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }

        binding.btncancel.blockingClickListener {
            binding.etword.setQuery("", false)
            val word: String = binding.etword.query.toString()
            if (word == "") {
                binding.cardview.visibility = View.GONE
            }
        }
        
        binding.btncopttocb.blockingClickListener {
            val text = binding.etword.query.toString()
            copytexttoclipboard(text)
        }
        
        binding.btnspeaker.blockingClickListener {
            Log.d("Clcikd", "clicked")
            speakOut()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localCode = "ur_PK"
            val locale = Locale(localCode)
            val result = tts!!.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            }
        }
    }

    fun copytexttoclipboard(text: String) {
        val clipboard: ClipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Text Copied", Toast.LENGTH_SHORT).show()
    }

    fun getwordsdetails(word:String) {
        dictionaryViewModel.getwordsdetails(word)
        requireActivity().hideKeyboard()
        dictionaryViewModel.wordslivedata.observe(requireActivity()){
            when(it) {
                is Baseresponse.Error -> {
                    Log.d("Dictionaryerr",it.errormessage.toString())
                }

                is Baseresponse.Loading -> {
                    Log.d("DictionaryLoading","Loading")
                }

                is Baseresponse.Success -> {
                    binding.cardview.visibility = View.VISIBLE
                    binding.linear1.visibility = View.VISIBLE
                    binding.linear2.visibility = View.GONE
                    binding.linear2.visibility = View.VISIBLE
                    Log.d("Dictionaryres",it.data.toString())
                    if (it.data!=null) {
                        try {
                            val data= it.data[0]
                            binding.tvWord.text=data.word
                            val samplesentence1= data.meanings[0].definitions[0].definition
                            binding.tvSentence1.text=samplesentence1
                            val samplesentence2=data.meanings[1].definitions[0].definition
                            binding.tvSentence2.text=samplesentence2
                            val webdef1=data.meanings[2].definitions[0].definition
                            binding.linear3.visibility=View.VISIBLE
                            binding.tvWebDef1.text=webdef1
                        } catch (e:Exception) {
                            Log.d("Error",e.message.toString())
                            binding.linear3.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun speakOut() {
        val text = binding.etword.query.trim().toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun speakOutSampleDef() {
        val text =
            binding.tvSentence1.text.trim().toString() + binding.tvSentence2.text.trim().toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun speakOutWevDef() {
        val text =
            binding.tvWebDef1.text.trim().toString() + binding.tvWebDef2.text.trim().toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}