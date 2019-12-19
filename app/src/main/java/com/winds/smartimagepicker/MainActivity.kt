package com.winds.smartimagepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.winds.imagepickerlibrary.ImagePicker
import com.winds.imagepickerlibrary.OnImagePickedListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var filePath: File? = null
    private lateinit var mContext: MainActivity
    private lateinit var imagePicker: ImagePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_main)

        btnClick.setOnClickListener {
            imagePicker = ImagePicker(this, null, object : OnImagePickedListener {
                override fun onImagePicked(imageUri: Uri?) {
                    filePath = imagePicker.getImageFile()
                    Log.d("TAGS", "FilePath $filePath")
                    image.setImageURI(imageUri)
                }
            }).setWithImageCrop(true)
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
