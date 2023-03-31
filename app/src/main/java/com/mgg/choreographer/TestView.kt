package com.mgg.choreographer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

class TestView : View , Choreographer.FrameCallback{

    private var paint: Paint? = null
    private var path: Path? = null
    private var startX = 0f
    private var startY = 0f
    private var lastFrameTime: Long = 0L
    private var frames = 0

    private var width = 0
    private var height = 0


    private var intervalOfExplosionPoints = 16f
    private var numberOfExplosionPoints = 16f
    private var drawHeightCount = 0

    private var widthCount = 0f
    private var heightCount = 0f

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        paint?.style = Paint.Style.STROKE
        paint?.strokeWidth = 5f
        paint?.color = Color.RED
        path = Path()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                path?.moveTo(startX, startY)
            }
            MotionEvent.ACTION_MOVE -> {
                val endX: Float = event.x
                val endY: Float = event.y
                path?.lineTo(endX, endY)
            }
            MotionEvent.ACTION_UP -> {
                val x = event.x
                val y = event.y
                path?.moveTo(x, y)
            }
        }
        return true
    }

    private fun mockTouchEvent() {
        val downTime: Long = SystemClock.uptimeMillis()
        val eventTime: Long = SystemClock.uptimeMillis()

        val motionEventDown: MotionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            startX,
            startX,
            0
        )
        dispatchTouchEvent(motionEventDown)
        motionEventDown.recycle()

        widthCount = width.div(intervalOfExplosionPoints)
        heightCount = (height - numberOfExplosionPoints * intervalOfExplosionPoints).div(intervalOfExplosionPoints)
        if (numberOfExplosionPoints > widthCount) {
            heightCount = height.div(intervalOfExplosionPoints)
            for (number in 0 until numberOfExplosionPoints.toInt()) {
                val x: Float = startX + width * (number/numberOfExplosionPoints)
                val y: Float = startY + drawHeightCount * intervalOfExplosionPoints
                val metaState = 0
                val motionEvent: MotionEvent = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    x,
                    y,
                    metaState
                )
                dispatchTouchEvent(motionEvent)
                motionEvent.recycle()
            }
        } else {
            for (number in 0 until numberOfExplosionPoints.toInt()) {
                val x: Float = startX + width * (number/numberOfExplosionPoints)
                val y: Float = startY + intervalOfExplosionPoints * number +
                        drawHeightCount * intervalOfExplosionPoints
                val metaState = 0
                val motionEvent: MotionEvent = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    x,
                    y,
                    metaState
                )
                dispatchTouchEvent(motionEvent)
                motionEvent.recycle()
            }
        }
        drawHeightCount ++

        if (drawHeightCount >= heightCount) {
            drawHeightCount = 0
            clearPath()
        }

        val motionEventUp: MotionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            0f,
            0f,
            0
        )
        dispatchTouchEvent(motionEventUp)
        motionEventUp.recycle()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTime != 0L) {
            val frameTimeMillis: Long = (frameTimeNanos - lastFrameTime) / 1000000
            frames++
            if (frameTimeMillis > 16) { // 16ms = 60fps
                Log.d("FPS", "Frames: $frames" + ", Time: " + frameTimeMillis + "ms")
                frames = 0
            }
        }
        lastFrameTime = frameTimeNanos
        mockTouchEvent() // 模拟移动事件
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path?.let { path->
            paint?.let {paint->
                canvas.drawPath(path, paint)
            }
        }
    }

    fun clearPath(){
        path?.reset()
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@TestView.viewTreeObserver.removeOnGlobalLayoutListener(this);
                width = measuredWidth
                height = measuredHeight
                Choreographer.getInstance().postFrameCallback(this@TestView)
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(this)
    }
}