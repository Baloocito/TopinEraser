package com.topingames.topineraser.data.source

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitBackgroundRemovalSource @Inject constructor() {

    private val segmenter by lazy {
        SubjectSegmentation.getClient(
            SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
        )
    }

    fun removeBackground(sourceBitmap: Bitmap): Flow<EraserResult> = callbackFlow {
        trySend(EraserResult.Loading)

        val inputImage = InputImage.fromBitmap(sourceBitmap, 0)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                val foreground = result.foregroundBitmap
                if (foreground != null) {
                    trySend(EraserResult.Success(foreground))
                } else {
                    // Manual mask approach as fallback
                    val mask = result.confidenceMasks?.firstOrNull()
                    if (mask != null) {
                        val output = applyMask(sourceBitmap, mask.buffer.array())
                        trySend(EraserResult.Success(output))
                    } else {
                        trySend(EraserResult.Error("No se pudo segmentar la imagen"))
                    }
                }
                close()
            }
            .addOnFailureListener { e ->
                trySend(EraserResult.Error(e.message ?: "Error desconocido"))
                close()
            }

        awaitClose { segmenter.close() }
    }

    private fun applyMask(source: Bitmap, maskBuffer: ByteArray): Bitmap {
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(source.width * source.height)
        output.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)

        for (i in pixels.indices) {
            val confidence = (maskBuffer[i].toInt() and 0xFF) / 255f
            if (confidence < 0.5f) {
                pixels[i] = Color.TRANSPARENT
            }
        }

        output.setPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        return output
    }
}
