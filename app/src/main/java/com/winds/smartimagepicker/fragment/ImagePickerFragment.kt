package com.winds.smartimagepicker.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.winds.imagepickerlibrary.ImagePicker
import com.winds.imagepickerlibrary.OnImagePickedListener
import com.winds.smartimagepicker.R
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.io.File

class ImagePickerFragment : Fragment() {

    private var filePath: File? = null
    private lateinit var mContext: ActivitySecond
    private lateinit var imagePicker: ImagePicker
    private lateinit var image:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext= activity as ActivitySecond
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_main, container, false)

        image=view.image
        view.btnClick.setOnClickListener {
            imagePicker = ImagePicker(mContext, null, object : OnImagePickedListener {
                override fun onImagePicked(imageUri: Uri?) {
                    // image.setImageURI(imageUri)
                    imagePicker.setFileSize(700)
                    image.setImageBitmap(imagePicker.getBitmap())
                    filePath = imagePicker.getImageFile()
                    Log.d("TAGS", "fragment_ filePath $filePath")
                    Log.d("TAGS", "fragment_ imageUri $imageUri")
                    Log.d("TAGS", "fragment_ File Size ${imagePicker.getFileSize()}")

                }
            }).setWithImageCrop()
            imagePicker.choosePicture(true)

        }
        return view
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
