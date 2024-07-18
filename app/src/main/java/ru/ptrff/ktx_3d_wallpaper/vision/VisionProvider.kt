package ru.ptrff.ktx_3d_wallpaper.vision

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VisionProvider(private val updateCoordFunction: (Float, Float, Float) -> Unit) :
    FaceLandmarkerHelper.LandmarkerListener {

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var backgroundExecutor: ExecutorService

    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT


    fun start(context: Context, lifecycleOwner: LifecycleOwner) {
        Log.d(javaClass.name, "start")

        backgroundExecutor = Executors.newSingleThreadExecutor()

        setUpCamera(context, lifecycleOwner)

        backgroundExecutor.execute {
            faceLandmarkerHelper = FaceLandmarkerHelper(
                context = context,
                runningMode = RunningMode.LIVE_STREAM,
                faceLandmarkerHelperListener = this
            )
        }

    }


    private fun setUpCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    detectFace(image)
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e(javaClass.name, "Use case binding failed", exc)
        }
    }

    private fun detectFace(imageProxy: ImageProxy) {
        faceLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }


    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
        val leftEyeIndex = 468
        val rightEyeIndex = 473

        // use first face
        resultBundle.result.faceLandmarks()[0].let { face ->
            val leftEyePosition = face[leftEyeIndex]
            val rightEyePosition = face[rightEyeIndex]

            val imageWidth = resultBundle.inputImageWidth
            val imageHeight = resultBundle.inputImageHeight

            val centerX = imageWidth / 2f
            val centerY = imageHeight / 2f

            // relative positions from the center of the camera
            val leftEyeRelativeX = leftEyePosition.x() * imageWidth - centerX
            val leftEyeRelativeY = leftEyePosition.y() * imageHeight - centerY
            val rightEyeRelativeX = rightEyePosition.x() * imageWidth - centerX
            val rightEyeRelativeY = rightEyePosition.y() * imageHeight - centerY

            //center between eyes
            val centerBetweenEyesX = (leftEyeRelativeX + rightEyeRelativeX) / 2
            val centerBetweenEyesY = (leftEyeRelativeY + rightEyeRelativeY) / 2
            updateCoordFunction(centerBetweenEyesX, centerBetweenEyesY, leftEyePosition.z())
        }
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e(javaClass.name, "Error $errorCode: $error")
    }
}