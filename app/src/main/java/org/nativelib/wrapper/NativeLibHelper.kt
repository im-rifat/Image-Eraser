package org.nativelib.wrapper

import android.graphics.Bitmap
import android.graphics.Color

object NativeLibHelper {

    /**
     * this method invert mask images black to transparent,
     * transparent to black and vice versa
     */
    external fun invertMaskImg(maskBmp: Bitmap, black: Int = Color.BLACK, transparent: Int = Color.TRANSPARENT)

    init {
        System.loadLibrary("native-lib")
    }
}