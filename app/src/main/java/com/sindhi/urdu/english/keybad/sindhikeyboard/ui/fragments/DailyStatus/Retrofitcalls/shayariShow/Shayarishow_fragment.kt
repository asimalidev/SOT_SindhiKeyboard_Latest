package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.shayariShow

import android.content.*
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentShayarishowFragmentBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.PURCHASE
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.Util
import com.sindhi.urdu.english.keybad.sindhikeyboard.interfaces.generalstatusitemclicklistner
import com.sindhi.urdu.english.keybad.sindhikeyboard.interfaces.GeneralAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.QuoteX
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.dailystatusResponse
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.FirebaseLog
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_OVER_ALL

class Shayarishow_fragment : Fragment(), generalstatusitemclicklistner {
    private var _binding: FragmentShayarishowFragmentBinding? = null
    private val binding get() = _binding!!
    var isPurchased: Boolean? = null

    val args: Shayarishow_fragmentArgs by navArgs()

    var shayriModelList: List<QuoteX> = ArrayList()
    var adapter: GeneralAdapter? = null
    lateinit var quotesdata:ArrayList<dailystatusResponse>
    lateinit var navController: NavController
    var bundle = Bundle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = FragmentShayarishowFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        bundle.putString("ShayariShowFragment","ShayariShowFragment")
        FirebaseLog.getAnalytics(requireContext()).logEvent("event_shayari_show", bundle)

        navController = findNavController()
        quotesdata= ArrayList()
        isPurchased = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(PURCHASE, false)
        if (Util.dataArray!=null) {
            quotesdata= Util.dataArray
        }
        val id = args.id
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
                    adId =adId!!,
                    fragmentName = "Shayarishow_fragment",
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onFailed = { binding.nativeAdContainerAd.visibility = View.GONE },
                    onAddLoaded = { binding.tvLoadingAdLabelthemes.visibility = View.GONE })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
            }
        }

        Log.d("General status array ", quotesdata.toString())
        binding.qoutelistRecycle.setHasFixedSize(true)
        binding.qoutelistRecycle.layoutManager = LinearLayoutManager(requireContext())
        if (args.shayaritype=="c") {
            for (i in quotesdata.indices) {
                for (j in quotesdata[i].quotes ) {
                    if (j.id==id) {
                        shayriModelList = j.quotes
                        adapter = GeneralAdapter(this)
                        adapter!!.getitemsforgeneralstatus(shayriModelList)

                    }
                }

            }
            binding.qoutelistRecycle.adapter = adapter
        } else if (args.shayaritype=="g") {
            //navController.findDestination(R.id.shayarishow_fragment)?.label="General Poetry"
            for (i in quotesdata.indices) {
                for (j in quotesdata[i].quotes) {
                    if (j.id==7) {
                        shayriModelList = j.quotes
                        adapter = GeneralAdapter(this)
                        adapter!!.getitemsforgeneralstatus(shayriModelList)
                    }
                }
            }
            binding.qoutelistRecycle.adapter = adapter
        } else if (args.shayaritype=="f") {
            // navController.findDestination(R.id.shayarishow_fragment)?.label="Famous Poetry"
            for (i in quotesdata.indices) {
                for (j in quotesdata[i].quotes) {
                    if (j.id==30)
                    {
                        shayriModelList = j.quotes
                        adapter = GeneralAdapter(this)
                        adapter!!.getitemsforgeneralstatus(shayriModelList)
                    }

                }
            }
            binding.qoutelistRecycle.adapter = adapter
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtSindhiKeyboard = requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null
            )

            args.let {
                txtSindhiKeyboard.text = args.nameForHeader
            }

            // ✅ ADD THIS: Convert 12dp to pixels and set the gap
            val gapInDp = 12 // Change this to make the gap bigger or smaller
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx

            val startDrawable = txtSindhiKeyboard.compoundDrawables[0]
            txtSindhiKeyboard.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // ✅ UPDATE THIS: Include the padding in the touch area so it's easier to click
                    val touchAreaWidth = (startDrawable?.bounds?.width() ?: 0) + txtSindhiKeyboard.compoundDrawablePadding

                    if (event.x <= touchAreaWidth) {
                        // ✅ UPDATE THIS: Use the modern, non-deprecated back press dispatcher
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    override fun onshayariitemcopyitemclicked(quoteX: QuoteX) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", quoteX.poetry)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text Copied to ClipBoard", Toast.LENGTH_SHORT).show()
    }

    override fun onshayariitemshareitemclicked(quoteX: QuoteX) {
        val intent2 = Intent()
        intent2.action = Intent.ACTION_SEND
        intent2.type = "text/plain"
        intent2.putExtra(Intent.EXTRA_TEXT, quoteX.poetry)
        requireContext().startActivity(Intent.createChooser(intent2, "Share via"))
    }

    override fun onshayariitemmessengeritemclicked(quoteX: QuoteX) {

        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.type = "text/plain"
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT,
            quoteX.poetry)
        sendIntent.setPackage("com.facebook.orca")
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(Intent.createChooser(sendIntent, "Share text via..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please Install Facebook Messenger", Toast.LENGTH_LONG).show()
        }
    }

    override fun onshayariitemwhatsappitemclicked(quoteX: QuoteX) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT,
            quoteX.poetry)

        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            requireContext().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onshayariitemwitteritemclicked(quoteX: QuoteX) {
        val msg: String = quoteX.poetry
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            requireContext().startActivity(Intent.createChooser(intent, "Share text via..."))
        } catch (e: Exception) {
            Toast.makeText(context,
                "You don't seem to have twitter installed on this device",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onshayariitemedititemclicked(quoteX: QuoteX) {

    }
}