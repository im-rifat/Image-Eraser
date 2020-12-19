package com.braincrafttask.image_eraser.controller

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.braincrafttask.image_eraser.AppConstants
import com.braincrafttask.image_eraser.R
import com.braincrafttask.image_eraser.WriteImageTask
import com.braincrafttask.image_eraser.view.EraserImageView
import com.braincrafttask.image_eraser.view.MagnifyView

class MainActivity : AppCompatActivity(), EraserImageView.FingerListener {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mPreview: EraserImageView
    private lateinit var mMagnifyView: MagnifyView

    private lateinit var mBtnInvert: Button
    private lateinit var mBtnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPreview = findViewById(R.id.preview)
        mPreview.setBitmap(BitmapFactory.decodeResource(resources,
            R.drawable.sunflower
        ))

        mMagnifyView = findViewById(R.id.magnifyView)

        mBtnInvert = findViewById(R.id.btnInvert)
        mBtnInvert.setOnClickListener {
            mPreview.invert()
        }

        mBtnSave = findViewById(R.id.btnSave)
        mBtnSave.setOnClickListener {
            val savedPath = WriteImageTask(this).saveImage(mPreview.getBitmap())

            val intent = Intent(this, ShareActivity::class.java)
            intent.putExtra(AppConstants.IE_PATH, savedPath)
            startActivity(intent)
        }
    }

    override fun onMoved(point: PointF, action: Int) {
        mMagnifyView.bitmap = mPreview.getBitmap()
        mMagnifyView.toTranslate(point, mPreview.getCurrentScale())
        mMagnifyView.visibility = if(action == EraserImageView.FingerListener.START) View.VISIBLE else View.GONE

        Log.wtf("xyz", "moved action " + action)
    }
}