package com.topingames.topineraser.data.repository

import android.graphics.Bitmap
import com.topingames.topineraser.data.source.MlKitBackgroundRemovalSource
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundRemovalRepositoryImpl @Inject constructor(
    private val mlKitSource: MlKitBackgroundRemovalSource
) : BackgroundRemovalRepository {

    override fun removeBackground(bitmap: Bitmap, tolerance: Int): Flow<EraserResult> =
        mlKitSource.removeBackground(bitmap, tolerance)
}
