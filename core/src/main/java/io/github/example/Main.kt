package io.github.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var grassTexture: Texture
    private lateinit var waterTexture: Texture
    private lateinit var mountainTexture: Texture
    private val pawns = mutableListOf<PlayerPawn>()
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
    private lateinit var wallTexture: Texture // Текстура стены
    private val walls = mutableListOf<Sprite>()
    private var isCreatingWall = false // Флаг для отслеживания режима создания стены
    private var previewWall: Sprite? = null
    private val tasks = mutableListOf<Task>()
    private lateinit var selectionProcessor: SelectionProcessor

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
        selectionProcessor = SelectionProcessor(camera)

        // Настройка InputMultiplexer для совместной обработки ввода
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(inputProcessor)
        inputMultiplexer.addProcessor(selectionProcessor)

        Gdx.input.inputProcessor = inputMultiplexer
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0f)
        camera.update()

        terrain = generateTerrain(60, 44)
        terrainTexture = FrameBuffer(Pixmap.Format.RGBA8888, terrain.size * TILE_SIZE, terrain[0].size * TILE_SIZE, false)

        renderTerrainToTexture()

        populateEntities(10)
        spawnTrees(20)

        createButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                isCreatingWall = true // Включаем режим создания стены
                previewWall = Sprite(wallTexture)
            }
        })
    }

    override fun render() {
        // Обновляем камеру
        inputProcessor.handleCameraMovement(Gdx.graphics.deltaTime)

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.projectionMatrix = camera.combined

        // Обновляем положение предварительной стены
        if (isCreatingWall && previewWall != null) {
            val mouseX = Gdx.input.x.toFloat()
            val mouseY = Gdx.graphics.height - Gdx.input.y.toFloat()
            previewWall!!.setPosition(mouseX - previewWall!!.width / 2, mouseY - previewWall!!.height / 2)

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                placeWall(previewWall!!.x + previewWall!!.width / 2, previewWall!!.y + previewWall!!.height / 2)
                isCreatingWall = false // Завершаем режим создания стены
                previewWall = null // Убираем предварительную стену
            }
        }

        batch.begin()
        renderTerrain()

        for (entity in pawns) {
            entity.sprite.setSize(25f, 30f)

            // Стремимся к последней установленной стене
            if (tasks.isNotEmpty()) {
                val targetTask = tasks[tasks.size - 1] // Получаем последнюю задачу
                moveEntityTowards(entity, targetTask)
            }

            entity.sprite.draw(batch)
        }

        for (tree in trees) {
            tree.sprite.setSize(50f, 60f)
            tree.sprite.draw(batch)
        }

        for (wall in walls) {
            wall.draw(batch)
        }

        previewWall?.draw(batch)

        batch.end()

        selectionProcessor.renderSelection()

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
        stage.dispose()
        wallTexture.dispose()
    }

    private fun initTexture() {

        wallTexture = Texture("wall.png")

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

    private fun worldToGridCoordinates(x: Float, y: Float): Pair<Int, Int> {
        val gridX = (x / TILE_SIZE).toInt()
        val gridY = (y / TILE_SIZE).toInt()
        return Pair(gridX, gridY)
    }

    private fun placeWall(x: Float, y: Float) {
        // Преобразуем мировые координаты в координаты клетки
        val (gridX, gridY) = worldToGridCoordinates(x, y)

        // Рассчитываем мировые координаты верхнего левого угла клетки
        val positionX = gridX * TILE_SIZE
        val positionY = gridY * TILE_SIZE

        // Создаем спрайт стены и устанавливаем его позицию
        val wallSprite = Sprite(wallTexture).apply {
            setSize(TILE_SIZE.toFloat(), TILE_SIZE.toFloat()) // Задайте размер стены
            setPosition(positionX.toFloat(), positionY.toFloat()) // Устанавливаем на сетку
        }
        walls.add(wallSprite) // Добавляем спрайт стены в список

        // Добавляем задачу для перемещения сущностей к стене
        tasks.add(Task(positionX + TILE_SIZE / 2f, positionY + TILE_SIZE / 2f, 0)) // Центрируем задачу
    }

    private fun populateEntities(count: Int) {
        for (n in 0 until count) {
            val entitySprite = Sprite(Texture("human.png"))
            entitySprite.x = Random.nextInt(0, 60 * TILE_SIZE).toFloat()
            entitySprite.y = Random.nextInt(0, 44 * TILE_SIZE).toFloat()
            pawns.add(PlayerPawn(n, n + 10, 0, entitySprite, batch, 0, 0, 0, null))
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

    private fun moveEntityTowards(entity: Entity, task: Task) {
        val speed = 100f * Gdx.graphics.deltaTime // Скорость движения

        // Получаем координаты сетки цели
        val (gridX, gridY) = worldToGridCoordinates(task.x, task.y)

        // Переводим координаты сетки обратно в мировые координаты
        val targetX = gridX * TILE_SIZE
        val targetY = gridY * TILE_SIZE

        val dx = targetX - entity.sprite.x
        val dy = targetY - entity.sprite.y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance > 1f) { // Если сущность не достигла цели
            // Нормализуем вектор и умножаем на скорость
            entity.sprite.x += (dx / distance) * speed
            entity.sprite.y += (dy / distance) * speed
        } else {
            // Если сущность близка к цели, установить её на точную позицию
            entity.sprite.x = targetX.toFloat()
            entity.sprite.y = targetY.toFloat()
        }
    }

    private fun createWallsInSelection(start: Vector2, end: Vector2) {
        // Получаем координаты сетки для начала и конца выделения
        val topLeft = worldToGridCoordinates(start.x, start.y)
        val bottomRight = worldToGridCoordinates(end.x, end.y)

        // Итерируемся по всей области выделения и создаем стены
        for (gridX in topLeft.first..bottomRight.first) {
            for (gridY in topLeft.second..bottomRight.second) {
                // Изменяем позицию, чтобы установить стену по сетке
                val wallPositionX = gridX * TILE_SIZE
                val wallPositionY = gridY * TILE_SIZE
                placeWall(wallPositionX.toFloat(), wallPositionY.toFloat())
            }
        }
    }

}
