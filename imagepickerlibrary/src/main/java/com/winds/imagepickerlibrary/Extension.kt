package com.winds.imagepickerlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DecimalFormat


private val format: DecimalFormat = DecimalFormat("#.##")
private const val MiB = 1024 * 1024.toLong()
private const val KiB: Long = 1024

fun checkFileSize(uri: Uri?): String? {
    val f = File(uri?.path!!)
    val length: Long = f.length()
    if (length > MiB) {
        return format.format(length / MiB).toString() + " MB"
    }
    return if (length > KiB) {
        format.format(length / KiB).toString() + " KB"
    } else format.format(length).toString() + " Byte"
}


fun Uri.getBitmap(context: Context?): Bitmap {
    val bitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver, this)
    return getResizedBitmap(rotateImageIfRequired(bitmap, this), 300)
}

private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
    var width = image.width
    var height = image.height

    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 0) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(image, width, height, true)
}


fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {

    val ei = ExifInterface(selectedImage.path)
    val orientation =
        ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
        else -> img
    }
}

private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}

fun Bitmap.getImageUri(inContext: Context): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = Images.Media.insertImage(inContext.contentResolver, this, "Title", null)
    return Uri.parse(path)
}