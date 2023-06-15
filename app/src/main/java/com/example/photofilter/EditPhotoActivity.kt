package com.example.photofilter
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class EditPhotoActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var btnFilter1: Button
    private lateinit var btnFilter2: Button
    private lateinit var btnFilter3: Button
    private lateinit var btnDone: Button

    private var currentFilter: Int = 0
    private var isDrawingMode: Boolean = false

    private lateinit var drawingPath: Path
    private lateinit var drawingPaint: Paint
    private lateinit var drawingCanvas: Canvas
    private lateinit var drawingBitmap: Bitmap

    private lateinit var originalBitmap: Bitmap
    private lateinit var editedBitmap: Bitmap
    companion object {
        private const val REQUEST_EXPORT_PHOTO = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        imageView = findViewById(R.id.imageViewEditedPhoto)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        btnFilter1 = findViewById(R.id.buttonFilter1)
        btnFilter2 = findViewById(R.id.buttonFilter2)
        btnFilter3 = findViewById(R.id.buttonFilter3)
        btnDone = findViewById(R.id.buttonDone)

        val imageUri = intent.getStringExtra("imageUri")
        originalBitmap = getImageBitmap(Uri.parse(imageUri))
        editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        imageView.setImageBitmap(editedBitmap)

        btnFilter1.setOnClickListener {
            applyFilter1()
        }

        btnFilter2.setOnClickListener {
            toggleDrawingMode()
        }

        btnFilter3.setOnClickListener {
            applyFilter3()
        }

        btnDone.setOnClickListener {
            saveEditedImage()
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyBrightnessFilter(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        drawingPaint = Paint()
        drawingPaint.isAntiAlias = true
        drawingPaint.color = Color.BLACK
        drawingPaint.style = Paint.Style.STROKE
        drawingPaint.strokeJoin = Paint.Join.ROUND
        drawingPaint.strokeWidth = 10f

        drawingPath = Path()
        drawingBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        drawingCanvas = Canvas(drawingBitmap)
    }

    private fun applyFilter1() {
        val blackAndWhiteBitmap = applyBlackAndWhiteFilter(originalBitmap)
        originalBitmap = combineBitmaps(blackAndWhiteBitmap, drawingBitmap)
        imageView.setImageBitmap(originalBitmap)
        currentFilter = 1
    }

    private fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val blackAndWhiteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(blackAndWhiteBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return blackAndWhiteBitmap
    }

    private fun toggleDrawingMode() {
        isDrawingMode = !isDrawingMode
        if (isDrawingMode) {
            imageView.setOnTouchListener { _, event ->
                handleDrawingTouchEvent(event)
                true
            }
        } else {
            imageView.setOnTouchListener(null)
        }
    }

    private fun handleDrawingTouchEvent(event: MotionEvent) {
        val action = event.action
        val x = event.x
        val y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                drawingPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                drawingPath.lineTo(x, y)
                drawingCanvas.drawPath(drawingPath, drawingPaint) // Рисуем на drawingCanvas
                imageView.setImageBitmap(combineBitmaps(originalBitmap, drawingBitmap)) // Обновляем imageView с комбинированным изображением и рисунком
            }
            MotionEvent.ACTION_UP -> {
                drawingPath.lineTo(x, y)
                drawingCanvas.drawPath(drawingPath, drawingPaint) // Рисуем на drawingCanvas
                drawingPath.reset()
                imageView.setImageBitmap(combineBitmaps(originalBitmap, drawingBitmap)) // Обновляем imageView с комбинированным изображением и рисунком
            }
        }
    }



    private fun applyFilter3() {
        val shrekBitmap = applyShrekFilter(originalBitmap)
        originalBitmap = combineBitmaps(shrekBitmap, drawingBitmap)
        imageView.setImageBitmap(originalBitmap)
        currentFilter = 3
    }

    private fun applyShrekFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val shrekBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(shrekBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(floatArrayOf(
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        ))
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return shrekBitmap
    }

    private fun applyBrightnessFilter(brightness: Int) {
        val adjustedBitmap = adjustBrightness(originalBitmap, brightness)
        editedBitmap = combineBitmaps(adjustedBitmap, drawingBitmap)
        imageView.setImageBitmap(editedBitmap)
    }

    private fun adjustBrightness(bitmap: Bitmap, brightness: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val adjustedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(adjustedBitmap)
        val paint = Paint()

        val brightnessMatrix = ColorMatrix()
        brightnessMatrix.setScale(brightness / 100f, brightness / 100f, brightness / 100f, 1f)

        val filter = ColorMatrixColorFilter(brightnessMatrix)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return adjustedBitmap
    }

    private fun saveEditedImage() {
        val resultBitmap = if (isDrawingMode) {
            combineBitmaps(originalBitmap, drawingBitmap)
        } else {
            editedBitmap
        }

        val intent = Intent(this, ExportPhotoActivity::class.java)
        val imageUri = saveImageToTemporaryFile(resultBitmap)
        intent.putExtra("imageUri", imageUri.toString())

        startActivityForResult(intent, REQUEST_EXPORT_PHOTO)
    }
    private fun saveImageToTemporaryFile(bitmap: Bitmap): Uri? {
        // Создаем файл во внешнем каталоге временных файлов
        val file = File.createTempFile("temp_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_DCIM))

        // Сохраняем Bitmap в файл
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }

        // Возвращаем URI файла
        return Uri.fromFile(file)
    }



    private fun getImageBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun combineBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(
            bitmap1.width,
            bitmap1.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmap1, 0f, 0f, null)
        canvas.drawBitmap(bitmap2, 0f, 0f, null)

        return combinedBitmap
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EXPORT_PHOTO && resultCode == RESULT_OK) {
            // Обработка результата из активности ExportPhotoActivity
        }
    }

}
