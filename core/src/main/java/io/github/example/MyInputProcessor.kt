package io.github.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera

class MyInputProcessor(private val camera: OrthographicCamera) : InputAdapter() {
    private val zoomFactor = 0.1f

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        // Изменение зума камеры
        camera.zoom += zoomFactor * amountY
        camera.zoom = camera.zoom.coerceIn(0.1f, 2f) // Ограничение зума
        return true
    }

    fun handleCameraMovement(deltaTime: Float) {
        val speed = 200f * deltaTime // Скорость движения камеры

        // Движение камеры с клавишами (WASD)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0f, speed) // Движение вверх
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0f, -speed) // Движение вниз
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-speed, 0f) // Движение влево
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(speed, 0f) // Движение вправо
        }

        camera.update() // Обновляем камеру
    }

    // Остальные методы InputProcessor (можно оставить пустыми, если не нужны)
    override fun keyDown(keycode: Int): Boolean = false
    override fun keyUp(keycode: Int): Boolean = false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchCancelled(p0: Int, p1: Int, p2: Int, p3: Int): Boolean = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
    override fun keyTyped(character: Char): Boolean = false
}
