package com.example.dog_breed_identification_android_app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.dog_breed_identification_android_app.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.text.DecimalFormat


const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_CODE_PICK_IMAGE = 2

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var selectedBitmap: Bitmap
    private var imageWidth: Int? = null
    private var imageHeight: Int? = null
    val df = DecimalFormat("#.##")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val mainViewModel by viewModels<MainViewModel>()

        binding.btnCaptureImage.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnSelectImage.setOnClickListener {
            dispatchPickImageIntent()
        }

        binding.btnPredict.setOnClickListener {
            if (::selectedBitmap.isInitialized && selectedBitmap != null) {
                Toast.makeText(this, "Please Wait!", Toast.LENGTH_SHORT).show()

                binding.btnCaptureImage.isEnabled = false
                binding.btnSelectImage.isEnabled = false
                binding.btnPredict.isEnabled = false

                // convert bitmap to jpeg
                val stream = ByteArrayOutputStream()
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                val byteBuffer = ByteBuffer.allocate(selectedBitmap.byteCount)
                selectedBitmap.copyPixelsToBuffer(byteBuffer)
                val byteArray = byteBuffer.array()
//                val byteArray = stream.toByteArray()

                mainViewModel.detectDogBreed(byteArray, imageWidth!!, imageHeight!!)
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.dogBreedName.observe(this) {
            binding.topPrediction.text =
                it.entries.first().key + "(" + df.format(it.entries.first().value * 100) + "%)"

            var remainingPreds = ""

            for ((index, entry) in it.entries.withIndex()) {
                if (index >= 1) {
                    remainingPreds += entry.key + "(" + df.format(entry.value * 100) + "%), "
                }
            }
            binding.next4Preds.text = remainingPreds

            binding.btnCaptureImage.isEnabled = true
            binding.btnSelectImage.isEnabled = true
            binding.btnPredict.isEnabled = true
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the camera app is not available
        }
    }

    private fun dispatchPickImageIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the camera app is not available
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedBitmap = data?.extras?.get("data") as Bitmap
            imageWidth = selectedBitmap.width
            imageHeight = selectedBitmap.height

            binding.ivShowSelectedImage.setImageBitmap(selectedBitmap)

            // Use the imageBitmap to display or save the captured image
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.data != null) {
                val selectedImage: Uri = data.data!!
                val inputStream = contentResolver.openInputStream(selectedImage)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imageWidth = selectedBitmap.width
                imageHeight = selectedBitmap.height

                inputStream?.close()

                binding.ivShowSelectedImage.setImageBitmap(selectedBitmap)
            } else {
                Toast.makeText(this, "the selected image didn't give anything", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
}