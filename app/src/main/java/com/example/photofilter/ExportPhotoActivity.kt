package com.example.photofilter
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ExportPhotoActivity : AppCompatActivity() {
    private lateinit var imageViewExport: ImageView
    private lateinit var btnShare: Button
    private lateinit var btnSave: Button

    private lateinit var imageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_photo)

        imageViewExport = findViewById(R.id.imageViewExport)
        btnShare = findViewById(R.id.btnShare)
        btnSave = findViewById(R.id.btnSave)

        // Получаем переданный bitmap изображения
        val imageUri = intent.getStringExtra("imageUri")
        imageBitmap = getImageBitmap(Uri.parse(imageUri))!!

        imageViewExport.setImageBitmap(imageBitmap)

        btnShare.setOnClickListener {
            shareImage()
        }

        btnSave.setOnClickListener {
            saveImage()
        }
    }

    private fun getImageBitmap(uri: Uri): Bitmap? {
        return try {
            // Читаем изображение из URI
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun shareImage() {
        val imageUri = getImageUri(imageBitmap) // Получение URI изображения

        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(Intent.createChooser(sendIntent, "Поделиться через..."))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Нет поддерживающих приложений для обмена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val imageFile = File(externalCacheDir, "image.jpg")

        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
    }

    private fun saveImage() {
        val imageUri = saveImageToGallery(imageBitmap)
        if (imageUri != null) {
            // Опционально, вы можете показать уведомление о сохранении изображения
            Toast.makeText(this, "Изображение сохранено в галерее", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        return try {
            val contentResolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                val outputStream = contentResolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream?.close()
            }

            uri
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
