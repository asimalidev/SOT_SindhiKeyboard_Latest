package com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.editorClasses

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.TextItem
import com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.stickerview.StickerView
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow

@SuppressLint("ClickableViewAccessibility")
class TextEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var hideAllTexts: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    private var isRotating = false
    private var isResizing = false
    val textItems = mutableListOf<TextItem>()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.bg)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private var rotateOrgX = -1f
    private var rotateOrgY = -1f
    private var resizeOrgX = -1f
    private var resizeOrgY = -1f
    private var centerX: Double = 0.0
    private var centerY: Double = 0.0

    var selectedTextItem: TextItem? = null
    private val padding = 20f
    private val buttonSize = 60f

    private var drawables = mutableMapOf<String, Drawable?>()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var backgroundDrawable: Drawable? = null
    var onCornerButtonTouchListener: ((Int) -> Unit)? = null

    fun setCornerDrawables(drawables: Map<String, Drawable?>) {
        this.drawables = drawables.toMutableMap()
        invalidate()
    }

    fun setBackgroundImage(resourceId: Int? = null, color: Int? = null, bitmap: Bitmap? = null) {
        backgroundDrawable = when {
            color != null -> ColorDrawable(color)
            resourceId != null -> context.getDrawable(resourceId)
            bitmap != null -> BitmapDrawable(context.resources, bitmap)
            else -> null
        }
        this.background = backgroundDrawable
        invalidate()
    }

    init {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                deselectAllOthers()
            }
            false
        }
    }

    private fun deselectAllOthers() {
        for (sticker in StickerView.allStickers) {
            sticker.select(false)
        }
    }

    private fun getStaticLayout(item: TextItem, paint: TextPaint): StaticLayout {
        paint.color = item.textColor
        paint.textSize = item.textSize
        paint.typeface = item.typeface

        var maxLineWidth = 0f
        val lines = item.text.split("\n")
        for (line in lines) {
            val w = paint.measureText(line)
            if (w > maxLineWidth) maxLineWidth = w
        }

        val canvasWidth = if (width > 0) width - (padding * 4) else 1000f
        val layoutWidth = minOf(maxLineWidth, canvasWidth.toFloat()).toInt().coerceAtLeast(1)

        val alignment = when (item.alignment) {
            Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
            Paint.Align.CENTER -> Layout.Alignment.ALIGN_CENTER
            Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(item.text, 0, item.text.length, paint, layoutWidth)
                .setAlignment(alignment)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(item.text, paint, layoutWidth, alignment, 1.0f, 0.0f, false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundDrawable?.let {
            it.bounds = canvas.clipBounds
            it.draw(canvas)
        }

        selectedTextItem?.let {
            deselectAllOthers()
        }

        if (!hideAllTexts) {
            textItems.forEach { item ->

                // Generate the multiline layout
                val staticLayout = getStaticLayout(item, textPaint)

                // Update the bounding box mathematically
                item.updateRect(padding, staticLayout)

                canvas.save()
                // Rotate around the exact center of the bounding box
                canvas.rotate(item.rotation, item.rect.centerX(), item.rect.centerY())

                // Move canvas to the X,Y position to draw the text block
                canvas.translate(item.x, item.y)
                staticLayout.draw(canvas)
                canvas.restore()

                // Draw the border and buttons if selected
                if (item == selectedTextItem) {
                    canvas.save()
                    canvas.rotate(item.rotation, item.rect.centerX(), item.rect.centerY())
                    canvas.drawRect(item.rect, borderPaint)
                    drawCornerButtons(canvas, item.rect)
                    canvas.restore()
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> handleActionDown(event = it)
                MotionEvent.ACTION_MOVE -> handleActionMove(event = it)
                MotionEvent.ACTION_UP -> handleActionUp()
                else -> {}
            }
        }
        return true
    }

    fun resetSelectedState() {
        selectedTextItem = null
        invalidate()
    }

    private fun handleActionDown(event: MotionEvent): Boolean {
        rotateOrgX = event.rawX
        rotateOrgY = event.rawY

        resizeOrgX = event.rawX
        resizeOrgY = event.rawY

        centerX = event.x + width / 2.0
        centerY = event.y + height / 2.0

        lastTouchX = event.x
        lastTouchY = event.y
        if (handleCornerButtonTouch(event = event)) return true

        selectedTextItem = textItems.find { item ->
            val rotatedPoint = floatArrayOf(event.x, event.y)
            // Use rect.centerX() and centerY() for accurate hit detection
            val matrix = Matrix().apply { postRotate(-item.rotation, item.rect.centerX(), item.rect.centerY()) }
            matrix.mapPoints(rotatedPoint)
            item.rect.contains(rotatedPoint[0], rotatedPoint[1])
        }
        invalidate()
        return selectedTextItem != null
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1).absoluteValue.pow(2) + (y2 - y1).absoluteValue.pow(2))
    }

    private fun handleActionMove(event: MotionEvent) {
        selectedTextItem?.let { item ->
            if (isRotating || isResizing || handleCornerButtonTouch(event = event)) {
                if (isRotating) {
                    val itemCenterX = item.rect.centerX()
                    val itemCenterY = item.rect.centerY()
                    val initialAngle = atan2(rotateOrgY - itemCenterY, rotateOrgX - itemCenterX)
                    val currentAngle = atan2(event.rawY - itemCenterY, event.rawX - itemCenterX)
                    val angleDiff = Math.toDegrees((currentAngle - initialAngle).toDouble()).toFloat()
                    item.rotation = (item.rotation + angleDiff) % 360
                    rotateOrgX = event.rawX
                    rotateOrgY = event.rawY
                    invalidate()
                } else if (isResizing) {
                    val initialDistance = distance(centerX.toFloat(), centerY.toFloat(), resizeOrgX, resizeOrgY)
                    val currentDistance = distance(centerX.toFloat(), centerY.toFloat(), event.rawX, event.rawY)
                    val scaleFactor = (initialDistance / currentDistance).let {
                        1.0f + (it - 1.0f) * 1.5f
                    }

                    if (scaleFactor > 0) {
                        val newSize = item.textSize * scaleFactor
                        item.textSize = newSize.coerceIn(12f, 500f)
                        resizeOrgX = event.rawX
                        resizeOrgY = event.rawY
                        invalidate()
                    }
                }
            } else if (!isRotating && !isResizing) {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                val newRect = RectF(item.rect)
                newRect.offset(dx, dy)
                val boundaryMargin = 16 * Resources.getSystem().displayMetrics.density

                val clampedDx = when {
                    newRect.left < boundaryMargin -> boundaryMargin - item.rect.left
                    newRect.right > width - boundaryMargin -> (width - boundaryMargin) - item.rect.right
                    else -> dx
                }

                val clampedDy = when {
                    newRect.top < boundaryMargin -> boundaryMargin - item.rect.top
                    newRect.bottom > height - boundaryMargin -> (height - boundaryMargin) - item.rect.bottom
                    else -> dy
                }

                item.move(dx = clampedDx, dy = clampedDy)
                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
            }
        }
    }

    private fun handleActionUp() {
        isRotating = false
        isResizing = false
        invalidate()
    }

    private fun handleCornerButtonTouch(event: MotionEvent): Boolean {
        selectedTextItem?.let { item ->
            val rotatedPoint = floatArrayOf(event.x, event.y)
            val matrix = Matrix().apply {
                postRotate(-item.rotation, item.rect.centerX(), item.rect.centerY())
            }
            matrix.mapPoints(rotatedPoint)
            return checkIfTouchingButtons(rotatedPoint[0], rotatedPoint[1], item)
        }
        return false
    }

    private fun checkIfTouchingButtons(touchX: Float, touchY: Float, item: TextItem): Boolean {
        val halfButtonSize = buttonSize / 2

        return when {
            isTouchingButton(touchX, touchY, item.rect.left - halfButtonSize, item.rect.top - halfButtonSize) -> {
                if (!isRotating && !isResizing) {
                    onCornerButtonTouchListener?.invoke(0)
                    true
                } else false
            }
            isTouchingButton(touchX, touchY, item.rect.right + halfButtonSize, item.rect.top - halfButtonSize) -> {
                if (!isRotating && !isResizing) {
                    onCornerButtonTouchListener?.invoke(1)
                    true
                } else false
            }
            isTouchingButton(touchX, touchY, item.rect.left - halfButtonSize, item.rect.bottom + halfButtonSize) -> {
                if (!isResizing) {
                    isRotating = true
                    true
                } else false
            }
            isTouchingButton(touchX, touchY, item.rect.right + halfButtonSize, item.rect.bottom + halfButtonSize) -> {
                if (!isRotating) {
                    isResizing = true
                    true
                } else false
            }
            else -> false
        }
    }

    fun clearText(item: TextItem) {
        textItems.remove(item)
        selectedTextItem = null
        invalidate()
    }

    private fun isTouchingButton(touchX: Float, touchY: Float, buttonX: Float, buttonY: Float): Boolean {
        return touchX >= (buttonX - buttonSize) &&
                touchX <= (buttonX + buttonSize) &&
                touchY >= (buttonY - buttonSize) &&
                touchY <= (buttonY + buttonSize)
    }

    private fun drawCornerButtons(canvas: Canvas, bounds: RectF) {
        val halfButtonSize = buttonSize / 2
        drawables["edit"]?.let { drawable ->
            drawable.setBounds(
                (bounds.left - halfButtonSize).toInt(),
                (bounds.top - halfButtonSize).toInt(),
                (bounds.left + halfButtonSize).toInt(),
                (bounds.top + halfButtonSize).toInt()
            )
            drawable.draw(canvas)
        }
        drawables["close"]?.let { drawable ->
            drawable.setBounds(
                (bounds.right - halfButtonSize).toInt(),
                (bounds.top - halfButtonSize).toInt(),
                (bounds.right + halfButtonSize).toInt(),
                (bounds.top + halfButtonSize).toInt()
            )
            drawable.draw(canvas)
        }
        drawables["rotate"]?.let { drawable ->
            drawable.setBounds(
                (bounds.left - halfButtonSize).toInt(),
                (bounds.bottom - halfButtonSize).toInt(),
                (bounds.left + halfButtonSize).toInt(),
                (bounds.bottom + halfButtonSize).toInt()
            )
            drawable.draw(canvas)
        }
        drawables["resize"]?.let { drawable ->
            drawable.setBounds(
                (bounds.right - halfButtonSize).toInt(),
                (bounds.bottom - halfButtonSize).toInt(),
                (bounds.right + halfButtonSize).toInt(),
                (bounds.bottom + halfButtonSize).toInt()
            )
            drawable.draw(canvas)
        }
    }

    fun addText(text: String, x: Float, y: Float, textColor: Int, textSize: Float) {
        textItems.add(
            TextItem(
                text = text,
                x = x,
                y = y,
                textColor = textColor,
                textSize = textSize,
                rotation = 0f
            )
        )
        invalidate()
    }

    fun addWaterMarkText(text: String, textColor: Int = Color.BLACK, textSize: Float = 24f) {
        val paint = Paint().apply {
            this.textSize = textSize
            this.color = textColor
            this.isAntiAlias = true
        }

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        val x = (canvasWidth - textWidth) / 2
        val y = canvasHeight - textHeight - 5f

        textItems.add(TextItem(text = text, x = x, y = y, textColor = textColor, textSize = textSize, rotation = 0f))
        invalidate()
    }

    fun invalidateChanges() {
        invalidate()
    }

    // CHANGED: Update Rect now uses the height/width of the multiline layout
    private fun TextItem.updateRect(padding: Float, layout: StaticLayout) {
        rect.set(
            x - padding,
            y - padding,
            x + layout.width + padding,
            y + layout.height + padding
        )
    }

    private fun TextItem.move(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun captureAsBitmap(): Bitmap {
        resetSelectedState()
        deselectAllOthers()
        if (width == 0 || height == 0) {
            measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun saveBitmapAsPng(bitmap: Bitmap, context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Sindhi_Text_Editor_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/SindhiTextEditor")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
        }
        return uri
    }

    fun saveBitmapToLegacyStorage(bitmap: Bitmap, context: Context): Uri? {
        val fileName = "Urdu_Text_Editor_${System.currentTimeMillis()}.png"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/UrduTextEditor")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val file = File(storageDir, fileName)
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
            ) { path, uri ->
                Log.d("MediaScanner", "File scanned: $path, Uri: $uri")
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("SaveBitmap", "Error saving bitmap", e)
            null
        }
    }
}