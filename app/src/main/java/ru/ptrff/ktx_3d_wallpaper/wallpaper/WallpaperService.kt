package ru.ptrff.ktx_3d_wallpaper.wallpaper

import android.content.Intent
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService
import ru.ptrff.ktx_3d_wallpaper.vision.VisionProvider

class WallpaperService : AndroidLiveWallpaperService(), LifecycleOwner {

    private val dispatcher = ServiceLifecycleDispatcher(this)

    @CallSuper
    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    override fun onCreateApplication() {
        dispatcher.onServicePreSuperOnBind()
        super.onCreateApplication()
        init()
    }

    private fun init() {
        val config = AndroidApplicationConfiguration().apply {
            useCompass = false
            useAccelerometer = false
            getTouchEventsForLiveWallpaper = false
            useGyroscope = false
            useWakelock = false
        }


        val wallpaperScene = WallpaperScene()

        VisionProvider { x, y, z ->
            println("($x, $y, $z)")
            wallpaperScene.updateFacePosition(x/50, y/50, z)
        }.start(baseContext, this)

        initialize(WallpaperScreen(wallpaperScene), config)


    }
}
