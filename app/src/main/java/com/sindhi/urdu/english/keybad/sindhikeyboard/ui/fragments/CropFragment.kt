package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sindhi.urdu.english.keybad.R
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sindhi.urdu.english.keybad.databinding.FragmentCropBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NetworkCheck
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountryListMe
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.DialogView
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import java.io.IOException
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_CROP_TO_IMG_TRANSLATOR

class CropFragment : Fragment(), CropImageView.OnCropImageCompleteListener {
    private lateinit var binding: FragmentCropBinding
    private var sourceUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uriString = arguments?.getString("image_uri")
        if (uriString != null) {
            sourceUri = Uri.parse(uriString)
            binding.cropImageView.setImageUriAsync(sourceUri)
        } else {
            Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.cropImageView.setOnCropImageCompleteListener(this)

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivBackPress.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnOK.setOnClickListener {
            binding.clProgress.visibility = View.GONE // Show loading overlay
            binding.cropImageView.croppedImageAsync() // Start cropping
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        binding.clProgress.visibility = View.GONE

        if (result.isSuccessful) {
            val croppedBitmap = result.bitmap
            val croppedUri = if (croppedBitmap != null) {
                saveBitmapToCache(croppedBitmap)
            } else {
                result.uriContent
            }

            if (croppedUri != null) {
                // Instantly pass the newly cropped image to ML Kit!
                processImage(croppedUri)
            } else {
                Toast.makeText(requireContext(), "Failed to get cropped image", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Crop failed: ${result.error?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val file = File.createTempFile("cropped_image_", ".jpg", requireContext().cacheDir)
            val outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun processImage(uri: Uri) {
        try {
            DialogView.showLoadingDialog(requireContext(), "Extracting text...")

            val input = requireContext().contentResolver.openInputStream(uri)
                ?: throw IOException("Could not open stream")
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()

            if (bitmap == null) throw IOException("Failed to decode valid bitmap")

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizers = listOf(
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()),
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()),
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build()),
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            )

            val textFutures = recognizers.map { it.process(image) }
            Tasks.whenAllComplete(textFutures).addOnSuccessListener {
                DialogView.dismissDialog()

                val sb = StringBuilder()
                for (future in textFutures) {
                    val result = future.result
                    if (result != null && result.textBlocks.isNotEmpty()) {
                        result.textBlocks.forEach { sb.append(it.text).append("\n") }
                        break
                    }
                }

                val recognizedText = sb.toString()
                if (recognizedText.isNotBlank()) {
                    android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit { putString("PhotoExtractedText", recognizedText) }

                    detectLanguage(recognizedText)

                    if (NetworkCheck.isNetworkAvailable(requireContext())) {
                        navigateToResult()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No internet connection.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No text recognized. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                DialogView.dismissDialog()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            DialogView.dismissDialog()
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detectLanguage(text: String) {
        LanguageIdentification.getClient().identifyLanguage(text).addOnSuccessListener { code ->
            if (code != "und") {
                AllCountryListMe.getCountryList().firstOrNull { it.code == code }?.let {
                    android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit { putString("fromlangPT", it.countryName) }
                }
            }
        }
    }

    private fun navigateToResult() {
        try {
            val sharedPrefs = requireActivity().getSharedPreferences(RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE)
            val remotePrefs = requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)

            val isInternetOn = NetworkCheck.isNetworkAvailable(requireActivity())
            val isPurchased = sharedPrefs.getBoolean(Preferences.IS_PURCHASED, false)
            val proceedAd = remotePrefs.getString(SHOW_INTER_CROP_TO_IMG_TRANSLATOR, "ON") == "ON"

            Log.d("NavDebug", "Status -> Internet: $isInternetOn, Purchased: $isPurchased, AdEnabled: $proceedAd")

            if (isInternetOn && !isPurchased && proceedAd) {
                Log.d("NavDebug", "Attempting to show AdMob Interstitial...")

                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                    context = requireActivity(),
                    nameFragment = "CropFragment",
                    onAdClosedCallBackAdmob = {
                        Log.d("NavDebug", "Ad closed/failed callback triggered. Navigating...")
                        safeNavigate()
                    },
                    onAdShowedCallBackAdmob = {
                        Log.d("NavDebug", "Ad successfully showed on screen.")
                    }
                )
            } else {
                Log.d("NavDebug", "Conditions not met for ad. Navigating directly...")
                safeNavigate()
            }
        } catch (e: Exception) {
            Log.e("NavDebug", "CRASH inside navigateToResult setup: ${e.message}", e)
        }
    }

    private fun safeNavigate() {
        // 1. Ensure the fragment is still alive before doing anything
        if (!isAdded || view == null) {
            Log.e("NavDebug", "Navigation aborted: Fragment is not attached or view is null.")
            return
        }

        // 2. Queue the navigation to ONLY happen when the fragment is fully resumed
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            try {
                Log.d("NavDebug", "Fragment is fully resumed. Executing Navigation action now...")

                val navController = findNavController()
                val currentDestId = navController.currentDestination?.id

                // 3. Double-check we are actually on the CropFragment before navigating
                if (currentDestId == R.id.cropFragment) {
                    navController.navigate(R.id.action_cropFragment_to_navPhotoTranslatorResult)
                    Log.d("NavDebug", "Navigation executed successfully.")
                } else {
                    Log.e("NavDebug", "Cannot navigate: Current destination is NOT cropFragment. It is: ${navController.currentDestination?.label}")
                }

            } catch (e: Exception) {
                // 4. If it crashes now, this will catch it and print the exact reason
                Log.e("NavDebug", "CRASH during actual navigation: ${e.message}", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as NavigationActivity).hidetoolbar()
    }

    override fun onPause() {
        super.onPause()
        (activity as NavigationActivity).showtoolbar()
    }
}