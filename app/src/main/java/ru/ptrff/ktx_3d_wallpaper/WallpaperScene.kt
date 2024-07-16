package ru.ptrff.ktx_3d_wallpaper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight

class WallpaperScene : Screen {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var assets: AssetManager
    private lateinit var modelInstance: ModelInstance

    override fun show() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(10f, 10f, 10f)
        camera.lookAt(0f, 0f, 0f)
        camera.near = 1f
        camera.far = 300f

        modelBatch = ModelBatch()

        environment = Environment()

        environment = Environment()
        val dirLight = DirectionalLight().set(Color.WHITE, -1f, -0.75f, -1f)
        environment.add(dirLight)

        assets = AssetManager()
        assets.load("bread.obj", Model::class.java)
        assets.load("textures/Dif_dark durum bread highpoly.jpg", Texture::class.java)
        assets.finishLoading()

        val model: Model = assets.get("bread.obj")

        val texture1 = assets.get("textures/Dif_dark durum bread highpoly.jpg", Texture::class.java)

        for (material in model.materials) {
            material.set(TextureAttribute.createDiffuse(texture1))
        }

        modelInstance = ModelInstance(model)
    }

    override fun render(delta: Float) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        camera.update()

        modelBatch.begin(camera)
        modelBatch.render(modelInstance, environment)
        modelBatch.end()

        modelInstance.transform.rotate(0f, 1f, 0f, 1f)
    }

    override fun resize(width: Int, height: Int) {
        // not needed
    }

    override fun pause() {
        // not needed
    }

    override fun resume() {
        // not needed
    }

    override fun hide() {
        // not needed
    }

    override fun dispose() {
        modelBatch.dispose()
    }
}
