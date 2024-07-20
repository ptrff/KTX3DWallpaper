package ru.ptrff.ktx_3d_wallpaper.wallpaper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.scenes.scene2d.ui.Label

class WallpaperScene : Screen {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var assets: AssetManager
    private lateinit var modelInstance: ModelInstance
    private lateinit var label: Label
    private lateinit var spriteBatch: SpriteBatch

    private val pid = PIDController(0.1f, 0f, 0f)
    var facePosition = Triple(0f, 0f, 0f)


    override fun show() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.lookAt(0f, 0f, 0f)
        camera.near = 1f
        camera.far = 300f

        modelBatch = ModelBatch()
        spriteBatch = SpriteBatch()

        environment = Environment()
        val dirLight = DirectionalLight().set(Color.WHITE, -1f, -0.75f, -1f)
        environment.add(dirLight)

        // label
        label = Label("", Label.LabelStyle(BitmapFont(), Color.RED))
        label.setPosition(20f, 20f)
        label.fontScaleX = 2f
        label.fontScaleY = 2f

        //cube
        val modelBuilder = ModelBuilder()
        val model = modelBuilder.createBox(
            2f, 2f, 2f,
            Material(ColorAttribute.createDiffuse(Color.GREEN)),
            (Usage.Position or Usage.Normal).toLong()
        )

        modelInstance = ModelInstance(model)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glClearColor(1f, 0.5f, 0f, 1f)
        move(delta)
        camera.update()

        modelBatch.begin(camera)
        modelBatch.render(modelInstance, environment)
        modelBatch.end()

        spriteBatch.begin()
        label.draw(spriteBatch, 1f)
        spriteBatch.end()
    }

    private fun move(delta: Float) {
        val currentPos = camera.position
        if (delta <= 0) return

        val computed = pid.compute(
            facePosition.first, currentPos.x,
            -facePosition.second, currentPos.y,
            facePosition.third, currentPos.z,
            delta
        )

        camera.position.set(computed.first, computed.second, computed.third)
        label.setText("(${currentPos.x}, ${currentPos.y}, ${currentPos.z}) ${Gdx.graphics.framesPerSecond}")
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
