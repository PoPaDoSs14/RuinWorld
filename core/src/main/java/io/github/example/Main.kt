package io.github.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var grassTexture: Texture
    private lateinit var waterTexture: Texture
    private val entities = mutableListOf<Entity>()
    private lateinit var terrain: Array<IntArray>
    private lateinit var terrainTexture: FrameBuffer

    private val TILE_SIZE = 32
    private val TERRAIN_GRASS = 0
    private val TERRAIN_WATER = 1
    private val TERRAIN_FOREST = 2
    private val TERRAIN_MOUNTAIN = 3

    override fun create() {
        batch = SpriteBatch()
        grassTexture = Texture("grass.png")
        waterTexture = Texture("water.png")

        terrain = generateTerrain(30, 22)
        terrainTexture = FrameBuffer(Pixmap.Format.RGBA8888, terrain.size * TILE_SIZE, terrain[0].size * TILE_SIZE, false)

        renderTerrainToTexture()

        for (n in 0..10) {
            val entitySprite = Sprite(Texture("human.png"))
            entitySprite.x = Random.nextInt(0, 940).toFloat()
            entitySprite.y = Random.nextInt(0, 680).toFloat()
            entities.add(Entity(n, n + 10, entitySprite, batch))
        }
    }

    override fun render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.begin()
        renderTerrain()
        handleInput()

        for (entity in entities) {
            entity.sprite.setSize(50f, 60f)
            entity.sprite.draw(batch)
        }

        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        grassTexture.dispose()
        waterTexture.dispose()
    }

    private fun generateTerrain(width: Int, height: Int): Array<IntArray> {
        val terrain = Array(width) { IntArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                terrain[x][y] = when (Random.nextInt(0, 2)) {
                    0 -> TERRAIN_GRASS
                    1 -> TERRAIN_WATER
                    else -> TERRAIN_FOREST
                }
            }
        }
        return terrain
    }

    private fun renderTerrainToTexture() {
        terrainTexture.begin()
        batch.begin()

        for (x in terrain.indices) {
            for (y in terrain[x].indices) {
                when (terrain[x][y]) {
                    TERRAIN_GRASS -> drawTile(grassTexture, x, y)
                    TERRAIN_WATER -> drawTile(waterTexture, x, y)
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
