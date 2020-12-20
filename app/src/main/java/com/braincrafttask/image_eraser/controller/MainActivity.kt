package com.braincrafttask.image_eraser.controller

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class MainActivity : AppCompatActivity(), EraserImageView.FingerListener {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mPreview: EraserImageView
    private lateinit var mMagnifyView: MagnifyView

    private lateinit var mBtnInvert: Button
    private lateinit var mBtnSave: Button

    private var mImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent?.let {
            mImagePath = it.getStringExtra(AppConstants.IE_PATH)
        } ?: savedInstanceState?.let {
            mImagePath = it.getString(AppConstants.IE_PATH)
        }

        mMagnifyView = findViewById(R.id.magnifyView)
        mMagnifyView.setBrushSize(30f)

        mPreview = findViewById(R.id.preview)
        mPreview.setBrushSize(30f)

        Glide.with(this).asBitmap().load(Uri.parse(mImagePath)).override(620, 620).into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                mPreview.setBitmap(resource)
            }
        })

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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(AppConstants.IE_PATH, mImagePath)

        super.onSaveInstanceState(outState)
    }

    override fun onMoved(point: PointF, action: Int) {
        mMagnifyView.setBitmap(mPreview.getBitmap())
        mMagnifyView.toTranslate(point, mPreview.getCurrentScale())
        mMagnifyView.visibility = if(action == EraserImageView.FingerListener.STOP) View.GONE else View.VISIBLE
    }
}