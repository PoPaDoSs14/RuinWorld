package io.github.example

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor

class Entity(
    var id: Int = DEFAULT_ID,
    var health: Int,
    val sprite: Sprite,
    val batch: SpriteBatch
): Actor() {

    companion object{
        const val DEFAULT_ID = 0
    }
}
