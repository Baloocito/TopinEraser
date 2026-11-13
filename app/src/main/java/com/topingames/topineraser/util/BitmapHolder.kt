package com.topingames.topineraser.util

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton para pasar el bitmap entre pantallas sin serialización.
 * Se limpia al guardar el resultado final.
 */
@Singleton
class BitmapHolder @Inject constructor() {
    var sourceBitmap: Bitmap? = null
    var resultBitmap: Bitmap? = null

    fun clear() {
        sourceBitmap = null
        resultBitmap = null
    }
}
