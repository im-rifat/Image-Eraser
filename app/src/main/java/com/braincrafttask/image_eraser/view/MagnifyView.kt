package com.braincrafttask.image_eraser.view

import android.widget.ImageView
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import com.braincrafttask.image_eraser.R

class MagnifyView: ImageView {

    val CIRCLE_RADIUS = context.resources.displayMetrics.density * 100f

    private var mPaint: Paint = Paint()

    lateinit var bitmap: Bitmap

    private var mTranslatePoint: PointF

    private var mMatrix: Matrix

    private var mBGBmp: BitmapDrawable

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, -1)

    constructor(context: Context, attributeSet: AttributeSet?, style: Int): super(context, attributeSet, style) {
        mPaint.color = Color.argb(255, 67, 67, 67)

        mTranslatePoint = PointF()
        mMatrix = Matrix()

        mBGBmp = BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.brush_view_bg))
        mBGBmp.setBounds(0, 0, width, height)
        mBGBmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    fun toTranslate(translatePointF: PointF, scale: Float) {
        mTranslatePoint = PointF(CIRCLE_RADIUS/1.25f + (-translatePointF.x * scale), CIRCLE_RADIUS/1.25f + (-translatePointF.y * scale))
        mMatrix.setScale(scale, scale)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mBGBmp.setBounds(0, 0, width, height)

        canvas?.let {
            it.drawCircle(CIRCLE_RADIUS, CIRCLE_RADIUS, CIRCLE_RADIUS, mPaint)
            it.clipPath(getPath())
            mBGBmp.draw(it)
            canvas.save()
            canvas.translate(mTranslatePoint.x, mTranslatePoint.y)
            if(this::bitmap.isInitialized) it.drawBitmap(bitmap, mMatrix, null)
            canvas.restore()
        }
    }

    private fun getPath(): Path {
        return Path().apply {
            addCircle(CIRCLE_RADIUS, CIRCLE_RADIUS, CIRCLE_RADIUS, Path.Direction.CW)
        }
    }
}