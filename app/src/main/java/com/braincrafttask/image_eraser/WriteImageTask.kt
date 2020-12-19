package com.braincrafttask.image_eraser

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class WriteImageTask(context: Context) {

    private val path = context.cacheDir.toString()

    fun saveImage(bitmap: Bitmap): String {
        val file = File(path, "${UUID.randomUUID()}.png")
        Log.wtf("xyz", file.path)

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

        return file.path
    }
}