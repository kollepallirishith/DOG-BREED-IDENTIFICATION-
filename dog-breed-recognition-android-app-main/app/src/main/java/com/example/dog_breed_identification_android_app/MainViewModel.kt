package com.example.dog_breed_identification_android_app

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _dogBreedName = MutableLiveData<Map<String, Float>>()
    val dogBreedName: LiveData<Map<String, Float>>
        get() = _dogBreedName

    private val app = application as MyApp

    fun detectDogBreed(byteArray: ByteArray, imageWidth: Int, imageHeight: Int) {
        viewModelScope.launch {
            val py = app.pythonInstance

            val dogBreedDetector = py.getModule("dog_breed_predictor")

            dogBreedDetector.callAttr("load_image", byteArray, imageWidth, imageHeight)

            val out = dogBreedDetector.callAttr("predict")

            withContext(Dispatchers.Main) {
                val type = object : TypeToken<Map<String, Double>>() {}.type
                val map: Map<String, Float> = Gson().fromJson(out.toString(), type)

                _dogBreedName.value = map
            }
        }
    }
}