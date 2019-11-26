package com.dalvinlabs.guidedog.objectdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

internal class Marker(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var rect: Rect? = null
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        rect?.let {
            canvas?.drawRect(it, paint)
        }
    }
}