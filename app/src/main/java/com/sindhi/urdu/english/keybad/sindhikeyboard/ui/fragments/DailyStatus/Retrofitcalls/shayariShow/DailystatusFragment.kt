package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.shayariShow

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.myproject.todoappwithnodejs.retrofitgenericresponse.Baseresponse
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentDailystatusBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.interfaces.MainAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.retrofitinit.retrofitinstance
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.Util
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.interfaces.shayaricategoryclicklistner
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.ViewmodelFactory.DictionaryViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.viewmodel.Dictionarydao
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.viewmodel.dictionaryrepository
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.ViewmodelFactory.staffviewmodelsfactory
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.Cat_Update
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.dailystatusResponse
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import okhttp3.OkHttpClient
import okhttp3.Response

class DailystatusFragment : Fragment(), shayaricategoryclicklistner {

    private lateinit var binding: FragmentDailystatusBinding
    var isPurchased: Boolean? = null
    var navController: NavController? = null
    lateinit var dictionaryViewModel: DictionaryViewModel
    lateinit var catlist: ArrayList<Cat_Update>
    lateinit var dailystatuslist:ArrayList<dailystatusResponse>
    lateinit var adapter: MainAdapter
    var bundle = Bundle()
    private var isPurchase = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDailystatusBinding.inflate(inflater, container, false)
        val root: View = binding.root

        isNavControllerAdded()

        bundle.putString("DailyStatusFragment","DailyStatusFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_daily_status", bundle)
        
        val BASE_URL = "https://solutionoftechnologies.com/daily_urdu_qoutes/"
        val cacheSize = (10 * 1024 * 1024).toLong()
        val dailystatusdao = retrofitinstance(cacheSize,requireContext(), BASE_URL = BASE_URL).getinstance().create(Dictionarydao::class.java)
        isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)?.getBoolean(IS_PURCHASED, false) == true
        val dictionaryrepository = dictionaryrepository(dailystatusdao)
        dictionaryViewModel= ViewModelProvider(this,
            staffviewmodelsfactory(requireActivity().application,requireActivity(),navController,dictionaryrepository)
        )[DictionaryViewModel::class.java]
        catlist = ArrayList()
        dailystatuslist= ArrayList()
        adapter= MainAdapter(this)

        getdailystatus()

        binding.mainRecycler.setHasFixedSize(true)
        binding.mainRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.onGeneralstatus.blockingClickListener {
            val action=DailystatusFragmentDirections.actionDailystatusFragmentToShayarishowFragment(7,"g","جنرل سٹیٹس")
            navController?.navigate(action)
        }
        binding.onFamousStatus.blockingClickListener {
            val action=DailystatusFragmentDirections.actionDailystatusFragmentToShayarishowFragment(30,"f", "مشہور کہاوتین")
            navController?.navigate(action)
        }
        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder.addInterceptor { chain ->
            val response: Response = chain.proceed(chain.request())
            if (response.networkResponse != null) {
                Log.v("TAG", "Response from networkResponse(): " + response.networkResponse)
            } else if (response.cacheResponse != null) {
                Log.v("TAG", "Response from cacheControl(): " + response.cacheResponse)
            }
            response
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = DailystatusFragmentDirections.actionDailyStatusFragmentToNavHome()
                    navController?.navigate(action)
                }
            })
        return root
    }

    fun getdailystatus() {
        dictionaryViewModel.getdailystatus()
        dictionaryViewModel.dailystatuslivedata.observe(viewLifecycleOwner) {
            if (it.data!=null) {
                binding.animation.visibility = View.GONE
            }
            when(it) {
                is Baseresponse.Error -> {
                    Log.d("Error",it.errormessage.toString())
                }
                is Baseresponse.Loading -> {

                }
                is Baseresponse.Success -> {
                    Log.d("Response",it.data?.cat.toString())
                    if (it.data!=null) {
                        dailystatuslist.add(it.data)
                        Util.dataArray = dailystatuslist

                        it.data.cat_update.forEach{
                            catlist.add(it)
                        }

                        Log.d("Cat List",catlist.toString())
                        adapter.getitems(catlist)
                    }

                    binding.mainRecycler.adapter = adapter
                    catlist.clear()

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }


        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        txtSindhiKeyboard?.apply {
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null
            )
            text = resources.getString(R.string.label_daily_status)

            // ✅ ADD THIS: Set the gap between the drawable and the text
            // Convert your desired dp value (e.g., 12dp) to pixels
            val gapInDp = 12
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            compoundDrawablePadding = gapInPx

            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (event.x <= (startDrawable?.bounds?.width() ?: 0)) {
                        requireActivity().onBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
        val pref =requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(NATIVE_OVER_ALL,resources.getString(R.string.admob_native))
        }
        else{
            resources.getString(R.string.admob_native)
        }

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (NetworkCheck.isNetworkAvailable(requireContext())) {
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "DailyStatusFragment",
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onFailed = { binding.nativeAdContainerAd.visibility = View.GONE },
                    onAddLoaded = { binding.tvLoadingAdLabelthemes.visibility = View.GONE })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }
    }

    override fun onshayaricategoryitemclicked(cat: Cat_Update) {
        Log.d("Response", cat.toString())
        if (NetworkCheck.isNetworkAvailable(requireContext())) {
            if (isPurchased!!) {
                val action = DailystatusFragmentDirections.actionDailystatusFragmentToShayarishowFragment(cat.id,"c", cat.name)
                navController?.navigate(action)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    val action = DailystatusFragmentDirections.actionDailystatusFragmentToShayarishowFragment(cat.id,"c", cat.name)
                    navController?.navigate(action)
                }, 1200)

                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(requireActivity(),"DailyStatusFragment", onAdClosedCallBackAdmob = {}, onAdShowedCallBackAdmob = {})
            }
        } else {
            val action = DailystatusFragmentDirections.actionDailystatusFragmentToShayarishowFragment(cat.id,"c", cat.name)
            navController?.navigate(action)
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}