package io.github.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import java.sql.Time
import java.time.LocalTime
import javax.management.timer.Timer
import kotlin.random.Random

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var grassTexture: Texture
    private lateinit var waterTexture: Texture
    private lateinit var mountainTexture: Texture
    private val entities = mutableListOf<Entity>()
    private val trees = mutableListOf<Object>()
    private lateinit var terrain: Array<IntArray>
    private lateinit var terrainTexture: FrameBuffer
    private lateinit var camera: OrthographicCamera
    private lateinit var inputProcessor: MyInputProcessor
    private lateinit var stage: Stage
    private lateinit var toolsButton: ImageButton
    private lateinit var toolsButtonNormal: Texture
    private lateinit var toolsButtonHover: Texture
    private lateinit var toolsButtonPressed: Texture
    private lateinit var createButton: ImageButton
    private lateinit var createButtonNormal: Texture
    private lateinit var createButtonHover: Texture
    private lateinit var createButtonPressed: Texture

    private val TILE_SIZE = 16
    private val TERRAIN_GRASS = 0
    private val TERRAIN_WATER = 1
    private val TERRAIN_FOREST = 2
    private val TERRAIN_MOUNTAIN = 3

    // Параметры для Перлин-шума
    private var noiseScale = Random.nextFloat() - 0.4f
    private val octaves = 3
    private val persistence = 0.9f

    override fun create() {
        batch = SpriteBatch()
        stage = Stage()

        initTexture()

        addButtons()

        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        inputProcessor = MyInputProcessor(camera)

        // Настройка InputMultiplexer для совместной обработки ввода
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(inputProcessor)

        Gdx.input.inputProcessor = inputMultiplexer
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0f)
        camera.update()

        terrain = generateTerrain(60, 44)
        terrainTexture = FrameBuffer(Pixmap.Format.RGBA8888, terrain.size * TILE_SIZE, terrain[0].size * TILE_SIZE, false)

        renderTerrainToTexture()

        populateEntities(10)
        spawnTrees(20)
    }

    private fun initTexture() {
        grassTexture = Texture("grass.png")
        waterTexture = Texture("water.png")
        mountainTexture = Texture("sand.png") // TODO замени потом текстуру

        toolsButtonNormal = Texture("buttons/tools_Button.png")
        toolsButtonHover = Texture("buttons/tools_Button_hover.png")
        toolsButtonPressed = Texture("buttons/tools_Button_pressed.png")

        createButtonNormal = Texture("buttons/create_Button.png")
        createButtonHover = Texture("buttons/create_Button_hover.png")
        createButtonPressed = Texture("buttons/create_Button_pressed.png")
    }

    private fun addButtons() {
        val toolsNormalDrawable = TextureRegionDrawable(TextureRegion(toolsButtonNormal))
        val toolsHoverDrawable = TextureRegionDrawable(TextureRegion(toolsButtonHover))
        val toolsPressedDrawable = TextureRegionDrawable(TextureRegion(toolsButtonPressed))

        val createNormalDrawable = TextureRegionDrawable(TextureRegion(createButtonNormal))
        val createHoverDrawable = TextureRegionDrawable(TextureRegion(createButtonHover))
        val createPressedDrawable = TextureRegionDrawable(TextureRegion(createButtonPressed))

        // Создаём стиль кнопки
        val toolsButtonStyle = ImageButton.ImageButtonStyle()
        toolsButtonStyle.up = toolsNormalDrawable
        toolsButtonStyle.over = toolsHoverDrawable
        toolsButtonStyle.down = toolsPressedDrawable

        val createButtonStyle = ImageButton.ImageButtonStyle()
        createButtonStyle.up = createNormalDrawable
        createButtonStyle.over = createHoverDrawable
        createButtonStyle.down = createPressedDrawable

        // Создаём кнопку и устанавливаем стиль
        toolsButton = ImageButton(toolsButtonStyle)
        toolsButton.setSize(38f, 38f) // Устанавливаем размер
        toolsButton.setPosition(10f, 10f) // Устанавливаем позицию в левом нижнем углу

        createButton = ImageButton(createButtonStyle)
        createButton.setSize(38f, 38f) // Устанавливаем размер
        createButton.setPosition(60f, 10f) // Устанавливаем позицию в левом нижнем углу

        // Добавляем кнопку на сцену
        stage.addActor(toolsButton)
        stage.addActor(createButton)
    }

    override fun render() {
        // Обновляем камеру
        inputProcessor.handleCameraMovement(Gdx.graphics.deltaTime)

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.projectionMatrix = camera.combined

        batch.begin()
        renderTerrain()
        handleInput()

        for (entity in entities) {
            entity.sprite.setSize(50f, 60f)
            entity.sprite.draw(batch)
        }

        for (tree in trees) {
            tree.sprite.setSize(50f, 60f)
            tree.sprite.draw(batch)
        }

        batch.end()

        // Обновляем сцену UI
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun dispose() {
        batch.dispose()
        grassTexture.dispose()
        waterTexture.dispose()
        mountainTexture.dispose()
        toolsButtonNormal.dispose()
        toolsButtonHover.dispose()
        toolsButtonPressed.dispose()
    }

    private fun populateEntities(count: Int) {
        for (n in 0 until count) {
            val entitySprite = Sprite(Texture("human.png"))
            entitySprite.x = Random.nextInt(0, 60 * TILE_SIZE).toFloat()
            entitySprite.y = Random.nextInt(0, 44 * TILE_SIZE).toFloat()
            entities.add(Entity(n, n + 10, entitySprite, batch))
        }
    }

    private fun spawnTrees(count: Int) {
        for (n in 0 until count) {
            val entitySprite = Sprite(Texture("tree.png"))
            entitySprite.x = Random.nextInt(0, 60 * TILE_SIZE).toFloat()
            entitySprite.y = Random.nextInt(0, 44 * TILE_SIZE).toFloat()
            trees.add(Object(n, n + 10, entitySprite, batch))
        }
    }

    private fun generateTerrain(width: Int, height: Int): Array<IntArray> {
        val terrain = Array(width) { IntArray(height) }

        for (x in 0 until width) {
            for (y in 0 until height) {
                val noiseValue = perlinNoise(x * noiseScale, y * noiseScale)

                // Преобразуем шумовое значение в типы местности
                terrain[x][y] = when {
                    noiseValue < -0.05 -> TERRAIN_WATER
                    noiseValue < 0.2 -> TERRAIN_GRASS
                    else -> TERRAIN_MOUNTAIN
                }
            }
        }
        return terrain
    }

    private fun perlinNoise(x: Float, y: Float): Float {
        // Функция для создания многослойного Перлин-шума
        var total = 0.5f
        var frequency = 1f
        var amplitude = 1f
        var maxValue = 2.5f

        for (i in 0 until octaves) {
            total += noise(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= persistence
            frequency *= 2f
        }

        return total / maxValue
    }

    private fun noise(x: Float, y: Float): Float {
        return ((Math.sin(x.toDouble()) + Math.cos(y.toDouble())) / 2).toFloat()
    }

    private fun renderTerrainToTexture() {
        terrainTexture.begin()
        batch.begin()

        for (x in terrain.indices) {
            for (y in terrain[x].indices) {
                when (terrain[x][y]) {
                    TERRAIN_GRASS -> drawTile(grassTexture, x, y)
                    TERRAIN_WATER -> drawTile(waterTexture, x, y)
                    TERRAIN_MOUNTAIN -> drawTile(mountainTexture, x, y)
                }
            }
        }

        batch.end()
        terrainTexture.end()
    }

    private fun renderTerrain() {
        batch.draw(terrainTexture.colorBufferTexture, 0f, 0f, terrainTexture.width.toFloat(), terrainTexture.height.toFloat())
    }

    private fun drawTile(texture: Texture, x: Int, y: Int) {
        batch.draw(texture, x * TILE_SIZE.toFloat(), y * TILE_SIZE.toFloat(), TILE_SIZE.toFloat(), TILE_SIZE.toFloat())
    }

    private fun handleInput() {
        val speed = 60f
        val delta = Gdx.graphics.deltaTime

        val cursorX = Gdx.input.x
        val cursorY = Gdx.graphics.height - Gdx.input.y

        for (entity in entities) {
            // Передвижение по оси X
            if (cursorX != entity.sprite.x.toInt()) {
                if (cursorX > entity.sprite.x.toInt()) {
                    entity.sprite.translateX(speed * delta)
                } else {
                    entity.sprite.translateX(-speed * delta)
                }
            }

            // Передвижение по оси Y
            if (cursorY != entity.sprite.y.toInt()) {
                if (cursorY > entity.sprite.y.toInt()) {
                    entity.sprite.translateY(speed * delta)
                } else {
                    entity.sprite.translateY(-speed * delta)
                }
            }
        }
    }
}
