package com.braincrafttask.image_eraser.view

import android.widget.ImageView
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import com.braincrafttask.image_eraser.R

class MagnifyView: ImageView {

    private val CIRCLE_RADIUS = context.resources.displayMetrics.density * 80f
    private val START_POS = resources.displayMetrics.density * 10f

    private var mPaint: Paint = Paint().apply {
        color = Color.argb(255, 255, 245, 238)
        strokeWidth = resources.displayMetrics.density * 10f
        style = Paint.Style.STROKE
    }

    lateinit var bitmap: Bitmap

    private var mTranslatePoint = PointF()

    private var mMatrix = Matrix()

    private var mBGBmp: BitmapDrawable =
        BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.brush_view_bg)).apply {
            setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, -1)

    constructor(context: Context, attributeSet: AttributeSet?, style: Int): super(context, attributeSet, style)

    fun toTranslate(translatePointF: PointF, scale: Float) {
        mTranslatePoint = PointF(CIRCLE_RADIUS/1.25f + (-translatePointF.x * scale), CIRCLE_RADIUS/1.25f + (-translatePointF.y * scale))
        mMatrix.setScale(scale, scale)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mBGBmp.setBounds(0, 0, width, height)

        canvas?.let {
            it.drawCircle(CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS, mPaint)
            it.clipPath(getPath())
            mBGBmp.draw(it)
            it.save()
            it.translate(mTranslatePoint.x, mTranslatePoint.y)
            if(this::bitmap.isInitialized) it.drawBitmap(bitmap, mMatrix, null)
            it.restore()
        }
    }

    private fun getPath(): Path {
        return Path().apply {
            addCircle(CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS, Path.Direction.CW)
        }
    }
}