#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <queue>
#include <android/bitmap.h>

#define  LOG_TAG    "jnibitmap"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define JNI_METHOD(NAME) \
    Java_org_nativelib_wrapper_NativeLibHelper_##NAME

extern "C" {
JNIEXPORT void JNICALL JNI_METHOD(invertMaskImg) (JNIEnv *env, jobject thiz, jobject img, jint black, jint transparent);
}

JNIEXPORT void JNICALL JNI_METHOD(invertMaskImg) (JNIEnv *env, jobject thiz, jobject img, jint black, jint transparent) {
    AndroidBitmapInfo imgInfo;

    int ret;
    if ((ret = AndroidBitmap_getInfo(env, img, &imgInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if (imgInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return;
    }

    void *bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, img, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed for image ! error=%d", ret);
        return;
    }

    int width = imgInfo.width;
    int height = imgInfo.height;
    int length = width * height;

    int *ptr = (int *) bitmapPixels;

    for(int i = 0; i < length; i++) {
        if(ptr[i] == black) {
            ptr[i] = transparent;
        } else {
            ptr[i] = black;
        }
    }

    AndroidBitmap_unlockPixels(env, img);
}