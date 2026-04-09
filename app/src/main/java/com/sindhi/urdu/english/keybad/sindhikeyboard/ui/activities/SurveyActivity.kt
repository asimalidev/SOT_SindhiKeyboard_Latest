package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sindhi.urdu.english.keybad.BuildConfig
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.ActivitySurveyBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck.Companion.isNetworkAvailable
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.AD_ID_NATVE_SURVAY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.IS_PURCHASED
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.NATIVE_SURVEY
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.REMOTE_CONFIG
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideSystemUIUpdated

class SurveyActivity : AppCompatActivity() {
    private  var _binding: ActivitySurveyBinding? =null
    private val binding get() = _binding
    private  lateinit var pref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySurveyBinding.inflate(layoutInflater)
        pref = getSharedPreferences(REMOTE_CONFIG, Context.MODE_PRIVATE)

        setContentView(binding?.root)
        supportActionBar?.hide()
        hideSystemUIUpdated()

        binding?.main.let { mainView ->
            if (mainView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }
        }
        loadNativeAd()
        binding?.btnCancel?.blockingClickListener {
            whenBack()
        }

        binding?.imgBack?.blockingClickListener {
            whenBack()
        }

        binding?.btnUninstall?.blockingClickListener {
            try {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:${packageName}".toUri()
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (_: Exception) {
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
                NATIVE_SURVEY, true) && !pref.getBoolean(IS_PURCHASED,false)
        ) {
            val adId = if (!BuildConfig.DEBUG){pref.getString(AD_ID_NATVE_SURVAY,getString(
                R.string.NATIVE_INSIDE_BIDDING))?:getString(R.string.NATIVE_INSIDE_BIDDING)} else{ getString(R.string.NATIVE_INSIDE_BIDDING)}
            NewNativeAdClass.checkAdRequestAdmob(
                mContext = this,
                fragmentName = "servay",
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