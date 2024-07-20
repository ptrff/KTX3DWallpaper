package ru.ptrff.ktx_3d_wallpaper.vision

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.tan

class VisionProvider(private val updateCoordFunction: (Float, Float, Float) -> Unit) :
    FaceLandmarkerHelper.LandmarkerListener {

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var backgroundExecutor: ExecutorService

    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private var focalLength = 1f
    private var sensorX = 0f
    private var sensorY = 0f
    private var angleX = 0f
    private var angleY = 0f

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
                bindCameraUseCases(context, lifecycleOwner)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases(context: Context, lifecycleOwner: LifecycleOwner) {
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

            calculateCameraParameters(context)

        } catch (exc: Exception) {
            Log.e(javaClass.name, "Use case binding failed", exc)
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun calculateCameraParameters(context: Context) {
        camera ?: return
        val camera2CameraInfo = Camera2CameraInfo.from(camera!!.cameraInfo)
        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = camera2CameraInfo.cameraId
        val chars = cameraManager.getCameraCharacteristics(cameraId)

        focalLength =
            chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.get(0) ?: 0f

        val sensorSize =
            chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

        angleX = toDegrees(2 * tan((sensorSize!!.width / (2 * focalLength)).toDouble())).toFloat()
        angleY = toDegrees(2 * tan((sensorSize.height / (2 * focalLength)).toDouble())).toFloat()
        sensorX = (tan(toRadians((angleX / 2).toDouble())) * 2 * focalLength).toFloat()
        sensorY = (tan(toRadians((angleY / 2).toDouble())) * 2 * focalLength).toFloat()
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

            val deltaX = abs(leftEyeRelativeX - rightEyeRelativeX)
            val deltaY = abs(leftEyeRelativeY - rightEyeRelativeY)

            // calculate distance from screen
            val ipd = 64 // distance between the eyes in mm
            val distance: Float = if (deltaX >= deltaY) {
                focalLength * (ipd / sensorX) * (imageWidth / deltaX)
            } else {
                focalLength * (ipd / sensorY) * (imageHeight / deltaY)
            }

            updateCoordFunction(centerBetweenEyesX, centerBetweenEyesY, distance)
        }
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e(javaClass.name, "Error $errorCode: $error")
    }
}