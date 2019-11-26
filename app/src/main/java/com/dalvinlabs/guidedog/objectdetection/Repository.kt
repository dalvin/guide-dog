package com.dalvinlabs.guidedog.objectdetection

import android.graphics.Rect
import android.media.Image
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal class Repository {

    private val tag: String = "Repository"
    private val objectDetector: FirebaseVisionObjectDetector
    private val subject: PublishSubject<Rect> = PublishSubject.create()

    init {
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)
    }

    fun startDetection(image: Image, rotation: Int): Observable<Rect> {
        Log.d(tag, "startDetection()")
        val firebaseVisionImageRotation = when (rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_0
        }
        val firebaseVisionImage = FirebaseVisionImage.fromMediaImage(image, firebaseVisionImageRotation)
        objectDetector.processImage(firebaseVisionImage)
            .addOnSuccessListener { objects ->
                Log.d(tag, "addOnSuccessListener objects = $objects")
                objects.forEach {
                    Log.d(tag, it.boundingBox.flattenToString())
                    Log.d(tag, it.classificationCategory.toString())
                    Log.d(tag, it.classificationConfidence.toString())
                    Log.d(tag, it.trackingId.toString())
                    subject.onNext(it.boundingBox)
                }
            }
            .addOnFailureListener {
                Log.d(tag, "addOnFailureListener exception = $it")
            }
        return subject.hide()
    }
}