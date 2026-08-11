package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sindhi.urdu.english.keybad.R
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.sindhi.urdu.english.keybad.databinding.FragmentCameraBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isUsingBackCamera = true
    private lateinit var camera: Camera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var preview: Preview
    private var isTorchOn = false

    companion object {
        private const val TAG = "CameraXApp"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                startPreviewActivity(imageUri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.icFlashoff.isEnabled = false
        preview = Preview.Builder()
            .setTargetRotation(requireActivity().windowManager.defaultDisplay.rotation ?: Surface.ROTATION_0)
            .build()

        startCamera()
        setupFlashButton()
        setupSwitchCameraButton()

        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.icGallery.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = if (isUsingBackCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
                preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                binding.icFlashoff.isEnabled = true
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupFlashButton() {
        binding.icFlashoff.setOnClickListener {
            if (::camera.isInitialized) {
                isTorchOn = !isTorchOn
                camera.cameraControl.enableTorch(isTorchOn)
                binding.icFlashoff.setImageResource(if (isTorchOn) R.drawable.ic_flash else R.drawable.ic_flash_off)
            }
        }
    }

    private fun setupSwitchCameraButton() {
        binding.ivRotate.setOnClickListener {
            isUsingBackCamera = !isUsingBackCamera
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val cameraSelector = if (isUsingBackCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        binding.progress.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        val fileName = "PhotoTranslator_${System.currentTimeMillis()}.jpg"
        val outputDirectory = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.ime_name)).apply { mkdirs() }
        } ?: requireContext().filesDir

        val tempFile = File(outputDirectory, "/PhotoTranslator").apply { mkdirs() }
        val photoFile = File(tempFile, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to capture", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    binding.progressBar.visibility = View.GONE
                    rotateImageIfRequired(photoFile)
                    MediaScannerConnection.scanFile(requireContext(), arrayOf(photoFile.absolutePath), arrayOf("image/jpeg")) { _, _ -> }

                    val photoUri = Uri.fromFile(photoFile).toString()
                    startPreviewActivity(photoUri)
                }
            }
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            pickImageLauncher.launch(intent)
        }
    }

    private fun rotateImageIfRequired(photoFile: File) {
        try {
            val ei = ExifInterface(photoFile.absolutePath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            val fos = FileOutputStream(photoFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // --- NAVIGATION TO CROP FRAGMENT ---
    private fun startPreviewActivity(imageUriString: String?) {
        if (imageUriString != null) {
            val bundle = Bundle().apply { putString("image_uri", imageUriString) }
            findNavController().navigate(R.id.action_cameraFragment_to_cropFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}