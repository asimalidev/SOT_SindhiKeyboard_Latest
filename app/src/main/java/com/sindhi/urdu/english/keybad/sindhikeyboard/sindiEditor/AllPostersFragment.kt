package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sindhi.urdu.english.keybad.databinding.FragmentAllPostersBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.PostersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AllPostersFragment : Fragment() {
    private var _binding: FragmentAllPostersBinding? = null
    private val binding get() = _binding!!
    private lateinit var postersAdapter: PostersAdapter
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPostersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadSavedPosters()
    }

    private fun setupRecyclerView() {
        postersAdapter = PostersAdapter { uri ->
            openPreviewOverlay(uri)
        }
        binding.rvPostGallery.adapter = postersAdapter
    }

    private fun setupClickListeners() {
        binding.ivBackPress.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnClosePreview.setOnClickListener {
            binding.clPreviewOverlay.visibility = View.GONE
            selectedImageUri = null
        }

        binding.btnSharePost.setOnClickListener {
            selectedImageUri?.let { uri ->
                sharePostImage(uri)
            } ?: Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadSavedPosters() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvPostGallery.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.ivEmptyState.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val savedUris = withContext(Dispatchers.IO) {
                val allFiles = mutableListOf<File>()
                val targetDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Sindhi Poster"
                )

                val legacyDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "UrduTextEditor"
                )

                // Gather files from primary directory
                if (targetDirectory.exists() && targetDirectory.isDirectory) {
                    targetDirectory.listFiles { file ->
                        file.isFile && (file.extension.equals("png", true) || file.extension.equals("jpg", true))
                    }?.let { allFiles.addAll(it) }
                }

                // Gather files from fallback directory
                if (legacyDirectory.exists() && legacyDirectory.isDirectory) {
                    legacyDirectory.listFiles { file ->
                        file.isFile && (file.extension.equals("png", true) || file.extension.equals("jpg", true))
                    }?.let { allFiles.addAll(it) }
                }

                // 3. SORT BY DATE: Sort all gathered files by 'lastModified' descending (newest first)
                val sortedFiles = allFiles.sortedByDescending { it.lastModified() }

                // 4. Convert sorted files to URIs
                val uris = mutableListOf<Uri>()
                sortedFiles.forEach { file ->
                    val uri = Uri.fromFile(file)
                    if (!uris.contains(uri)) uris.add(uri)
                }

                uris
            }

            binding.progressBar.visibility = View.GONE // Hide progress bar

            if (savedUris.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.ivEmptyState.visibility = View.VISIBLE
                binding.rvPostGallery.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.ivEmptyState.visibility = View.GONE
                binding.rvPostGallery.visibility = View.VISIBLE
                postersAdapter.submitList(savedUris)
            }
        }
    }

    private fun openPreviewOverlay(uri: Uri) {
        selectedImageUri = uri
        binding.ivLargePreview.setImageURI(uri)
        binding.clPreviewOverlay.visibility = View.VISIBLE
    }

    private fun sharePostImage(uri: Uri) {
        val secureShareUri: Uri = if (uri.scheme == "file") {
            val file = File(uri.path!!) // Extract the raw file path
            try {
                FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Toast.makeText(context, "FileProvider error: Check file_paths.xml", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            uri
        }
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, secureShareUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Post via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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