package com.winds.imagepickerlibrary

import android.content.Intent
import android.graphics.Bitmap
import java.io.File

interface ImagePickerContract {
    fun setWithImageCrop(aspectRatioX: Int=90, aspectRatioY: Int=110): ImagePicker
    fun setFileSize(fileSize:Int=500): ImagePicker
    fun setFixAspectRatio(fixAspectRatio: Boolean=false,autoZoomEnabled: Boolean=false): ImagePicker?
    // todo add this in v1.1
//    ImagePicker setWithImageCrop();
//    ImagePicker setWithIntentPickerTitle(String title);
//    ImagePicker setWithIntentPickerTitle(@StringRes int title);
    fun choosePicture(includeCamera: Boolean)

    fun openCamera()
    fun getImageFile(): File?
    fun getFileSize(): String?
    fun getBitmap(): Bitmap?
    fun handlePermission(requestCode: Int, grantResults: IntArray?)
    fun handleActivityResult(resultCode: Int, requestCode: Int, data: Intent?)
}