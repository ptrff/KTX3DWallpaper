package ru.ptrff.ktx_3d_wallpaper

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService

class WallpaperService : AndroidLiveWallpaperService() {

    override fun onCreateApplication() {
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

        initialize(WallpaperScreen(WallpaperScene()), config)
    }
}
