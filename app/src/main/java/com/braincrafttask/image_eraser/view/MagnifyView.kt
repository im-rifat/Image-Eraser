package com.braincrafttask.image_eraser.view

import android.widget.ImageView
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import com.braincrafttask.image_eraser.R

class MagnifyView: ImageView {

    private val mDensity = resources.displayMetrics.density

    private val CIRCLE_RADIUS = mDensity * 80f
    private val START_POS = mDensity * 20f

    private val DEFAULT_BRUSH_SIZE = mDensity * 10f

    private var mBrushSize = DEFAULT_BRUSH_SIZE

    private var mPaint: Paint = Paint().apply {
        color = Color.argb(255, 255, 245, 238)
        strokeWidth = mDensity * 10f
        style = Paint.Style.STROKE
    }

    private var mCirclePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = mDensity * 5.0f
    }

    private lateinit var bitmap: Bitmap

    private var mTranslatePoint = PointF()

    private var mMatrix = Matrix()

    private var mBGBmp: BitmapDrawable =
        BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.brush_view_bg)).apply {
            setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, -1)

    constructor(context: Context, attributeSet: AttributeSet?, style: Int): super(context, attributeSet, style)

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
            it.drawCircle(CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS + START_POS, mBrushSize, mCirclePaint)
        }
    }

    private fun getPath(): Path {
        return Path().apply {
            addCircle(CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS + START_POS, CIRCLE_RADIUS, Path.Direction.CW)
        }
    }

    fun toTranslate(translatePointF: PointF, scale: Float) {
        mTranslatePoint = PointF((CIRCLE_RADIUS + START_POS) + (-translatePointF.x * scale), (CIRCLE_RADIUS + START_POS) + (-translatePointF.y * scale))
        mMatrix.setScale(scale, scale)
        invalidate()
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun setBrushSize(brushSize: Float) {
        this.mBrushSize = mDensity * brushSize
    }
}