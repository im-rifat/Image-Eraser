package com.braincrafttask.image_eraser.controller

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.braincrafttask.image_eraser.AppConstants
import com.braincrafttask.image_eraser.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import java.util.jar.Manifest


class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        findViewById<Button>(R.id.btnLoadImage).setOnClickListener {
            requestPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == AppConstants.RC_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val paths = Matisse.obtainResult(data)
            val path = paths[0]

            startEditorActivity(path.toString())
        }
    }

    private fun requestPermission() {
        Dexter.withContext(this).withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                loadImagePicker()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
            }
        }).onSameThread().check()
    }

    private fun loadImagePicker() {
        Matisse.from(this)
            .choose(MimeType.ofImage())
            .countable(true)
            .maxSelectable(1)
            .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(GlideEngine())
            .forResult(AppConstants.RC_IMAGE_PICKER)
    }

    private fun startEditorActivity(path: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(AppConstants.IE_PATH, path)
        startActivity(intent)
    }
}