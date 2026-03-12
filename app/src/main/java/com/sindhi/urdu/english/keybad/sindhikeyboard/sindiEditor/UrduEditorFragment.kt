package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import com.sindhi.urdu.english.keybad.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.sindhi.urdu.english.keybad.databinding.FragmentUrduEditorBinding
import com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.Companion.imageUriUrduEditorBackgrounds
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
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.FontCache.loadFonts
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.StickerDataCache
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.TextItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.stickerview.StickerImageView
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.activities.StickersViewActivity
import com.sindhi.urdu.english.keybad.sindhikeyboard.utils.hideKeyboard
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.viewModels.StickerViewModel
import kotlin.collections.iterator
import androidx.core.view.isGone

class UrduEditorFragment : Fragment() {
    private lateinit var binding: FragmentUrduEditorBinding
    private var navController: NavController? = null
    private var clDoneCancel: ConstraintLayout? = null
    private var ivDismissEditing: ImageView? = null
    private var ivDoneEditing: ImageView? = null
    private var currentEditedTextItem: TextItem? = null
    private var colorList: MutableList<ColorItem>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isTouchingPlus = false
    private var isTouchingMinus = false
    private var selectedColor = Color.WHITE
    private lateinit var fontsAdapter: FontSpinnerAdapter
    private var backgroundBitmap: Bitmap? = null
    private val stickerViewModel: StickerViewModel by viewModels()
    var txtUrduKeyboard: AppCompatTextView? = null
    var ivSaveEditing: ImageView? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

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
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
            addTextItem("")
            handleEditText()
        }

        binding.tvAddSticker.setOnClickListener {
            if (binding.clAddSticker.isGone) {
                binding.clAddSticker.visibility = View.VISIBLE
            } else {
                binding.clAddSticker.visibility = View.GONE
            }
        }

        binding.tvBackground.setOnClickListener {
            if (binding.clTextBackground.isGone) {
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
                    ).putExtra("From", "FromUrduEditor")
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
    }

    override fun onPause() {
        super.onPause()
        binding.clExitDialog.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        if (fontCache == null) {
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
//                if (isAdded) {
//                    activity?.hideSystemUIUpdated()
//                }
            }
        ).show()
    }

    private val backgroundItems = mutableListOf(
        BackgroundImageItem(R.drawable.bg_1),
        BackgroundImageItem(R.drawable.bg_2),
        BackgroundImageItem(R.drawable.bg_3),
        BackgroundImageItem(R.drawable.bg_4),
        BackgroundImageItem(R.drawable.bg_5),
        BackgroundImageItem(R.drawable.bg_6)
    )

    private fun setupTextEditorFooter() {
        clDoneCancel = requireActivity().findViewById(R.id.clDoneCancel)
        ivDismissEditing = requireActivity().findViewById(R.id.ivDismissEditing)
        ivDoneEditing = requireActivity().findViewById(R.id.ivDoneEditing)
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

        val fontList = loadFonts(context = requireActivity())
        fontsAdapter =
            FontSpinnerAdapter(context = requireActivity(), fontList = fontList) { fontName ->
                Log.i("UrduEditorFragment", "Selected font: $fontName")
                binding.txtFont.text = fontName
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
            if (checkImagesPermissions()) {
                if (navController != null) {
                    galleryClicked()
                } else {
                    isNavControllerAdded()
                }
            } else {
                askForCameraImagesPermissions()
            }
        }
    }

    private fun galleryClicked() {
        galleryLauncher.launch("image/*")
    }

    private fun imgSaveOrShare(bitmap: Bitmap, context: Context, notInCache: Boolean = false) {
//        val savedUri = binding.textEditorView.saveBitmapAsPng(bitmap, context)
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
        colorList?.add(
            ColorItem(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.candidate_secondary_recommended
                )
            )
        )
        colorList?.add(
            ColorItem(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.board_theme__red
                )
            )
        )
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.colorAccent)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.Amber)))
        colorList?.add(ColorItem(ContextCompat.getColor(requireContext(), R.color.Chocolate)))
        colorList?.add(
            ColorItem(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btnEnabledKeyboard
                )
            )
        )
        colorList?.add(
            ColorItem(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btnEnabledKeyboardNumber
                )
            )
        )
        colorList?.add(
            ColorItem(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.candidate_background
                )
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    private fun hideUnusedUIComponents() {
        isNavControllerAdded()
        requireActivity().findViewById<ImageView>(R.id.ivHistory)
            .let { it?.visibility = View.INVISIBLE }
        requireActivity().findViewById<ImageView>(R.id.ivPremium)
            .let { it?.visibility = View.INVISIBLE }
        val clEditing = requireActivity().findViewById<ConstraintLayout>(R.id.clEditing)
        val clSubDiv = requireActivity().findViewById<ConstraintLayout>(R.id.clSubDiv)

        if (clEditing != null) {
            clSubDiv.background =
                requireActivity().resources.getDrawable(R.drawable.bg_header_blue, null)
        }

        if (clEditing != null) {
            clEditing.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTextEditorHeader() {
        // Ensure fragment is attached to activity before accessing it
        activity?.let { ctx ->
            txtUrduKeyboard = ctx.findViewById(R.id.txtSindhiKeyboard)
            ivSaveEditing = ctx.findViewById(R.id.ivSaveEditing)
            val ivShare = ctx.findViewById<ImageView>(R.id.ivShare)

            txtUrduKeyboard?.apply {
                setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.back
                    ), null, null, null
                )
                text = resources.getString(R.string.label_sindhi_editor)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && event.x <= (compoundDrawables[0]?.bounds?.width()
                            ?: 0)
                    ) {
                        if (binding.clTextEditor.visibility == View.VISIBLE) {
                            ivDismissEditing?.performClick()
                        } else if (binding.clTextEditor.visibility == View.GONE || binding.clTextEditor.visibility == View.INVISIBLE) {
                            clDoneCancel?.visibility = View.GONE
                            Log.i("UrduEditorFragment", "setupTextEditorHeader:")
                            changeDialogTitleDescription(
                                title = ctx.getString(R.string.label_quit_editing),
                                description = ctx.getString(R.string.label_description_exit_editing),
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

            ivShare.apply {
                setOnClickListener {
                    if (binding.clTextEditor.visibility == View.GONE || binding.clTextEditor.visibility == View.INVISIBLE) {
                        binding.textEditorView.addWaterMarkText("Sindhi Keyboard")
                        val bitmap = binding.textEditorView.captureAsBitmap()
                        imgSaveOrShare(bitmap = bitmap, context = ctx)
                    }
                }
            }

            ivSaveEditing?.apply {
                setOnClickListener {
                    if (binding.clTextEditor.visibility == View.GONE || binding.clTextEditor.visibility == View.INVISIBLE) {
                        changeDialogTitleDescription(
                            title = ctx.getString(R.string.save_poster),
                            description = ctx.getString(R.string.are_you_sure_to_save_changes),
                            btnSaveVisibility = true,
                            btnCancelVisibility = true
                        )
                        binding.clExitDialog.visibility = View.VISIBLE

//                        requireActivity().findViewById<ConstraintLayout>(R.id.clEditing)
//                            .let { it?.visibility = View.INVISIBLE }
                    }
                }
            }
        } ?: run {
            Log.e("UrduEditorFragment", "Fragment is not attached to activity")
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
                0 -> handleEditText()
                1 -> handleCloseText()
            }
        }
    }

    private fun addTextItem(text: String) {
        val textColor = colorList?.get(3)?.color
        val textSize = 50f
        binding.textEditorView.addText(text, 150.0f, 150.0f, textColor!!, textSize)
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
                binding.txtFont.text = it.fontName
            } ?: run {
                for ((fontName, typeface) in fontCache) {
                    if (typeface != null) {
                        binding.txtFont.text = fontName
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
        if (clDoneCancel != null && ivDismissEditing != null && ivDoneEditing != null) {
            clDoneCancel?.visibility = View.VISIBLE

            ivDismissEditing?.blockingClickListener {
                ivDismissEditingClicked()
            }

            ivDoneEditing?.blockingClickListener {
                ivDoneEditingClicked()
            }
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

        binding.seekbarTextSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, progress2: Boolean) {
                setTextView()
                Log.i("UrduEditorFragment", "onProgressChanged:$progress")
                currentEditedTextItem?.let {
                    it.textSize = progress.toFloat()
                    binding.tvTxtSize.text = "$progress"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
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
            binding.etEnterText.setText(it.text)
            binding.etEnterText.textSize = it.textSize
            binding.etEnterText.setTextColor(it.textColor)
            binding.etEnterText.typeface = it.typeface

            binding.llEnterText.gravity = when (it.alignment) {
                Paint.Align.LEFT -> Gravity.START
                Paint.Align.CENTER -> Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                Paint.Align.RIGHT -> Gravity.END
            }
        }
    }

    private fun changeDialogTitleDescription(title: String, description: String, btnSaveVisibility: Boolean, btnCancelVisibility: Boolean) {
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
        binding.ivAlignStart.setBackgroundResource(0)
        binding.ivAlignCenter.setBackgroundResource(0)
        binding.ivAlignEnd.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_selected_alignment))
    }

    private fun changeCenterDrawable() {
        setTextView()
        binding.ivAlignStart.setBackgroundResource(0)
        binding.ivAlignCenter.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_selected_alignment))
        binding.ivAlignEnd.setBackgroundResource(0)
    }

    private fun changeLeftDrawables() {
        setTextView()
        binding.ivAlignStart.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_selected_alignment))
        binding.ivAlignCenter.setBackgroundResource(0)
        binding.ivAlignEnd.setBackgroundResource(0)
    }


    private fun ivDoneEditingClicked() {
        Log.e("UrduEditorFragment", "ivDoneEditingClicked()")

        // Ensure the fragment is attached to an activity
        val activity = if (isAdded) activity else null ?: return

        val text = currentEditedTextItem?.text.orEmpty()
        if (text.isNotEmpty()) {

            activity?.hideKeyboard()
            binding.clTextEditor.visibility = View.GONE
            binding.rvFonts.visibility = View.GONE
            binding.clFontsRV.visibility = View.GONE
            clDoneCancel?.visibility = View.GONE

            val current = currentEditedTextItem
            val selected = binding.textEditorView.selectedTextItem

            if (current != null && selected != null) {
                selected.text = current.text
                selected.x = current.x
                selected.y = current.y
                selected.textColor = current.textColor
                selected.textSize = current.textSize
                selected.rotation = current.rotation
                selected.alignment = current.alignment
                selected.rect = current.rect
                selected.fontName = current.fontName
                selected.isSelected = current.isSelected
                current.typeface?.let { selected.typeface = it }
            }

            binding.textEditorView.invalidateChanges()

        } else {
            // Safely show dialog message if no text is present
            changeDialogTitleDescription(
                title = activity?.getString(R.string.no_text) ?: "",
                description = activity?.getString(R.string.there_is_no_text_in_the_field) ?: "",
                btnSaveVisibility = false,
                btnCancelVisibility = false
            )
            binding.clExitDialog.visibility = View.GONE
        }
    }


    private fun ivDismissEditingClicked() {
        Log.e("UrduEditorFragment", "ivDismissEditingClicked()")

        val activity = if (isAdded) activity else null
        activity?.hideKeyboard()


        binding.clTextEditor.visibility = View.GONE
        binding.rvFonts.visibility = View.GONE
        binding.clFontsRV.visibility = View.GONE
        clDoneCancel?.visibility = View.GONE

        if (currentEditedTextItem?.text.isNullOrEmpty() || binding.textEditorView.selectedTextItem?.text.isNullOrEmpty()) {
            handleCloseText()
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

        // Safely access the activity context
        activity?.let { ctx ->
            binding.txtTitle.text = ctx.getString(R.string.label_quit_editing)
            binding.btnCancel.text = ctx.getString(R.string.label_discard)

            binding.btnSave.blockingClickListener {
                binding.textEditorView.addWaterMarkText("Urdu English Keyboard")
                val bitmap = binding.textEditorView.captureAsBitmap()
                imgSaveOrShare(bitmap = bitmap, context = ctx, notInCache = true)
                binding.clExitDialog.visibility = View.GONE
                navController?.popBackStack()
            }

            binding.imgClose.blockingClickListener {
                binding.clExitDialog.visibility = View.GONE
            }

            binding.btnCancel.blockingClickListener {
                binding.clExitDialog.visibility = View.GONE
                navController?.popBackStack()
            }
        } ?: run {
            // Handle the case where the fragment is not attached to the activity
            Log.e("UrduEditorFragment", "Fragment is not attached to activity")
        }
    }

    private fun checkImagesPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun askForCameraImagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA),
                0
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 0
            )
        }
    }

    private fun createBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}