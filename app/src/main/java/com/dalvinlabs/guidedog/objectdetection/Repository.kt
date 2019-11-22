package com.dalvinlabs.guidedog.objectdetection

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions

internal class Repository {

    private val tag: String = "Repository"
    private val objectDetector: FirebaseVisionObjectDetector

    init {
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)
    }

    fun startDetection(context: Context) {
        val bitmap = BitmapFactory.decodeStream(context.assets.open("street.png"))
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
        objectDetector.processImage(firebaseVisionImage)
            .addOnSuccessListener { objects ->
                Log.d(tag, "addOnSuccessListener objects = $objects")
                objects.forEach {
                    Log.d(tag, it.boundingBox.flattenToString())
                    Log.d(tag, it.classificationCategory.toString())
                    Log.d(tag, it.classificationConfidence.toString())
                    Log.d(tag, it.trackingId.toString())
                }
            }
            .addOnFailureListener {
                Log.d(tag, "addOnFailureListener exception = $it")
            }
    }
}