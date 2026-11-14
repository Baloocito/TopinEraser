package com.topingames.topineraser.data.source

import android.graphics.Bitmap
import android.graphics.Color
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Removes background using flood-fill from image edges.
 * Works perfectly for anime/game characters with uniform backgrounds.
 * 100% on-device, no ML model needed.
 */
@Singleton
class MlKitBackgroundRemovalSource @Inject constructor() {

    fun removeBackground(sourceBitmap: Bitmap, tolerance: Int = 45): Flow<EraserResult> = flow {
        emit(EraserResult.Loading)
        try {
            val output = floodFillRemoveBackground(sourceBitmap, tolerance)
            emit(EraserResult.Success(output))
        } catch (e: Exception) {
            emit(EraserResult.Error(e.message ?: "Error desconocido"))
        }
    }

    private fun floodFillRemoveBackground(source: Bitmap, tolerance: Int): Bitmap {
        val width = source.width
        val height = source.height

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val visited = BooleanArray(width * height)
        val toRemove = BooleanArray(width * height)

        val bgColor = sampleBackgroundColor(pixels, width, height)

        val queue = LinkedList<Int>()

        fun enqueueIfBackground(x: Int, y: Int) {
            if (x < 0 || x >= width || y < 0 || y >= height) return
            val idx = y * width + x
            if (!visited[idx] && isSimilarColor(pixels[idx], bgColor, tolerance)) {
                visited[idx] = true
                toRemove[idx] = true
                queue.add(idx)
            }
        }

        for (x in 0 until width) {
            enqueueIfBackground(x, 0)
            enqueueIfBackground(x, height - 1)
        }
        for (y in 0 until height) {
            enqueueIfBackground(0, y)
            enqueueIfBackground(width - 1, y)
        }

        while (queue.isNotEmpty()) {
            val idx = queue.poll()
            val x = idx % width
            val y = idx / width
            enqueueIfBackground(x - 1, y)
            enqueueIfBackground(x + 1, y)
            enqueueIfBackground(x, y - 1)
            enqueueIfBackground(x, y + 1)
            enqueueIfBackground(x - 1, y - 1)
            enqueueIfBackground(x + 1, y - 1)
            enqueueIfBackground(x - 1, y + 1)
            enqueueIfBackground(x + 1, y + 1)
        }

        val outPixels = pixels.copyOf()
        for (i in outPixels.indices) {
            if (toRemove[i]) outPixels[i] = Color.TRANSPARENT
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun sampleBackgroundColor(pixels: IntArray, width: Int, height: Int): Int {
        val samples = listOf(
            pixels[0],
            pixels[width - 1],
            pixels[(height - 1) * width],
            pixels[height * width - 1],
            pixels[(height / 2) * width],
            pixels[(height / 2) * width + width - 1],
            pixels[width / 2],
            pixels[(height - 1) * width + width / 2]
        )
        return samples.groupBy { it }.maxByOrNull { it.value.size }?.key ?: pixels[0]
    }

    private fun isSimilarColor(pixel: Int, reference: Int, tolerance: Int): Boolean {
        if (Color.alpha(pixel) < 10) return true
        val dr = Math.abs(Color.red(pixel) - Color.red(reference))
        val dg = Math.abs(Color.green(pixel) - Color.green(reference))
        val db = Math.abs(Color.blue(pixel) - Color.blue(reference))
        return dr <= tolerance && dg <= tolerance && db <= tolerance
    }
}
