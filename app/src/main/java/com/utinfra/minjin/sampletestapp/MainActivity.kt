package com.utinfra.minjin.sampletestapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.utinfra.minjin.sampletestapp.databinding.ActivityMainBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val REQUEST_WRITE = 2
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null
    private var photoFile: File? = null
    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraBtn.setOnClickListener {
            cameraPermission()

        }

        getResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

//                saveImageInAlbum(this)
            }

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

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        takePictureIntent.resolveActivity(packageManager)

        photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.utinfra.minjin.sampletestapp.fileprovider",
                    it
            )

            takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
            )
            Log.d("로그", "photoURI $photoURI")

        }
//        getResult.launch(takePictureIntent)
        startActivityForResult(takePictureIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val test = Uri.fromFile(File(currentPhotoPath))
            saveQImage(test)
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val name = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_DCIM + "/TEST/")

        this.name = name

        return File.createTempFile(
                name,
                ".jpg",
                storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }

    }

    private fun saveQImage(uri: Uri) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        values.put(MediaStore.Images.Media.IS_PENDING, 1)

        val contentResolver = contentResolver
        val item = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            val pdf = contentResolver.openFileDescriptor(item!!, "w", null)
            if (pdf == null) {
                Log.d("로그", "null")
            } else {

                val inputData: ByteArray = getBytes(uri)!!
                val fos = FileOutputStream(pdf.fileDescriptor)
                fos.write(inputData)
                fos.close()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(item, values, null, null)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.d("로그", "FileNotFoundException  : " + e.localizedMessage)
        } catch (e: Exception) {
            Log.d("로그", "FileOutputStream = : " + e.message)
        }

    }

    @Throws(IOException::class)
    fun getBytes(image_uri: Uri?): ByteArray? {
        val iStream: InputStream? = contentResolver.openInputStream(image_uri!!)
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024 // 버퍼 크기
        val buffer = ByteArray(bufferSize) // 버퍼 배열
        var len = 0
        // InputStream에서 읽어올 게 없을 때까지 바이트 배열에 쓴다.
        while (iStream!!.read(buffer).also { len = it } != -1) byteBuffer.write(buffer, 0, len)
        return byteBuffer.toByteArray()
    }

    private fun saveImageInAlbum(context: Context): Uri? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues()

            with(values) {
                put(MediaStore.Images.Media.TITLE, name)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/TEST")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }


            return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        } else {

            val file = photoFile!!

            if (!file.exists()) {
                file.mkdirs()
            }

            val values = ContentValues()

            with(values) {
                put(MediaStore.Images.Media.TITLE, name)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.BUCKET_ID, name)
                put(MediaStore.Images.Media.DATA, currentPhotoPath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            return context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            )

        }

    }

}