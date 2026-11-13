package com.topingames.topineraser.data.source

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitBackgroundRemovalSource @Inject constructor() {

    private val segmenter by lazy {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        Segmentation.getClient(options)
    }

    fun removeBackground(sourceBitmap: Bitmap): Flow<EraserResult> = callbackFlow {
        trySend(EraserResult.Loading)

        val inputImage = InputImage.fromBitmap(sourceBitmap, 0)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                val mask = result.buffer
                val maskWidth = result.width
                val maskHeight = result.height

                val output = applyMask(sourceBitmap, mask.array(), maskWidth, maskHeight)
                trySend(EraserResult.Success(output))
                close()
            }
            .addOnFailureListener { e ->
                trySend(EraserResult.Error(e.message ?: "Error desconocido"))
                close()
            }

        awaitClose { segmenter.close() }
    }

    private fun applyMask(
        source: Bitmap,
        maskBytes: ByteArray,
        maskWidth: Int,
        maskHeight: Int
    ): Bitmap {
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(source.width * source.height)
        output.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)

        val scaleX = maskWidth.toFloat() / source.width
        val scaleY = maskHeight.toFloat() / source.height

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val maskX = (x * scaleX).toInt().coerceIn(0, maskWidth - 1)
                val maskY = (y * scaleY).toInt().coerceIn(0, maskHeight - 1)
                val maskIndex = maskY * maskWidth + maskX
                val confidence = (maskBytes[maskIndex].toInt() and 0xFF) / 255f
                if (confidence < 0.5f) {
                    pixels[y * source.width + x] = Color.TRANSPARENT
                }
            }
        }

        output.setPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        return output
    }
}
