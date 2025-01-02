package io.github.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var image: Texture
    private val entities = mutableListOf<Entity>()

    override fun create() {
        batch = SpriteBatch()
        image = Texture("human.png")
        val sprite = Sprite(image)

        for (n in 0..10) {
            val entitySprite = Sprite(image)
            entitySprite.x = Random.nextInt(0, 940).toFloat()
            entitySprite.y = Random.nextInt(0, 680).toFloat()
            entities.add(Entity(n, n + 10, entitySprite, batch))
        }
    }

    override fun render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.begin()
        handleInput()

        for (entity in entities) {
            entity.sprite.setSize(50f, 60f)
            entity.sprite.draw(batch)
        }

        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        image.dispose()
    }

    private fun handleInput() {
        val speed = 60f
        val delta = Gdx.graphics.deltaTime

        val cursorX = Gdx.input.x
        val cursorY = Gdx.graphics.height - Gdx.input.y

        for (entity in entities){
            // передвижение по оси x
            if (cursorX != entity.sprite.x.toInt()){
                if (cursorX > entity.sprite.x.toInt()){
                    entity.sprite.translateX(speed * delta)
                } else{
                    entity.sprite.translateX(-speed * delta)
                }
            }

            // передвижение по оси y
            if (cursorY != entity.sprite.y.toInt()){
                println(cursorY)
                println(entity.sprite.y.toInt())
                if (cursorY > entity.sprite.y.toInt()){
                    entity.sprite.translateY(speed * delta)
                } else{
                    entity.sprite.translateY(-speed * delta)
                }
            }
        }
    }
}
