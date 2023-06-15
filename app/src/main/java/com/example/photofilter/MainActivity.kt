package com.example.photofilter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_PICK_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun takePhoto(view: View) {
        if (hasCameraPermission()) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA_PERMISSION)
    }

    fun choosePhoto(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA_PERMISSION -> {
                    val image = data?.extras?.get("data") as Bitmap?
                    val imageUri = saveImageToTemporaryFile(image)
                    openEditPhotoActivity(imageUri)
                }
                REQUEST_PICK_IMAGE -> {
                    val selectedImageUri: Uri? = data?.data // Изменено: Используйте другое имя переменной, например, selectedImageUri
                    val imageBitmap = getImageBitmap(selectedImageUri)
                    val imageUri = saveImageToTemporaryFile(imageBitmap)
                    openEditPhotoActivity(imageUri)
                }
            }
        }
    }


    private fun getImageBitmap(imageUri: Uri?): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun saveImageToTemporaryFile(image: Bitmap?): Uri? {
        return try {
            val file = File(cacheDir, "temp_image.jpg")
            val outputStream = FileOutputStream(file)
            image?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.toUri() // Изменено: Возвращаем Uri вместо строки пути
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private fun openEditPhotoActivity(imageUri: Uri?) {
        val intent = Intent(this, EditPhotoActivity::class.java)
        intent.putExtra("imageUri", imageUri.toString())
        startActivity(intent)
    }

}
