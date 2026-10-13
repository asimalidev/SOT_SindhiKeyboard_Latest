package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.databinding.FragmentUrduEditorBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.blockingClickListener
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters.BackgroundImagesAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters.ColorAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters.FontSpinnerAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorAdapters.StickersEditorAdapter
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorClasses.ColorPickerDialogFragment
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.BackgroundImageItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.ColorItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.FontCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.FontCache.fontCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.StickerDataCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.TextItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.stickerview.StickerImageView
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.viewModels.StickerViewModel
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.NavigationActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.StickersViewActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideKeyboard
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UrduEditorFragment : Fragment() {
    private lateinit var binding: FragmentUrduEditorBinding
    private var navController: NavController? = null
    private var currentEditedTextItem: TextItem? = null
    private var originalTextBeforeEdit: TextItem? = null
    private var colorList: MutableList<ColorItem>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isTouchingPlus = false
    private var isTouchingMinus = false
    private var selectedColor = Color.WHITE
    private lateinit var fontsAdapter: FontSpinnerAdapter
    private var backgroundBitmap: Bitmap? = null
    private val stickerViewModel: StickerViewModel by viewModels()
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var imageUriUrduEditorBackgrounds: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUrduEditorBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUriUrduEditorBackgrounds = it
                backgroundBitmap = createBitmapFromUri(it)
                binding.textEditorView.setBackgroundImage(bitmap = backgroundBitmap)
                Log.i("UrduEditorFragment", "MyUriTAG 1 : $imageUriUrduEditorBackgrounds")
            }
        }
        setupTextEditorView()
        isNavControllerAdded()


        binding.tvAddText.blockingClickListener {
            binding.clDoneCancel.visibility = View.VISIBLE
            binding.clDoneCancel.bringToFront()

            // USE HELPER HERE INSTEAD OF hideAllTexts = true
            setCanvasElementsVisibility(false)

            originalTextBeforeEdit = null
            addTextItem(" ")
            binding.textEditorView.selectedTextItem?.let {
                currentEditedTextItem = it.copy()
            }
            binding.textEditorView.selectedTextItem?.let {
                binding.textEditorView.clearText(it)
                binding.textEditorView.selectedTextItem = null
            }
            handleEditText()
            binding.etEnterText.setText("")
            binding.etEnterText.requestFocus()
        }

        binding.tvAddSticker.setOnClickListener {
            if (binding.clAddSticker.visibility == View.GONE) {
                binding.clAddSticker.visibility = View.VISIBLE
            } else {
                binding.clAddSticker.visibility = View.GONE
            }
        }

        binding.tvBackground.setOnClickListener {
            if (binding.clTextBackground.visibility == View.GONE) {
                binding.clTextBackground.visibility = View.VISIBLE
            } else {
                binding.clTextBackground.visibility = View.GONE
            }
        }

        binding.clAddSticker.setOnClickListener {
            binding.clAddSticker.visibility = View.GONE
        }

        binding.clTextBackground.setOnClickListener {
            binding.clTextBackground.visibility = View.GONE
        }

        binding.tvBgColor.blockingClickListener {
            showColorPicker()
        }

        binding.btnViewMoreStickers.blockingClickListener {
            if (navController != null) {
                startActivity(
                    Intent(
                        requireActivity(),
                        StickersViewActivity::class.java
                    ).putExtra("MoveTo", "sindhi_editor")
                )
            } else {
                isNavControllerAdded()
            }
        }

        binding.textEditorView.setBackgroundImage(resourceId = backgroundItems[0].backgroundResId)

        stickerViewModel.stickerPackData.observe(requireActivity(), Observer { stickerPackData ->
            stickerPackData?.let {
                Log.i(
                    "UrduEditorFragment",
                    "Fetched : ${stickerPackData.sticker_content.size} sticker packs"
                )
            } ?: run {
                Log.e("UrduEditorFragment", "Received null StickerPackData")
            }
        })

        if (StickerDataCache.stickerPackData == null) {
            if (NetworkCheck.isNetworkAvailable(requireActivity())) {
                stickerViewModel.fetchStickers()
            }
        }
        val gridSpanCount = 4
        val layoutManager = GridLayoutManager(requireActivity(), gridSpanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((binding.rvStickers.adapter as StickersEditorAdapter).getItemViewType(
                        position
                    ) == StickersEditorAdapter.TYPE_HEADER
                ) {
                    gridSpanCount
                } else {
                    1
                }
            }
        }

        val stickersAdapter = StickersEditorAdapter(mutableListOf()) { drawable ->
            addStickerToTextEditorView(drawable)
            binding.clAddSticker.visibility = View.GONE
        }
        binding.rvStickers.adapter = stickersAdapter
        binding.rvStickers.layoutManager = layoutManager

        stickerViewModel.stickerPackList.observe(viewLifecycleOwner) { stickerPacks ->
            if (stickerPacks.isNullOrEmpty()) {
                binding.clNoStickerFound.visibility = View.VISIBLE
                binding.rvStickers.visibility = View.INVISIBLE
            } else {
                binding.clNoStickerFound.visibility = View.GONE
                binding.rvStickers.visibility = View.VISIBLE
                val combinedList = mutableListOf<Any>()
                for (pack in stickerPacks) {
                    combinedList.add(pack.folderName)
                    combinedList.addAll(pack.stickerFiles)
                }
                stickersAdapter.updateData(combinedList)
            }
        }
        val passedText = arguments?.getString("passed_text")
        if (!passedText.isNullOrEmpty()) {
            // Use post to ensure the canvas is fully measured before drawing
            binding.textEditorView.post {
                // 1. Add the passed text to the canvas
                addTextItem(passedText)

                // 2. Open the text editor UI immediately with the passed text
                binding.clDoneCancel.visibility = View.VISIBLE
                binding.clDoneCancel.bringToFront()
                binding.textEditorView.hideAllTexts = true
                originalTextBeforeEdit = null

                binding.textEditorView.selectedTextItem?.let {
                    it.textSize = 40f
                    currentEditedTextItem = it.copy()
                    binding.textEditorView.clearText(it)
                    binding.textEditorView.selectedTextItem = null
                }

                handleEditText()
                binding.etEnterText.requestFocus()

                // 3. Clear the argument so it doesn't re-trigger if the screen rotates
                arguments?.putString("passed_text", null)
            }
        }
    }

    private fun setCanvasElementsVisibility(isVisible: Boolean) {
        // 1. Toggle text canvas items
        binding.textEditorView.hideAllTexts = !isVisible

        // 2. Toggle child views (stickers)
        val visibilityStatus = if (isVisible) View.VISIBLE else View.INVISIBLE
        for (i in 0 until binding.textEditorView.childCount) {
            binding.textEditorView.getChildAt(i).visibility = visibilityStatus
        }

        // 3. Refresh the view
        binding.textEditorView.invalidateChanges()
    }

    private fun addStickerToTextEditorView(drawable: Drawable) {
        val ivSticker = StickerImageView(requireContext(), binding.textEditorView)
        ivSticker.setImageDrawable(drawable)
        ivSticker.ownerStickerViewId = System.currentTimeMillis().toString()
        Log.i("UrduEditorFragment", "ivSticker.ownerId: ${ivSticker.ownerStickerViewId}")
        ivSticker.isSelected = true
        binding.textEditorView.addView(ivSticker)
    }

    override fun onResume() {
        super.onResume()
        stickerViewModel.loadStickerPacks(requireActivity())
        initUI()
        (activity as? NavigationActivity)?.hidetoolbar()

    }

    override fun onStop() {
        super.onStop()
        (activity as? NavigationActivity)?.showtoolbar()
    }


    override fun onPause() {
        super.onPause()
        binding.clExitDialog.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {

        if (fontCache.isEmpty()) {
            FontCache.loadAllTypefacesFromAssets(requireContext())
        }

        for ((fontName, typeface) in fontCache) {
            if (typeface != null) {
                binding.txtFont.typeface = FontCache.getTypeface(requireActivity(), fontName)
                Log.i("UrduEditorFragment", "Cached font: $fontName -> Typeface: $typeface")
            } else {
                Log.i(
                    "UrduEditorFragment",
                    "Cached font: $fontName -> Typeface: null (Failed to load)"
                )
            }
        }

        binding.clTextEditor.setOnClickListener {
            binding.rvFonts.visibility = View.GONE
            binding.clFontsRV.visibility = View.GONE

        }

        setColorList()
        showCustomDialog()
        hideUnusedUIComponents()
        setupTextEditorHeader()
        setupTextEditorFooter()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    changeDialogTitleDescription(
                        title = requireActivity().getString(R.string.label_quit_editing),
                        description = requireActivity().getString(R.string.label_description_exit_editing),
                        btnSaveVisibility = true,
                        btnCancelVisibility = true
                    )
                    binding.clExitDialog.visibility = View.VISIBLE
                }
            })
    }


    private fun showColorPicker() {
        // Assume ColorPickerDialogFragment is correctly implemented elsewhere
        ColorPickerDialogFragment(
            context = requireContext(),
            initialColor = selectedColor,
            onColorSelected = { color ->
                Log.i("UrduEditorFragment", " $selectedColor")
                if (color != -1) {
                    selectedColor = color
                    binding.textEditorView.setBackgroundImage(color = selectedColor)
                }
            },
            onDismiss = {
                // handle dismiss
            }
        ).show()
    }


    private val backgroundItems = mutableListOf(
        BackgroundImageItem(R.drawable.bg_1),
        BackgroundImageItem(R.drawable.bg_2),
        BackgroundImageItem(R.drawable.bg_3),
        BackgroundImageItem(R.drawable.bg_4),
        BackgroundImageItem(R.drawable.bg_5),

    )

    private fun setupTextEditorFooter() {
        binding.rvColors.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvFonts.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        binding.rvBackgrounds.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        val adapter = ColorAdapter(requireActivity(), colorList!!) { color ->
            currentEditedTextItem?.textColor = color
            setTextView()
        }

        binding.rvColors.adapter = adapter

        val adapterBackground = BackgroundImagesAdapter(backgroundItems) { position ->
            Log.i("UrduEditorFragment", "BackgroundImagesAdapterClick: $position")
            binding.textEditorView.setBackgroundImage(resourceId = backgroundItems[position].backgroundResId)
            binding.clTextBackground.visibility = View.GONE
        }
        binding.rvBackgrounds.adapter = adapterBackground

        val fontList = FontCache.loadFonts(context = requireActivity())
        fontsAdapter =
            FontSpinnerAdapter(context = requireActivity(), fontList = fontList) { fontName ->
                Log.i("UrduEditorFragment", "Selected font: $fontName")

                binding.txtFont.text = getPreviewTextForFont(fontName)

                FontCache.getTypeface(context = requireActivity(), fontName = fontName)?.let {
                    binding.txtFont.typeface = it
                    currentEditedTextItem?.fontName = fontName
                    currentEditedTextItem?.typeface = it
                }
                setTextView()
            }
        binding.rvFonts.adapter = fontsAdapter

        binding.txtFont.setOnClickListener {
            if (binding.clFontsRV.visibility == View.VISIBLE) {
                binding.clFontsRV.visibility = View.GONE
                binding.rvFonts.visibility = View.GONE
            } else {
                binding.clFontsRV.visibility = View.VISIBLE
                binding.rvFonts.visibility = View.VISIBLE
            }
        }
        binding.ivClose.setOnClickListener {
            if (binding.clFontsRV.visibility == View.VISIBLE) {
                binding.clFontsRV.visibility = View.GONE
                binding.rvFonts.visibility = View.GONE
            }
        }

        binding.btnPhoneStorage.blockingClickListener {
            if (navController != null) {
                galleryClicked()
            } else {
                isNavControllerAdded()
            }
        }
    }

    private fun getPreviewTextForFont(fontName: String): String {
        return if (FontCache.isUrduFont(fontName)) {
            "سندھی کی بورڈ"
        } else {
            "Sindhi Keyboard"
        }
    }

    private fun galleryClicked() {
        galleryLauncher.launch("image/*")
    }

    private fun imgSaveOrShare(bitmap: Bitmap, context: Context, notInCache: Boolean = false) {
        val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.textEditorView.saveBitmapAsPng(bitmap, context)
        } else {
            binding.textEditorView.saveBitmapToLegacyStorage(bitmap, context)
        }

        binding.rvFonts.adapter = fontsAdapter
        if (!notInCache) {
            if (savedUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, savedUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newUri(context.contentResolver, "Image", savedUri)
                }

                val resInfoList = context.packageManager.queryIntentActivities(shareIntent, 0)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        savedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            } else {
                Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setColorList() {
        colorList?.clear()
        colorList = ArrayList()
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.white)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.black)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.settingsColor)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.candidate_other)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.navy_blue)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.candidate_secondary_recommended)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.red)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.colorAccent)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.Amber)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.Chocolate)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.btnEnabledKeyboard)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.btnEnabledKeyboardNumber)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.candidate_background))
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun hideUnusedUIComponents() {
        isNavControllerAdded()
        requireActivity().findViewById<ImageView>(R.id.ivHistory)
            ?.visibility = View.INVISIBLE
        requireActivity().findViewById<ImageView>(R.id.ivPremium)
            ?.visibility = View.INVISIBLE

        val clEditing = requireActivity().findViewById<ConstraintLayout>(R.id.clEditing)
        val clSubDiv = requireActivity().findViewById<ConstraintLayout>(R.id.clSubDiv)

        if (clEditing != null) {
            clSubDiv?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_header_blue)
        }

        if (clEditing != null) {
            clEditing.visibility = View.VISIBLE
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupTextEditorHeader() {
        binding.txtUrduKeyboard.apply {
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.back), null, null, null
            )
            compoundDrawablePadding = 30

            text = resources.getString(R.string.label_sindhi_editor)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && event.x <= (compoundDrawables[0]?.bounds?.width() ?: 0)) {
                    if (binding.clTextEditor.visibility == View.VISIBLE) {
                        ivDismissEditingClicked()
                    } else if (binding.clTextEditor.visibility == View.GONE || binding.clTextEditor.visibility == View.INVISIBLE) {
                        binding.clDoneCancel.visibility = View.INVISIBLE
                        Log.i("UrduEditorFragment", "setupTextEditorHeader:")
                        changeDialogTitleDescription(
                            title = requireContext().getString(R.string.label_quit_editing),
                            description = requireContext().getString(R.string.label_description_exit_editing),
                            btnSaveVisibility = true,
                            btnCancelVisibility = true
                        )
                        binding.clExitDialog.visibility = View.VISIBLE
                    }
                    true
                } else {
                    false
                }
            }
        }

        binding.ivdoneEditor.setOnClickListener {
            val bitmap = binding.textEditorView.captureAsBitmap()

            // 2. Save to cache so we can safely pass it to the next screen
            val cachedImageUri = saveBitmapToCache(bitmap, requireContext())

            binding.clExitDialog.visibility = View.GONE

            // 3. Navigate to Preview screen with the URI
            if (cachedImageUri != null) {
                val bundle = Bundle().apply {
                    putString("preview_image_uri", cachedImageUri.toString())
                }
                // Replace with your actual navigation action ID
                findNavController().navigate(
                    R.id.action_editor_to_previewPosterFragment,
                    bundle
                )
            } else {
                Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun saveBitmapToCache(bitmap: Bitmap, context: Context): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream(File(cachePath, "preview_image.png")) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imagePath = File(cachePath, "preview_image.png")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imagePath)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun setupTextEditorView() {
        val drawables = mapOf(
            "edit" to ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_text),
            "close" to ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_close),
            "rotate" to ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_rotate),
            "resize" to ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_resize)
        )

        binding.textEditorView.setCornerDrawables(drawables)

        binding.textEditorView.onCornerButtonTouchListener = { buttonIndex ->
            when (buttonIndex) {
                0 -> {

                    setCanvasElementsVisibility(false)
                    binding.textEditorView.selectedTextItem?.let {
                        originalTextBeforeEdit = it.copy()
                        currentEditedTextItem = it.copy()
                        binding.textEditorView.clearText(it)
                        binding.textEditorView.selectedTextItem = null
                    }
                    handleEditText()
                    binding.etEnterText.requestFocus()
                }
                1 -> handleCloseText()
            }
        }
    }

    private fun addTextItem(text: String) {
        val textColor = colorList?.get(1)?.color ?: Color.BLACK

        val textSize = 100f

        binding.textEditorView.addText(text, 250.0f, 250.0f, textColor, textSize)
        binding.textEditorView.selectedTextItem = binding.textEditorView.textItems.lastOrNull()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleEditText() {
        binding.textEditorView.selectedTextItem?.let {
            currentEditedTextItem = it.copy()
        }

        colorList?.forEach { colorItem ->
            currentEditedTextItem?.let {
                colorItem.isSelected = colorItem.color == it.textColor
            }
        }

        binding.rvColors.adapter?.notifyDataSetChanged()

        currentEditedTextItem?.let {
            setTextView()
            binding.seekbarTextSize.progress = it.textSize.toInt()
            binding.tvTxtSize.text = "" + it.textSize.toInt()

            it.typeface?.let { typeface ->
                Log.i("UrduEditorFragment", "It has fonts")
                binding.txtFont.typeface = typeface
                binding.txtFont.text = getPreviewTextForFont(it.fontName ?: "")
            } ?: run {
                for ((fontName, typeface) in fontCache) {
                    if (typeface != null) {
                        binding.txtFont.text = getPreviewTextForFont(fontName)

                        binding.etEnterText.typeface =
                            FontCache.getTypeface(requireActivity(), fontName)
                        binding.txtFont.typeface =
                            FontCache.getTypeface(requireActivity(), fontName)

                        currentEditedTextItem?.fontName = fontName
                        currentEditedTextItem?.typeface =
                            FontCache.getTypeface(requireActivity(), fontName)
                        Log.i("UrduEditorFragment", "Cached font: $fontName -> Typeface: $typeface")
                        break
                    }
                }
                Log.i("UrduEditorFragment", "TypeFace Null")
            }

            when (it.alignment.name) {
                Paint.Align.LEFT.name -> {
                    changeLeftDrawables()
                }
                Paint.Align.CENTER.name -> {
                    changeCenterDrawable()
                }
                Paint.Align.RIGHT.name -> {
                    changeRightDrawable()
                }
            }
        }

        binding.etEnterText.addTextChangedListener(
            afterTextChanged = { text ->
                currentEditedTextItem?.text = text.toString()
            }
        )

        binding.clTextEditor.visibility = View.VISIBLE

        // --- FIX FOR UNCLICKABLE DONE/CANCEL BUTTONS ---
        binding.clDoneCancel.visibility = View.VISIBLE
        binding.clDoneCancel.bringToFront() // Forces it above the clTextEditor shield

        // Removed null checks, viewbinding guarantees existence
        binding.ivDismissEditing.setOnClickListener {
            ivDismissEditingClicked()
        }

        binding.ivDoneEditing.setOnClickListener {
            ivDoneEditingClicked()
        }

        val increaseTextSize = object : Runnable {
            override fun run() {
                currentEditedTextItem?.let {
                    it.textSize += 1
                    binding.tvTxtSize.text = "" + it.textSize.toInt()
                    binding.seekbarTextSize.progress = it.textSize.toInt()
                }
                setTextView()
                if (isTouchingPlus) {
                    handler.postDelayed(this, 100)
                }
            }
        }

        val decreaseTextSize = object : Runnable {
            override fun run() {
                currentEditedTextItem?.let {
                    if (it.textSize > 12) {
                        it.textSize -= 1
                        binding.tvTxtSize.text = "" + it.textSize.toInt()
                        binding.seekbarTextSize.progress = it.textSize.toInt()
                    }
                }
                setTextView()
                if (isTouchingMinus) {
                    handler.postDelayed(this, 100)
                }
            }
        }

        binding.icPlus.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchingPlus = true
                    handler.post(increaseTextSize)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isTouchingPlus = false
                    handler.removeCallbacks(increaseTextSize)
                }
            }
            true
        }

        binding.icMinus.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchingMinus = true
                    handler.post(decreaseTextSize)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isTouchingMinus = false
                    handler.removeCallbacks(decreaseTextSize)
                }
            }
            true
        }

        binding.seekbarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                // 1. Update the data model FIRST
                currentEditedTextItem?.let {
                    it.textSize = progress.toFloat()
                    binding.tvTxtSize.text = "$progress"
                }

                // 2. THEN update the UI
                setTextView()
                Log.i("UrduEditorFragment", "onProgressChanged:$progress")
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.ivAlignStart.setOnClickListener {
            currentEditedTextItem?.let {
                it.alignment = Paint.Align.LEFT
            }
            changeLeftDrawables()
        }

        binding.ivAlignCenter.setOnClickListener {
            currentEditedTextItem?.let {
                it.alignment = Paint.Align.CENTER
            }
            changeCenterDrawable()
        }

        binding.ivAlignEnd.setOnClickListener {
            currentEditedTextItem?.let {
                it.alignment = Paint.Align.RIGHT
            }
            changeRightDrawable()
        }
    }

    private fun setTextView() {
        currentEditedTextItem?.let {
            // If the canvas just has our placeholder space, show an empty box to the user
            val textToShow = if (it.text == " ") "" else it.text

            // Prevent infinite loops with the TextWatcher by checking before setting
            if (binding.etEnterText.text.toString() != textToShow) {
                binding.etEnterText.setText(textToShow)
                binding.etEnterText.setSelection(textToShow.length) // Move cursor to the end
            }

            binding.etEnterText.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.textSize)
            binding.etEnterText.setTextColor(it.textColor)
            binding.etEnterText.typeface = it.typeface

            // FIX: Use absolute LEFT/RIGHT to override Urdu RTL behavior
            val absoluteGravity = when (it.alignment) {
                Paint.Align.LEFT -> Gravity.LEFT or Gravity.CENTER_VERTICAL
                Paint.Align.CENTER -> Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                Paint.Align.RIGHT -> Gravity.RIGHT or Gravity.CENTER_VERTICAL
            }

            // Apply gravity to the wrapper layout to push the box across the screen
            binding.llEnterText.gravity = absoluteGravity

            // Apply gravity to the EditText to align multi-line text correctly
            binding.etEnterText.gravity = absoluteGravity
        }
    }

    private fun changeDialogTitleDescription(
        title: String,
        description: String,
        btnSaveVisibility: Boolean,
        btnCancelVisibility: Boolean
    ) {
        binding.txtTitle.text = title
        binding.descExitEditing.text = description

        if (btnSaveVisibility) {
            binding.btnSave.visibility = View.VISIBLE
        } else {
            binding.btnSave.visibility = View.GONE
        }

        if (btnCancelVisibility) {
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.btnCancel.visibility = View.GONE
        }
    }

    private fun changeRightDrawable() {
        setTextView()
        binding.ivAlignStart.clearColorFilter()
        binding.ivAlignCenter.clearColorFilter()
        binding.ivAlignEnd.setColorFilter(Color.parseColor("#0066FF"))
    }

    private fun changeCenterDrawable() {
        setTextView()
        binding.ivAlignStart.clearColorFilter()
        binding.ivAlignCenter.setColorFilter(Color.parseColor("#0066FF"))
        binding.ivAlignEnd.clearColorFilter()
    }

    private fun changeLeftDrawables() {
        setTextView()
        binding.ivAlignStart.setColorFilter(Color.parseColor("#0066FF")) // Selected Blue
        binding.ivAlignCenter.clearColorFilter() // Unselected Normal
        binding.ivAlignEnd.clearColorFilter()    // Unselected Normal
    }

    private fun ivDoneEditingClicked() {
        val activity = if (isAdded) activity else null ?: return
        val finalString = binding.etEnterText.text.toString().trim()
        activity?.hideKeyboard()
        setCanvasElementsVisibility(true)
        binding.textEditorView.hideAllTexts = false
        binding.clTextEditor.visibility = View.GONE
        binding.rvFonts.visibility = View.GONE
        binding.clFontsRV.visibility = View.GONE
        binding.clDoneCancel.visibility = View.INVISIBLE

        if (finalString.isNotEmpty()) {
            val current = currentEditedTextItem
            var selected = binding.textEditorView.selectedTextItem

            if (selected == null && current != null) {
                addTextItem(finalString)
                selected = binding.textEditorView.selectedTextItem
            }

            if (current != null && selected != null) {
                selected.text = finalString
                selected.textColor = current.textColor
                selected.textSize = current.textSize
                selected.rotation = current.rotation
                selected.alignment = current.alignment
                selected.rect = current.rect
                selected.fontName = current.fontName
                selected.isSelected = current.isSelected
                current.typeface?.let { selected.typeface = it }

                // --- FIX: UPDATED MARGIN CALCULATION ---
                val canvasWidth = binding.textEditorView.width.toFloat()

                val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = selected.textSize
                    typeface = selected.typeface
                }

                var maxLineWidth = 0f
                val lines = finalString.split("\n")
                for (line in lines) {
                    val w = paint.measureText(line)
                    if (w > maxLineWidth) maxLineWidth = w
                }

                // Calculate total safe margin:
                // 20f (box padding) + 30f (half of button size 60f) + 10f (extra breathing room)
                val edgeMargin = 60f

                val safeTextWidth = minOf(maxLineWidth, canvasWidth - (edgeMargin * 2))

                when (selected.alignment) {
                    Paint.Align.LEFT -> {
                        selected.x = edgeMargin
                    }
                    Paint.Align.CENTER -> {
                        selected.x = (canvasWidth - safeTextWidth) / 2f
                    }
                    Paint.Align.RIGHT -> {
                        selected.x = canvasWidth - safeTextWidth - edgeMargin
                    }
                }

                selected.y = current.y
            }

            binding.textEditorView.invalidateChanges()
            originalTextBeforeEdit = null

        } else {
            originalTextBeforeEdit = null
            binding.textEditorView.invalidateChanges()
        }
    }

    private fun ivDismissEditingClicked() {
        val activity = if (isAdded) activity else null
        activity?.hideKeyboard()
        setCanvasElementsVisibility(true)
        binding.textEditorView.hideAllTexts = false
        binding.clTextEditor.visibility = View.GONE
        binding.rvFonts.visibility = View.GONE
        binding.clFontsRV.visibility = View.GONE
        binding.clDoneCancel.visibility = View.INVISIBLE

        // --- RESTORE BACKUP IF THEY CANCEL EDITING ---
        if (originalTextBeforeEdit != null) {
            val backup = originalTextBeforeEdit!!
            addTextItem(backup.text)
            val restored = binding.textEditorView.selectedTextItem
            if (restored != null) {
                restored.text = backup.text
                restored.x = backup.x
                restored.y = backup.y
                restored.textColor = backup.textColor
                restored.textSize = backup.textSize
                restored.rotation = backup.rotation
                restored.alignment = backup.alignment
                restored.rect = backup.rect
                restored.fontName = backup.fontName
                restored.isSelected = false // Ensure it's not selected
                backup.typeface?.let { restored.typeface = it }
            }
            binding.textEditorView.invalidateChanges()
            originalTextBeforeEdit = null // Clear backup
        } else {
            if (currentEditedTextItem?.text.isNullOrEmpty() || binding.textEditorView.selectedTextItem?.text.isNullOrEmpty()) {
                handleCloseText()
            }
        }
    }

    private fun handleCloseText() {
        Log.e("UrduEditorFragment", "Removing text")
        binding.textEditorView.selectedTextItem?.let {
            binding.textEditorView.clearText(it)
            binding.textEditorView.selectedTextItem = null
        }
    }

    private fun showCustomDialog() {
        binding.imgClose.visibility = View.VISIBLE
        binding.descExitEditing.visibility = View.VISIBLE

        binding.btnSave.blockingClickListener {
            navigateBackOrHome()
        }

        activity?.let { ctx ->
            binding.txtTitle.text = ctx.getString(R.string.label_quit_editing)
            binding.btnCancel.text = ctx.getString(R.string.label_cancel)

            binding.imgClose.blockingClickListener {
                binding.clExitDialog.visibility = View.GONE
            }

            binding.btnCancel.blockingClickListener {
                binding.clExitDialog.visibility = View.GONE
            }
        } ?: run {
            Log.e("UrduEditorFragment", "Fragment is not attached to activity")
        }
    }

    private fun navigateBackOrHome() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_home, false)
            .build()

        navController?.navigate(R.id.nav_home, null, navOptions)
    }

    private fun createBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true // Check size without loading into memory
            }
            requireActivity().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate a sample size (e.g., scale down by 2 or 4)
            options.inSampleSize = 2
            options.inJustDecodeBounds = false

            requireActivity().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}