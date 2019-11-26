package com.dalvinlabs.guidedog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dalvinlabs.guidedog.objectdetection.Marker
import com.dalvinlabs.guidedog.objectdetection.Repository
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 100
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private val repository = Repository()
    private lateinit var marker: Marker

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraView = findViewById<TextureView>(R.id.camera_view)

        // Preview use case, provide preview to show on screen.
        val previewConfig = PreviewConfig.Builder().build()
        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val parent = cameraView.parent as ViewGroup
            parent.removeView(cameraView)
            parent.addView(cameraView, 0)
            cameraView.surfaceTexture = it.surfaceTexture
        }

        // Analyze use case, provide image frame for further analysis
        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setTargetResolution(Size(1280, 720))
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { imageProxy, rotationDegrees ->
                Log.d(tag, "image = ${imageProxy}, rotationDegrees = $rotationDegrees")
                val image = imageProxy.image ?: return@Analyzer
                repository.startDetection(image, rotationDegrees).subscribe {
                    marker.rect = it
                }
            }
        )

        CameraX.bindToLifecycle(this, preview, imageAnalysis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marker = findViewById(R.id.marker)
        setSupportActionBar(toolbar)
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Snackbar.make(
                    window.decorView.rootView,
                    "Permission is needed to perform work",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Action", null).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
