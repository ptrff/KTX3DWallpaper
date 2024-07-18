package ru.ptrff.ktx_3d_wallpaper.wallpaper

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen

class WallpaperScreen(private val engine: Screen) : Game() {

    override fun create() {
        setScreen(engine)
    }
}