package io.github.example

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class PlayerPawn(
    id: Int = DEFAULT_ID,
    health: Int,
    hunger: Int,
    sprite: Sprite,
    batch: SpriteBatch,
    var mentalWork: Int,
    var building: Int,
    var combatSkills: Int
) : Entity(
    id, health,hunger, sprite, batch
) {


}
