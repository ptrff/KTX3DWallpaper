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
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Label
import kotlin.random.Random

class WallpaperScene : Screen {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var assets: AssetManager
    private lateinit var modelInstances: List<ModelInstance>
    private lateinit var label: Label
    private lateinit var spriteBatch: SpriteBatch

    private val pid = PIDController(0.1f, 0f, 0f)
    var facePosition = Triple(0f, 0f, 10f)


    override fun show() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.lookAt(0f, 0f, 0f)
        camera.near = 1f
        camera.far = 300f

        modelBatch = ModelBatch()
        spriteBatch = SpriteBatch()

        environment = Environment().apply {
            add(DirectionalLight().set(Color.WHITE, Vector3(-0.6f, -0.3f, -0.65f)))
            add(DirectionalLight().set(Color.WHITE, Vector3(0.3f, 0.7f, 0.4f)))
        }

        // label
        label = Label("", Label.LabelStyle(BitmapFont(), Color.RED))
        label.setPosition(20f, 20f)
        label.fontScaleX = 2f
        label.fontScaleY = 2f

        // init models
        createModels()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glClearColor(1f, 0.5f, 0f, 1f)
        move(delta)
        camera.update()

        modelBatch.begin(camera)
        modelInstances.forEach { modelBatch.render(it, environment) }
        modelBatch.end()

        spriteBatch.begin()
        label.draw(spriteBatch, 1f)
        spriteBatch.end()
    }

    private fun move(delta: Float) {
        val computed = pid.compute(
            facePosition.first * 2, camera.position.x,
            -facePosition.second * 2 + 2, camera.position.y,
            facePosition.third * 3 + 70, camera.position.z,
            delta
        )

        camera.position.set(computed.first, computed.second, computed.third)
        label.setText("(${camera.position.x}, ${camera.position.y}, ${camera.position.z}) ${Gdx.graphics.framesPerSecond}")
    }

    private fun createModels() {
        val modelBuilder = ModelBuilder()
        val instances = mutableListOf<ModelInstance>()

        val cubeModel = modelBuilder.createBox(
            5f, 5f, 20f,
            Material(ColorAttribute.createDiffuse(Color.CHARTREUSE)),
            (Usage.Position or Usage.Normal).toLong()
        )

        val planeModel = modelBuilder.createBox(
            50f, 100f, 1f,
            Material(ColorAttribute.createDiffuse(Color.GRAY)),
            (Usage.Position or Usage.Normal).toLong()
        )

        val wallModel = modelBuilder.createBox(
            0.1f, 100f, 500f,
            Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            (Usage.Position or Usage.Normal).toLong()
        )

        // abstract statue
        for (y in 0..2) {
            for (x in 0..(2 - y)) {
                val instance = ModelInstance(cubeModel).apply {
                    transform.setToTranslation(
                        x * 5f * Random.nextInt(-2, 2).toFloat(),
                        x * 5f * Random.nextInt(-2, 2).toFloat(),
                        y * 20f
                    )
                    transform.rotate(Vector3.Z, Random.nextInt(1, 180).toFloat())
                    transform.rotate(Vector3.Y, Random.nextInt(-45, 45).toFloat())
                    transform.rotate(Vector3.X, Random.nextInt(-15, 15).toFloat())
                }
                instances.add(instance)
            }
        }
        instances.add(ModelInstance(planeModel))

        val wall1 = ModelInstance(wallModel).apply {
            transform.setToTranslation(25f, 0f, 0f)
        }
        instances.add(wall1)

        val wall2 = ModelInstance(wallModel).apply {
            transform.setToTranslation(-25f, 0f, 0f)
        }
        instances.add(wall2)

        val wall3 = ModelInstance(wallModel).apply {
            transform.setToTranslation(0f, 50f, 0f)
            transform.rotate(Vector3.Z, 90f)
        }
        instances.add(wall3)

        val wall4 = ModelInstance(wallModel).apply {
            transform.setToTranslation(0f, -50f, 0f)
            transform.rotate(Vector3.Z, 90f)
        }
        instances.add(wall4)

        modelInstances = instances
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
        modelInstances.forEach { it.model.dispose() }
        modelBatch.dispose()
        spriteBatch.dispose()
    }
}
