package com.winds.imagepickerlibrary

import android.net.Uri

interface OnImagePickedListener {
    fun onImagePicked(imageUri: Uri?)
}