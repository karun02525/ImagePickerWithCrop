package com.winds.imagepickerlibrary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.IOException
import java.util.*


class ImagePicker(
    private var activity: Activity,
    private val fragment: Fragment? = null,
    val listener: OnImagePickedListener
) : ImagePickerContract {


    companion object {
        private const val TAG = "ImagePicker"
        private const val CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA = 100
        private const val CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA = 101
        private var currentCameraFileName = ""
    }


    private var aspectRatioX = 0
    private var aspectRatioY = 0
    private var fileSize = 500
    private var withCrop = false
    private var imageFile: File? = null
    private var imagesUri: Uri? = null
    private var setFixAspectRatio = false
    private var setAutoZoomEnabled = false

    override fun setWithImageCrop(aspectRatioX: Int, aspectRatioY: Int): ImagePicker {
        withCrop = true
        this.aspectRatioX = aspectRatioX
        this.aspectRatioY = aspectRatioY
        return this
    }

    override fun setFileSize(fileSize: Int): ImagePicker {
        this.fileSize = fileSize
        return this
    }

    override fun setFixAspectRatio(
        fixAspectRatio: Boolean,
        autoZoomEnabled: Boolean
    ): ImagePicker? {
        this.setFixAspectRatio = fixAspectRatio
        this.setAutoZoomEnabled = autoZoomEnabled
        return this
    }


    @SuppressLint("NewApi")
    override fun choosePicture(includeCamera: Boolean) {
        if (needToAskPermissions()) {
            val neededPermissions = neededPermissions
            val requestCode =
                if (includeCamera) CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA else CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA
            if (fragment != null) {
                fragment.requestPermissions(neededPermissions, requestCode)
            } else {
                activity.requestPermissions(neededPermissions, requestCode)
            }
        } else {
            startImagePickerActivity(includeCamera)
        }
    }

    @SuppressLint("NewApi")
    override fun openCamera() {
        if (needToAskPermissions()) {
            if (fragment != null) {
                fragment.requestPermissions(
                    neededPermissions,
                    CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                )
            } else {
                activity.requestPermissions(
                    neededPermissions,
                    CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val cameraIntent = cameraIntent
            if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(
                    cameraIntent,
                    CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                )
            }
        }
    }

    override fun getImageFile(): File? {
        return getBitmap()?.onConvertBitmapToFile()
    }

    override fun getFileSize(): String? {
        return getImageFile()?.checkFileSize()
    }

    override fun getBitmap(): Bitmap? {
        return imagesUri?.getURI(activity, fileSize)
    }


    override fun handlePermission(requestCode: Int, grantResults: IntArray?) {
        Log.d(TAG, "handlePermission: $requestCode")
        if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA) {
            if (grantResults!!.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startImagePickerActivity(true)
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA) {
            if (grantResults!!.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startImagePickerActivity(false)
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults!!.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun handleActivityResult(
        resultCode: Int,
        requestCode: Int,
        data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "handleActivityResult: 1")
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                Log.d(TAG, "handleActivityResult: 2")
                handlePickedImageResult(data)
            } else {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    handleCroppedImageResult(data)
                }
            }
        } else {
            Log.d(TAG, "handleActivityResult: $resultCode")
            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(
                    TAG,
                    "onActivityResult: Image picker Error"
                )
            }
        }
    }

    private val neededPermissions: Array<String>
        get() = if (withCrop) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(Manifest.permission.CAMERA)
        }

    private fun needToAskPermissions(): Boolean {
        return if (withCrop) {
            (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

                    )
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handleCroppedImageResult(data: Intent?) {
        Log.d(TAG, "handleCroppedImageResult: ")
        val result = CropImage.getActivityResult(data)
        val croppedImageUri = result?.uri
       // deletePreviouslyCroppedFiles(croppedImageUri!!)
        imagesUri = croppedImageUri
        imageFile = File(croppedImageUri?.path!!)
        listener.onImagePicked(croppedImageUri)
    }

    @SuppressLint("NewApi")
    private fun handlePickedImageResult(data: Intent?) {
        var isCamera = true
        if (data != null && data.data != null) {
            val action = data.action
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
        }
        val imageUri =
            if (isCamera || data!!.data == null) getCameraFileUri(activity) else data.data!!
        if (isCamera) {
            deletePreviousCameraFiles()
        }
        Log.d(TAG, "handlePickedImageResult: $imageUri")
        if (withCrop) {
            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAutoZoomEnabled(setAutoZoomEnabled)
                .setFixAspectRatio(setFixAspectRatio)
                //.setAspectRatio(aspectRatioX, aspectRatioY)
                //  .setRequestedSize(12,34)
                .start(activity)
        } else {
            imagesUri = imageUri
            imageFile = File(imageUri.path!!)
            listener.onImagePicked(imageUri)
        }
    }

    private fun deletePreviousCameraFiles() {
        val imagePath = File(activity.filesDir, "images")
        if (imagePath.exists() && imagePath.isDirectory) {
            if (imagePath.listFiles()!!.isNotEmpty()) {
                for (file in imagePath.listFiles()!!) {
                    if (file.name != currentCameraFileName) {
                        file.delete()
                    }
                }
            }
        }
    }

    private fun deletePreviouslyCroppedFiles(currentCropImageUri: Uri) {
        Log.d(TAG, "deletePreviouslyCroppedFiles: $currentCropImageUri")
        val croppedImageName = currentCropImageUri.lastPathSegment
        val imagePath = activity.cacheDir
        Log.d(
            TAG,
            "deletePreviouslyCroppedFiles: " + imagePath.exists() + " " + imagePath.isDirectory
        )
        if (imagePath.exists() && imagePath.isDirectory) {
            Log.d(TAG, "deletePreviouslyCroppedFiles: $imagePath")
            Log.d(TAG, "deletePreviouslyCroppedFiles: " + imagePath.listFiles()?.size)
            if (imagePath.listFiles()!!.isNotEmpty()) {
                for (file in imagePath.listFiles()!!) {
                    Log.d(TAG, "deletePreviouslyCroppedFiles: " + file.name)
                    if (file.name != croppedImageName) {
                        file.delete()
                    }
                }
            }
        }
    }

    private val cameraIntent: Intent
        get() {
            currentCameraFileName = "outputImage" + System.currentTimeMillis() + ".jpg"
            val imagesDir = File(activity.filesDir, "images")
            imagesDir.mkdirs()
            val file = File(imagesDir, currentCameraFileName)
            try {
                file.createNewFile()
            } catch (e: IOException) {
                Log.d(TAG, "openCamera: coudln't crate ")
                e.printStackTrace()
            }
            Log.d(TAG, "openCamera: file exists " + file.exists() + " " + file.toURI().toString())
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val authority = activity.packageName + ".smart-image-picket-providers"
            val outputUri = FileProvider.getUriForFile(activity.applicationContext, authority, file)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            activity.grantUriPermission("com.google.android.GoogleCamera", outputUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            return cameraIntent
        }

    private fun getCameraFileUri(activity: Activity): Uri {
        val imagePath = File(activity.filesDir, "images/$currentCameraFileName")
        return Uri.fromFile(imagePath)
    }

    private fun startImagePickerActivity(includeCamera: Boolean) {
        val allIntents: MutableList<Intent> = ArrayList()
        val packageManager = activity.packageManager
        var galleryIntents = CropImage.getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, true)
        if (galleryIntents.size == 0) { // if no intents found for get-content try pick intent action (Huawei P9).
            galleryIntents = CropImage.getGalleryIntents(packageManager, Intent.ACTION_PICK, true)
        }
        if (includeCamera) {
            allIntents.add(cameraIntent)
        }


          for(i in galleryIntents.indices) {
              if(galleryIntents[i].component?.packageName=="com.google.android.apps.photos" &&
                  galleryIntents[i].component?.className=="com.google.android.apps.photos.picker.external.ExternalPickerActivity"){
                  allIntents.add(galleryIntents[i])
              }else if(galleryIntents[i].component?.packageName?.contains("gallery")!!){
                  allIntents.add(galleryIntents[i])
              }else{
                  Log.d(TAG, "startImagePickerActivity: $i")
              }
          }


        //  allIntents.addAll(galleryIntents) //if you want multiple chooser this enable this line

        val target: Intent
        if (allIntents.isEmpty()) {
            target = Intent()
        } else {
            target = allIntents[allIntents.size - 1]
            allIntents.removeAt(allIntents.size - 1)
        }
        // Create a chooser from the main  intent
        val chooserIntent =
            Intent.createChooser(target, activity.getString(R.string.select_source))
        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())
        activity.startActivityForResult(chooserIntent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE)
    }
  private  fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer).toString() + " " + model
        }
    }

    private fun capitalize(s: String?): String? {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

}