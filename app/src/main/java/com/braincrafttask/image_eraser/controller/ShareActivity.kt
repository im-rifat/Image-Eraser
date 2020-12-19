package com.braincrafttask.image_eraser.controller

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.braincrafttask.image_eraser.AppConstants
import com.braincrafttask.image_eraser.R
import com.bumptech.glide.Glide

class ShareActivity: AppCompatActivity() {

    private lateinit var mImgPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_share)

        mImgPreview = findViewById(R.id.imgPreview)

        Glide.with(mImgPreview.context).load(intent?.getStringExtra(AppConstants.IE_PATH)).into(mImgPreview)

        Toast.makeText(this, "Image saved in cache folder", Toast.LENGTH_LONG).show()
    }
}