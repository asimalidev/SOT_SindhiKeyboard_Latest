package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sindhi.urdu.english.keybad.R
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.databinding.FragmentPreviewPosterBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.Preferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.RemoteConfigConst.SHOW_INTER_SAVE_SINDHI_POST
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.safeNavigate
import java.io.File
import java.io.FileOutputStream

class PreviewPosterFragment : Fragment() {
    private var _binding: FragmentPreviewPosterBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    var navController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewPosterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        // 1. Get the image URI passed from the Editor
        val uriString = arguments?.getString("preview_image_uri")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            // Load the image into the ImageView
            binding.ivPreviewImage.setImageURI(imageUri)
        } else {
            Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
        }

        // 2. Setup Click Listeners
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_preview_to_home)
        }


        binding.btnSharePost.setOnClickListener {
            imageUri?.let { uri ->
                shareImage(uri)
            } ?: run {
                Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSavePost.setOnClickListener {

            if (navController != null) {
                val isNetworkAvailable = NetworkCheck.isNetworkAvailable(requireContext())
                val isPurchased = requireContext().getSharedPreferences(
                    RemoteConfigConst.REMOTE_CONFIG, Context.MODE_PRIVATE
                ).getBoolean(Preferences.IS_PURCHASED, false)
                val isAdEnabled = requireContext().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(SHOW_INTER_SAVE_SINDHI_POST, "ON").equals("ON", true)

                if (isNetworkAvailable && !isPurchased && isAdEnabled) {
                    InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                        context = requireContext(),
                        nameFragment = "DailyStatus",
                        onAdClosedCallBackAdmob = {
                            Handler(Looper.getMainLooper()).postDelayed({
                                // 2. CRITICAL FIX: Check if the fragment is still attached
                                // before navigating after the 300ms delay.
                                if (isAdded && activity != null) {
                                    imageUri?.let { uri ->
                                        saveImageToGallery(uri)
                                        val action= PreviewPosterFragmentDirections.actionPreviewToShowAll()
                                        (navController as NavController).safeNavigate(action)

                                    } ?: run {
                                        Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }, 300)
                        },
                        onAdShowedCallBackAdmob = {

                        }
                    )
                } else {
                    imageUri?.let { uri ->
                        saveImageToGallery(uri)
                        val action= PreviewPosterFragmentDirections.actionPreviewToShowAll()
                        (navController as NavController).safeNavigate(action)

                    } ?: run {
                        Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                isNavControllerAdded()
            }
        }
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as NavigationActivity).hidetoolbar()
    }


    override fun onStop() {
        super.onStop()
        if (!isVisible) {
            (activity as NavigationActivity).showtoolbar()
        }
    }

    private fun shareImage(uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
            // VERY IMPORTANT: Grant permission so other apps (like WhatsApp) can read your cached file
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Post via"))
    }


//    private fun saveImageToGallery(cachedUri: Uri) {
//        val resolver = requireContext().contentResolver
//        val displayName = "Urdu_Post_${System.currentTimeMillis()}.png"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // --- FOR ANDROID 10 AND ABOVE (Scoped Storage) ---
//            val contentValues = ContentValues().apply {
//                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
//                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
//                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Urdu Poster")
//            }
//
//            val galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//
//            try {
//                if (galleryUri != null) {
//                    resolver.openOutputStream(galleryUri).use { outputStream ->
//                        resolver.openInputStream(cachedUri).use { inputStream ->
//                            inputStream?.copyTo(outputStream!!)
//                        }
//                    }
//                    Toast.makeText(requireContext(), "Saved to Gallery successfully!", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(requireContext(), "Failed to create gallery entry", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
//            }
//
//        } else {
//            // --- FOR ANDROID 9 AND BELOW (Legacy Storage) ---
//
//            // 1. Create the specific folder path
//            val storageDir = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                "Sindhi Poster"
//            )
//            if (!storageDir.exists()) {
//                storageDir.mkdirs()
//            }
//
//            // 2. Create the file
//            val imageFile = File(storageDir, displayName)
//
//            try {
//                // 3. Write the bytes to the legacy file system
//                FileOutputStream(imageFile).use { outputStream ->
//                    resolver.openInputStream(cachedUri).use { inputStream ->
//                        inputStream?.copyTo(outputStream)
//                    }
//                }
//
//                // 4. CRITICAL: Tell the Android Media Scanner to scan the new file,
//                // otherwise it won't show up in the gallery app!
//                MediaScannerConnection.scanFile(
//                    requireContext(),
//                    arrayOf(imageFile.absolutePath),
//                    arrayOf("image/png")
//                ) { path, uri ->
//                    Log.i("PreviewPosterFragment", "Scanned $path:")
//                }
//
//                Toast.makeText(requireContext(), "Saved to Gallery successfully!", Toast.LENGTH_SHORT).show()
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Error saving image on Android 9", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun saveImageToGallery(cachedUri: Uri) {
        val resolver = requireContext().contentResolver
        val displayName = "Urdu_Post_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // --- FOR ANDROID 10 AND ABOVE (Scoped Storage) ---
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                // FIX: Changed from "Urdu Poster" to "Sindhi Poster"
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sindhi Poster")
            }

            val galleryUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            try {
                if (galleryUri != null) {
                    resolver.openOutputStream(galleryUri).use { outputStream ->
                        resolver.openInputStream(cachedUri).use { inputStream ->
                            inputStream?.copyTo(outputStream!!)
                        }
                    }
                    Toast.makeText(requireContext(), "Saved to Gallery successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to create gallery entry", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            }

        } else {
            // --- FOR ANDROID 9 AND BELOW (Legacy Storage) ---
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Sindhi Poster"
            )
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val imageFile = File(storageDir, displayName)

            try {
                FileOutputStream(imageFile).use { outputStream ->
                    resolver.openInputStream(cachedUri).use { inputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }

                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(imageFile.absolutePath),
                    arrayOf("image/png")
                ) { path, uri ->
                    Log.i("PreviewPosterFragment", "Scanned $path:")
                }

                Toast.makeText(requireContext(), "Saved to Gallery successfully!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image on Android 9", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}