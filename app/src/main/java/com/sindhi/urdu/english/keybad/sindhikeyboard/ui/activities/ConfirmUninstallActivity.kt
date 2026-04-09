package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivityConfirmUninstallBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATIVE_UNINSTAL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_UNINSTALL
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideSystemUIUpdated

class ConfirmUninstallActivity : AppCompatActivity() {
    private  var _binding: ActivityConfirmUninstallBinding? =null
    private val binding get() = _binding
    private  lateinit var pref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityConfirmUninstallBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.hide()
     hideSystemUIUpdated()
        pref = getSharedPreferences(REMOTE_CONFIG, Context.MODE_PRIVATE)
        loadNativeAd()
        _binding?.imgBack?.blockingClickListener {
            whenBack()
        }

        binding?.btnTryAgain?.blockingClickListener {
            whenBack()
        }

        binding?.btnStillUninstall?.blockingClickListener {
            Intent(this, SurveyActivity::class.java).apply {
                startActivity(this)
            }
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                whenBack()
            }
        })

    }

    private fun whenBack() {
        Intent(this, NavigationActivity::class.java).apply {
            startActivity(this)
        }
        finish()
    }

    private fun loadNativeAd(){
        if (isNetworkAvailable(this) && pref.getBoolean(
                NATIVE_UNINSTALL, true) && !pref.getBoolean(IS_PURCHASED,false)
        ) {
            val adId = if (!BuildConfig.DEBUG){pref.getString(AD_ID_NATIVE_UNINSTAL,getString(
                R.string.NATIVE_INSIDE_BIDDING))?:getString(R.string.NATIVE_INSIDE_BIDDING)} else{ getString(R.string.NATIVE_INSIDE_BIDDING)}
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = this,
                fragmentName = "uninstall",
                adId = adId,
                isMedia = true,
                isMediaOnLeft = true,
                adContainer = binding?.frAds,
                isMediumAd = true,
                onFailed = {
                    runOnUiThread{
                        _binding?.let {
                            it.frAds.visibility = View.GONE
                        }

                    }

                },
                onAddLoaded = {
                    runOnUiThread{
                        _binding?.let {
                            it.shimmerLayout.stopShimmer()
                            it.shimmerLayout.visibility = View.GONE
                        }

                    }

                })
        } else {
            binding?.frAds?.visibility = View.GONE
        }
    }
}