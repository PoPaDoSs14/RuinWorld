package io.github.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var sprite: Sprite
    private lateinit var image: Texture
    private lateinit var entity: Entity

    override fun create() {
        batch = SpriteBatch()
        image = Texture("human.png")
        sprite = Sprite(image)
        entity = Entity(0,100, sprite)
    }

    override fun render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.begin()
        handleInput()
        entity.sprite.setSize(50f, 60f)
        entity.sprite.draw(batch)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        image.dispose()
    }

    private fun handleInput() {
        val speed = 40f
        val delta = Gdx.graphics.deltaTime

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            entity.sprite.translateX(speed * delta)
        }
    }
}
