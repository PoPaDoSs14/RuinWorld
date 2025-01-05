package io.github.example

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3

class SelectionProcessor(private val camera: OrthographicCamera) : InputAdapter() {
    private var selectionStart: Vector3? = null
    private var selectionEnd: Vector3? = null
    private var isSelecting = false
    private val shapeRenderer = ShapeRenderer()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == Input.Buttons.LEFT) {
            selectionStart = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
            camera.unproject(selectionStart)
            isSelecting = true
        }
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (isSelecting) {
            selectionEnd = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
            camera.unproject(selectionEnd)
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (isSelecting) {
            selectionEnd = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
            camera.unproject(selectionEnd)
            isSelecting = false
            handleSelection(selectionStart!!, selectionEnd!!)
        }
        return true
    }

    fun renderSelection() {
        shapeRenderer.projectionMatrix = camera.combined // Установка матрицы проекции

        if (isSelecting && selectionStart != null && selectionEnd != null) {
            val x = Math.min(selectionStart!!.x, selectionEnd!!.x)
            val y = Math.min(selectionStart!!.y, selectionEnd!!.y)
            val width = Math.abs(selectionStart!!.x - selectionEnd!!.x)
            val height = Math.abs(selectionStart!!.y - selectionEnd!!.y)

            // Рисуем рамку вокруг выделенной области
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color(0f, 0f, 1f, 1f) // Цвет рамки
            shapeRenderer.line(x, y, x + width, y) // Верхняя линия
            shapeRenderer.line(x + width, y, x + width, y + height) // Правая линия
            shapeRenderer.line(x + width, y + height, x, y + height) // Нижняя линия
            shapeRenderer.line(x, y + height, x, y) // Левая линия
            shapeRenderer.end()
        }
    }

    private fun handleSelection(start: Vector3, end: Vector3) {
        // Логика обработки выделенных объектов
    }
}
