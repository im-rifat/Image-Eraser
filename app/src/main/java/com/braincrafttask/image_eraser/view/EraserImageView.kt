package com.braincrafttask.image_eraser.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import com.braincrafttask.image_eraser.model.EraserState
import java.lang.ClassCastException
import java.util.*


class EraserImageView: ImageView {

    interface FingerListener {
        companion object {
            const val START = 1
            const val STOP = 0
        }

        fun onMoved(point: PointF, action: Int)
    }

    private val TAG = EraserImageView::class.java.simpleName

    private lateinit var mOrinCanvas: Canvas
    private lateinit var mOriginalBmp: Bitmap
    private lateinit var mEditableBmp: Bitmap

    private lateinit var mPaint: Paint

    private lateinit var mPath: Path

    private val mMatVals = FloatArray(9)
    private lateinit var mMatrix: Matrix

    private lateinit var mMaskCanvas: Canvas
    private lateinit var mMaskBmp: Bitmap
    private lateinit var mMaskPaint: Paint

    private lateinit var mLastPoint: PointF
    private lateinit var mCirclePaint: Paint
    private var mCirclePaintable: Boolean = false

    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var origWidth: Float = 0f
    private var origHeight: Float = 0f
    private var oldMeasuredWidth: Int = 0
    private var oldMeasuredHeight: Int = 0
    private var mCurrentScale: Float = 1.0f

    private var mFingerListener: FingerListener? = null

    private val mStates: Vector<EraserState> = Vector()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, -1)

    constructor(context: Context, attrs: AttributeSet?, style: Int): super(context, attrs, style) {
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = 36.0f
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        mMaskPaint = Paint()
        mMaskPaint.isAntiAlias = true
        mMaskPaint.isDither = true
        mMaskPaint.color = Color.TRANSPARENT
        mMaskPaint.strokeWidth = 36.0f
        mMaskPaint.strokeCap = Paint.Cap.ROUND
        mMaskPaint.style = Paint.Style.STROKE

        mPath = Path()

        mMatrix = Matrix()
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX

        mOrinCanvas = Canvas()
        mMaskCanvas = Canvas()

        mLastPoint = PointF()
        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCirclePaint.isDither = true
        mCirclePaint.color = Color.LTGRAY
        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeWidth = 5.0f

        try {
            mFingerListener = context as FingerListener
        } catch (e: ClassCastException) {
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.mMatrix.getValues(mMatVals)

        canvas?.let {
            if(mCirclePaintable) it.drawCircle(mLastPoint.x, mLastPoint.y, 30f, mCirclePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        Log.wtf(TAG, event?.toString())

        event?.let {
            val action: Int = event.action

            val realX = (event.x - mMatVals[Matrix.MTRANS_X]) / mMatVals[Matrix.MSCALE_X]
            val realY = (event.y - mMatVals[Matrix.MTRANS_Y]) / mMatVals[Matrix.MSCALE_Y]

            mLastPoint.set(event.x, event.y)
            var currentAction = action

            when(action) {
                MotionEvent.ACTION_DOWN -> {
                    mCirclePaintable = true
                    mPath = Path()
                    mPath.moveTo(realX, realY)

                    currentAction = FingerListener.START
                }
                MotionEvent.ACTION_MOVE -> {
                    mPath.lineTo(realX, realY)
                    mOrinCanvas.drawPath(mPath, mPaint)
                    mMaskCanvas.drawPath(mPath, mMaskPaint)

                    currentAction = FingerListener.START
                }
                MotionEvent.ACTION_UP -> {
                    mCirclePaintable = false
                    //setImageBitmap()
                    currentAction = FingerListener.STOP
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

    fun setBitmap(originalBmp: Bitmap) {
        this.mOriginalBmp = originalBmp
        mInverted = true
        mMaskBmp = Bitmap.createBitmap(this.mOriginalBmp.width, this.mOriginalBmp.height, Bitmap.Config.ARGB_8888)

        invert()

        setImageBitmap(this.mEditableBmp)
        invalidate()
    }

    private fun fitScreen() {
        val drawable = drawable
        if (drawable != null && drawable.intrinsicWidth != 0 && drawable.intrinsicHeight != 0) {
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight
            mCurrentScale = Math.min(this.viewWidth.toFloat() / bmWidth.toFloat(), this.viewHeight.toFloat() / bmHeight.toFloat())
            this.mMatrix.setScale(mCurrentScale, mCurrentScale)
            val redundantYSpace = (this.viewHeight.toFloat() - bmHeight.toFloat() * mCurrentScale) / 2.0f
            val redundantXSpace = (this.viewWidth.toFloat() - bmWidth.toFloat() * mCurrentScale) / 2.0f
            this.mMatrix.postTranslate(redundantXSpace, redundantYSpace)
            this.origWidth = this.viewWidth.toFloat() - 2.0f * redundantXSpace
            this.origHeight = this.viewHeight.toFloat() - 2.0f * redundantYSpace
            imageMatrix = this.mMatrix

            this.mMatrix.getValues(mMatVals)
        }
    }

    fun getBitmap(): Bitmap {
        val canvas = Canvas()
        val bmp = Bitmap.createBitmap(mOriginalBmp.width, mOriginalBmp.height, Bitmap.Config.ARGB_8888)

        canvas.setBitmap(bmp)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(mEditableBmp, 0f, 0f, null)

        return bmp
    }

    private var mInverted: Boolean = true

    fun invert() {
        mInverted = !mInverted

        if(mInverted) {
            mMaskPaint.color = Color.BLACK
        } else {
            mMaskPaint.color = Color.TRANSPARENT
            mMaskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        invertMaskBmp()
        mMaskCanvas.setBitmap(mMaskBmp)

        initEditableBmp()

        setImageBitmap(mEditableBmp)
    }

    fun getCurrentScale() = mCurrentScale

    private fun initEditableBmp() {
        val canvas = Canvas()
        mEditableBmp = mOriginalBmp.copy(mOriginalBmp.config, true)
        canvas.setBitmap(mEditableBmp)

        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mMaskBmp, 0f, 0f, paint)

        mOrinCanvas.setBitmap(mEditableBmp)
    }

    private fun invertMaskBmp() {
        for(i in 0..mMaskBmp.width-1) {
            for(j in 0..mMaskBmp.height-1) {
                var color = mMaskBmp.getPixel(i, j)
                if(mInverted) {
                    if(color == Color.BLACK) color = Color.TRANSPARENT
                    else color = Color.BLACK
                } else {
                    if(color == Color.TRANSPARENT) color = Color.BLACK
                    else color = Color.TRANSPARENT
                }
                mMaskBmp.setPixel(i, j, color)
            }
        }
    }
}