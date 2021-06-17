package com.utinfra.minjin.sampletestapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.utinfra.minjin.sampletestapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val REQUEST_WRITE = 2
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraBtn.setOnClickListener {
            cameraPermission()
        }

    }

    private fun cameraPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        }
    }

    private fun storagePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermission()
            }
        }

        if (requestCode == REQUEST_WRITE) {
            for (i in grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    return
                }
            }
            dispatchTakePictureIntent()
        }

    }


    private fun dispatchTakePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->


            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {

                    intent.resolveActivity(packageManager)?.also {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveImageInAlbum(this))
                        startActivityForResult(intent, REQUEST_CAMERA)
                    }
                }
            }

        }

    }

    //    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
//            Log.d("로그","")
//            Toast.makeText(this, " 저장 성공", Toast.LENGTH_SHORT).show()
//        }
//
//
//    }

    private fun saveImageInAlbum(context: Context): Uri? {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val name = "JPEG_${timeStamp}_"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues()

            with(values) {
                put(MediaStore.Images.Media.TITLE, name)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/KiniCare")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            return context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

        } else {

            val dir = "/storage/emulated/0/DCIM/Folder/KiniCare"
            val file = File(dir)

            if (!file.exists()) {
                file.mkdirs()
            }

            val imgFile = File(file, name)

            val values = ContentValues()

            with(values) {
                put(MediaStore.Images.Media.TITLE, name)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.BUCKET_ID, name)
                put(MediaStore.Images.Media.DATA, imgFile.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            return context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        }

    }


}