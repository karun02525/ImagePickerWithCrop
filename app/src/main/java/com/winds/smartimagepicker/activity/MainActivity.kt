package com.winds.smartimagepicker.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.winds.imagepickerlibrary.ImagePicker
import com.winds.imagepickerlibrary.OnImagePickedListener
import com.winds.smartimagepicker.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var filePath: File? = null
    private lateinit var imagePicker: ImagePicker
    
    private lateinit var mContext: Context

    companion object {
        private const val TAG = "MainActivity"
    }

  

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_main)

        btnClick.setOnClickListener {
            imagePicker = ImagePicker(this, null, object : OnImagePickedListener {
                override fun onImagePicked(imageUri: Uri?) {
                  // image.setImageURI(imageUri)
                    imagePicker.setFileSize(700)
                    image.setImageBitmap(imagePicker.getBitmap())
                    filePath = imagePicker.getImageFile()
                    Log.d(TAG, "filePath $filePath")
                    Log.d(TAG, "imageUri $imageUri")
                    Log.d(TAG, "File Size ${imagePicker.getFileSize()}")

                }
            }).setWithImageCrop()
            imagePicker.choosePicture(true)



        }




    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.handleActivityResult(resultCode, requestCode, data)


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker.handlePermission(requestCode, grantResults)
    }


}
