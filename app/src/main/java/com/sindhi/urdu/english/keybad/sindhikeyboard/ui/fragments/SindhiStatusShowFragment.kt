package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentSindhiStatusShowBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.SindhiPoetryShowAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetryItemClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_BANNER_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_SINDHI_POETRY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_BANNER_SINDHI_POETRY
import org.json.JSONArray
import java.io.InputStream

class SindhiStatusShowFragment : Fragment(), SindhiPoetryItemClickListener {
    lateinit var binding: FragmentSindhiStatusShowBinding
    var isPurchased: Boolean? = null
    var navController: NavController? = null
    private val args: SindhiStatusShowFragmentArgs by navArgs()
    var adapter: SindhiPoetryShowAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSindhiStatusShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPurchased = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navController = findNavController()
                    val currentDestId = navController.currentDestination?.id
                    if (currentDestId == R.id.navSindhiStatusShow) {
                        navController.navigate(SindhiStatusShowFragmentDirections.actionNavSindhiStatusShowToNavSindhiStatus())
                    } else {
                        navController.popBackStack()
                    }
                }
            })

        checkForLoadBanner()

        binding.rvPoetryShow.layoutManager = LinearLayoutManager(requireActivity())

        // 1. Construct the filename (e.g., "استادبخاري.json")
        val jsonFileName = "${args.poetryName}.json"

        // 2. Read and parse the list from assets
        val poetryList = loadPoetryFromAssets(jsonFileName)

        // 3. Bind the list to the adapter
        adapter = SindhiPoetryShowAdapter(poetryList, this)
        binding.rvPoetryShow.adapter = adapter
    }


    private fun loadPoetryFromAssets(fileName: String): List<String> {
        val poetryList = ArrayList<String>()
        try {
            // Open the JSON file from the assets folder
            val inputStream: InputStream = requireContext().assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            // Convert buffer to string
            val jsonString = String(buffer, Charsets.UTF_8)

            // Parse JSON array
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                poetryList.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SindhiStatusShow", "Error reading JSON file: $fileName")
            // Optionally show a toast to the user if the file isn't found
        }
        return poetryList
    }

    private fun checkForLoadBanner() {
        val isPurchase = requireContext().getSharedPreferences(REMOTE_CONFIG, MODE_PRIVATE)
            ?.getBoolean(IS_PURCHASED, false) == true
        if (!isPurchase && isNetworkAvailable(requireContext()) && requireActivity().getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                SHOW_BANNER_SINDHI_POETRY, "ON"
            ).equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("showFragment")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["showFragment"]
                Handler().postDelayed({
                    binding.shimmerLayoutBanner.stopShimmer()
                    binding.shimmerLayoutBanner.visibility = View.GONE
                    binding.adViewContainer.removeView(binding.shimmerLayoutBanner)

                    val parent = collapsibleAdView?.parent as? ViewGroup
                    parent?.removeView(collapsibleAdView)

                    binding.adViewContainer.addView(collapsibleAdView)
                }, 500)
            } else {
                loadBanner()
            }
        } else {
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
        }
    }

    private val adSize: AdSize
        get() {
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                adWidth
            )
        }

    private fun loadBanner() {
        Log.d("jdjasjjsa", "loading: ")
        val pref = requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId = if (!BuildConfig.DEBUG) {
            pref.getString(AD_ID_BANNER_SINDHI_POETRY, resources.getString(R.string.BANNER_DEFAULT))
        } else {
            resources.getString(R.string.BANNER_DEFAULT)
        }
        val adView = AdView(requireActivity())
        adView.setAdSize(adSize)
        adView.adUnitId = adId!!
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                NativeMaster.collapsibleBannerAdMobHashMap!!["showFragment"] = adView
                Log.d("jdjasjjsa", "onAdLoaded: ")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
            }

            override fun onAdOpened() {
                Log.d("jdjasjjsa", "onAdOpened: ")
            }

            override fun onAdClicked() {
            }

            override fun onAdClosed() {
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivClose)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

        val txtSindhiKeyboard =
            requireActivity().findViewById<AppCompatTextView>(R.id.txtSindhiKeyboard)
        if (txtSindhiKeyboard != null) {
            txtSindhiKeyboard.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.back
                ), null, null, null
            )
            txtSindhiKeyboard.text = args.poetryName
            val gapInDp = 12
            val gapInPx = (gapInDp * resources.displayMetrics.density).toInt()
            txtSindhiKeyboard.compoundDrawablePadding = gapInPx
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

        if (isPurchased!!) {
            binding.nativeAdContainerAd.visibility = View.GONE
        } else {
            if (isNetworkAvailable(requireContext())
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(Preferences.SHOW_NATIVE_SINDHI_POETRY, "ON").equals("ON", true)
            ) {
                val pref = requireActivity().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                val adId = if (!BuildConfig.DEBUG) {
                    pref.getString(AD_ID_NATIVE_SINDHI_POETRY,  resources.getString(R.string.NATIVE_DEFAULT))
                } else {
                    resources.getString(R.string.NATIVE_DEFAULT)
                }
                NewNativeAdClass.checkAdRequestAdmob(
                    mContext = requireActivity(),
                    adId = adId!!,
                    fragmentName = "showFragment",
                    isMedia = false,
                    adContainer = binding.nativeAdContainerAd,
                    isMediumAd = false,
                    onFailed = {
                        binding.nativeAdContainerAd.visibility = View.GONE
                        binding.separator.visibility = View.GONE
                    },
                    onAddLoaded = {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        binding.separator.visibility = View.VISIBLE
                    })
            } else {
                binding.nativeAdContainerAd.visibility = View.GONE
                binding.separator.visibility = View.GONE
            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onPoetryItemCopyClicked(poetry: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", poetry)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text Copied to ClipBoard", Toast.LENGTH_SHORT).show()
    }

    override fun onPoetryItemShareClicked(poetry: String) {
        val intent2 = Intent()
        intent2.action = Intent.ACTION_SEND
        intent2.type = "text/plain"
        intent2.putExtra(Intent.EXTRA_TEXT, poetry)
        requireContext().startActivity(Intent.createChooser(intent2, "Share via"))
    }

    override fun onPoetryItemWhatsappClicked(poetry: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, poetry)
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPoetryItemMessengerClicked(poetry: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.type = "text/plain"
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, poetry)
        sendIntent.setPackage("com.facebook.orca")
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(Intent.createChooser(sendIntent, "Share text via..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please Install Facebook Messenger", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPoetryItemTwitterClicked(poetry: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, poetry)
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            requireContext().startActivity(Intent.createChooser(intent, "Share text via..."))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "You don't seem to have twitter installed on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 3. ADDED this function to handle passing text and navigating
    override fun onPoetryItemEditClicked(poetry: String) {
        val bundle = Bundle().apply {
            putString("passed_text", poetry)
        }

        navController?.navigate(R.id.action_navSindhiStatusShow_to_sindhiEditor, bundle)
    }
}