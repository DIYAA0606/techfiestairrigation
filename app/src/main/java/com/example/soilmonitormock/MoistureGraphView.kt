package com.example.soilmonitormock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class MoistureGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<Int> = emptyList()

    private val linePaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = Color.parseColor("#1B5E20")
        strokeWidth = 10f
        isAntiAlias = true
    }

    fun setData(values: List<Int>) {
        data = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.size < 2) return
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), linePaint)


        val widthStep = width.toFloat() / (data.size - 1)
        val maxVal = 100f

        var prevX = 0f
        var prevY = height - (data[0] / maxVal) * height

        for (i in 1 until data.size) {
            val x = i * widthStep
            val y = height - (data[i] / maxVal) * height

            canvas.drawLine(prevX, prevY, x, y, linePaint)
            canvas.drawCircle(prevX, prevY, 6f, pointPaint)

            prevX = x
            prevY = y
        }

        canvas.drawCircle(prevX, prevY, 6f, pointPaint)
    }
}
