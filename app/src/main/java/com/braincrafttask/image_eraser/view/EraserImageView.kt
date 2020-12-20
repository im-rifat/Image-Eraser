package com.braincrafttask.image_eraser.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.braincrafttask.image_eraser.model.EraserState
import org.nativelib.wrapper.NativeHelper
import java.lang.ClassCastException
import java.lang.Exception
import java.util.*


class EraserImageView: ImageView {

    interface FingerListener {
        companion object {
            const val START = 1
            const val MOVE = 2
            const val STOP = 0
        }

        fun onMoved(point: PointF, action: Int)
    }

    private val TAG = EraserImageView::class.java.simpleName

    private val mDensity = resources.displayMetrics.density

    private val DEFUALT_BRUSH_SIZE = mDensity * 10f

    private var mBrushSize = DEFUALT_BRUSH_SIZE

    private val mOrinCanvas = Canvas()
    private lateinit var mOriginalBmp: Bitmap
    private lateinit var mEditableBmp: Bitmap

    private var mBrushPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.BLACK
        strokeWidth = mBrushSize
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var mPath = Path()
    private var mCirclePath = Path()

    private val mMatVals = FloatArray(9)
    private var mMatrix = Matrix()

    private val mMaskCanvas = Canvas()
    private lateinit var mMaskBmp: Bitmap
    private var mMaskBrushPaint = Paint(mBrushPaint).apply {
        color = Color.TRANSPARENT
    }

    private var mLastPoint = PointF()
    private var mCirclePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = mDensity * 5.0f
    }

    private var mCirclePaintable: Boolean = false

    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var origWidth: Float = 0f
    private var origHeight: Float = 0f
    private var oldMeasuredWidth: Int = 0
    private var oldMeasuredHeight: Int = 0

    private var mFingerListener: FingerListener? = null

    private val mStates: Vector<EraserState> = Vector()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, -1)

    constructor(context: Context, attrs: AttributeSet?, style: Int): super(context, attrs, style) {
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX

        try {
            mFingerListener = context as FingerListener
        } catch (e: ClassCastException) {
        }

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas?) {
        this.mMatrix.getValues(mMatVals)

        super.onDraw(canvas)

        canvas?.let {
            //if(mCirclePaintable) it.drawCircle(mLastPoint.x, mLastPoint.y, mBrushSize / mDensity, mCirclePaint)
            if(mCirclePaintable) it.drawPath(mCirclePath, mCirclePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        Log.wtf(TAG, event?.toString())

        event?.let {
            val action: Int = event.action

            val realX = (event.x - mMatVals[Matrix.MTRANS_X]) / getCurrentScale()
            val realY = (event.y - mMatVals[Matrix.MTRANS_Y]) / getCurrentScale()

            mLastPoint.set(event.x, event.y)
            var currentAction = FingerListener.STOP
            mCirclePath.reset()

            when(action) {
                MotionEvent.ACTION_DOWN -> {
                    mCirclePaintable = true
                    mPath.moveTo(realX, realY)

                    mCirclePath.reset()
                    mCirclePath.moveTo(event.x, event.y)
                    mCirclePath.addCircle(event.x, event.y, mBrushSize, Path.Direction.CW)

                    currentAction = FingerListener.START
                }
                MotionEvent.ACTION_MOVE -> {
                    mPath.lineTo(realX, realY)
                    mOrinCanvas.drawPath(mPath, mBrushPaint)
                    mMaskCanvas.drawPath(mPath, mMaskBrushPaint)

                    mCirclePath.reset()
                    mCirclePath.moveTo(event.x, event.y)
                    mCirclePath.addCircle(event.x, event.y, mBrushSize, Path.Direction.CW)

                    currentAction = FingerListener.MOVE
                }
                MotionEvent.ACTION_UP -> {
                    mPath.reset()
                    mCirclePaintable = false
                }
                MotionEvent.ACTION_CANCEL -> {
                }
                MotionEvent.ACTION_OUTSIDE -> {
                } else -> {
                }
            }

            mFingerListener?.let {
                it.onMoved(PointF(realX, realY), currentAction)
            }
        }

        invalidate()
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        this.viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        this.viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if ((this.oldMeasuredHeight != this.viewWidth || this.oldMeasuredHeight != this.viewHeight) && this.viewWidth != 0 && this.viewHeight != 0) {
            this.oldMeasuredHeight = this.viewHeight
            this.oldMeasuredWidth = this.viewWidth
            fitScreen()
        }
    }

    private fun fitScreen() {
        val drawable = drawable
        if (drawable != null && drawable.intrinsicWidth != 0 && drawable.intrinsicHeight != 0) {
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight
            val scale = Math.min(this.viewWidth.toFloat() / bmWidth.toFloat(), this.viewHeight.toFloat() / bmHeight.toFloat())
            this.mMatrix.setScale(scale, scale)
            val redundantYSpace = (this.viewHeight.toFloat() - bmHeight.toFloat() * scale) / 2.0f
            val redundantXSpace = (this.viewWidth.toFloat() - bmWidth.toFloat() * scale) / 2.0f
            this.mMatrix.postTranslate(redundantXSpace, redundantYSpace)
            this.origWidth = this.viewWidth.toFloat() - 2.0f * redundantXSpace
            this.origHeight = this.viewHeight.toFloat() - 2.0f * redundantYSpace
            imageMatrix = this.mMatrix

            this.mMatrix.getValues(mMatVals)
        }
    }

    fun setBrushSize(brushSize: Float) {
        this.mBrushSize = (mDensity *brushSize)
        mBrushPaint.strokeWidth = this.mBrushSize
        mMaskBrushPaint.strokeWidth = this.mBrushSize
    }

    fun setBitmap(originalBmp: Bitmap) {
        this.mOriginalBmp = originalBmp
        mInverted = true
        mMaskBmp = Bitmap.createBitmap(this.mOriginalBmp.width, this.mOriginalBmp.height, Bitmap.Config.ARGB_8888)

        invert()
    }

    fun getBitmap(): Bitmap {
        return mEditableBmp
    }

    private var mInverted: Boolean = true

    fun invert() {
        mInverted = !mInverted

        NativeHelper.invertMaskImg(mMaskBmp)
        mMaskCanvas.setBitmap(mMaskBmp)

        initEditableBmp()

        setImageBitmap(mEditableBmp)
    }

    fun getCurrentScale(): Float {
        mMatrix.getValues(mMatVals)

        return mMatVals[Matrix.MSCALE_X]
    }

    private fun initEditableBmp() {
        val canvas = Canvas()
        mEditableBmp = Bitmap.createBitmap(mOriginalBmp.width, mOriginalBmp.height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(mEditableBmp)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(mOriginalBmp.copy(mOriginalBmp.config, true), 0f, 0f, null)

        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mMaskBmp, 0f, 0f, paint)

        mOrinCanvas.setBitmap(mEditableBmp)
    }
}