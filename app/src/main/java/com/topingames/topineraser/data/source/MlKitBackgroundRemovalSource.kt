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

    // Tolerance: how similar a pixel must be to background color to be removed (0-255)
    private val tolerance = 30

    fun removeBackground(sourceBitmap: Bitmap): Flow<EraserResult> = flow {
        emit(EraserResult.Loading)
        try {
            val output = floodFillRemoveBackground(sourceBitmap)
            emit(EraserResult.Success(output))
        } catch (e: Exception) {
            emit(EraserResult.Error(e.message ?: "Error desconocido"))
        }
    }

    private fun floodFillRemoveBackground(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val visited = BooleanArray(width * height)
        val toRemove = BooleanArray(width * height)

        // Sample background color from multiple edge points for robustness
        val bgColor = sampleBackgroundColor(pixels, width, height)

        // BFS flood fill from all 4 edges
        val queue = LinkedList<Int>()

        fun enqueueIfBackground(x: Int, y: Int) {
            if (x < 0 || x >= width || y < 0 || y >= height) return
            val idx = y * width + x
            if (!visited[idx] && isSimilarColor(pixels[idx], bgColor)) {
                visited[idx] = true
                toRemove[idx] = true
                queue.add(idx)
            }
        }

        // Seed from all edges
        for (x in 0 until width) {
            enqueueIfBackground(x, 0)
            enqueueIfBackground(x, height - 1)
        }
        for (y in 0 until height) {
            enqueueIfBackground(0, y)
            enqueueIfBackground(width - 1, y)
        }

        // BFS expansion
        while (queue.isNotEmpty()) {
            val idx = queue.poll()
            val x = idx % width
            val y = idx / width
            enqueueIfBackground(x - 1, y)
            enqueueIfBackground(x + 1, y)
            enqueueIfBackground(x, y - 1)
            enqueueIfBackground(x, y + 1)
        }

        // Apply: make removed pixels transparent
        val outPixels = pixels.copyOf()
        for (i in outPixels.indices) {
            if (toRemove[i]) {
                outPixels[i] = Color.TRANSPARENT
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Sample background color from corners and edges, return the most common.
     */
    private fun sampleBackgroundColor(pixels: IntArray, width: Int, height: Int): Int {
        val samples = listOf(
            pixels[0],                          // top-left
            pixels[width - 1],                  // top-right
            pixels[(height - 1) * width],       // bottom-left
            pixels[height * width - 1],         // bottom-right
            pixels[(height / 2) * width],       // mid-left
            pixels[(height / 2) * width + width - 1], // mid-right
            pixels[width / 2],                  // top-mid
            pixels[(height - 1) * width + width / 2]  // bottom-mid
        )
        // Return the most common sample (simple majority)
        return samples.groupBy { it }.maxByOrNull { it.value.size }?.key ?: pixels[0]
    }

    private fun isSimilarColor(pixel: Int, reference: Int): Boolean {
        // Ignore fully transparent pixels
        if (Color.alpha(pixel) < 10) return true

        val dr = Math.abs(Color.red(pixel) - Color.red(reference))
        val dg = Math.abs(Color.green(pixel) - Color.green(reference))
        val db = Math.abs(Color.blue(pixel) - Color.blue(reference))
        return dr <= tolerance && dg <= tolerance && db <= tolerance
    }
}
