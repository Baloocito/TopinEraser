package com.topingames.topineraser.data.repository

import android.graphics.Bitmap
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.flow.Flow

interface BackgroundRemovalRepository {
    fun removeBackground(bitmap: Bitmap, tolerance: Int = 45): Flow<EraserResult>
}
