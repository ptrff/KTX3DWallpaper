package ru.ptrff.ktx_3d_wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.ptrff.ktx_3d_wallpaper.databinding.ActivityMainBinding
import ru.ptrff.ktx_3d_wallpaper.vision.VisionProvider
import ru.ptrff.ktx_3d_wallpaper.wallpaper.WallpaperService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setupWallpaper.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, WallpaperService::class.java)
            )
            startActivity(intent)
        }

        binding.startWatchingFace.setOnClickListener {
            val visProvider = VisionProvider { x, y, z ->
                runOnUiThread {
                    binding.eyeCoords.text = "Face coords: (${x.toInt()}, ${y.toInt()}, ${z.toInt()})"
                    binding.eyeCoords.translationX = x
                    binding.eyeCoords.translationY = y
                }
            }
            visProvider.start(this, this)
        }
    }

}