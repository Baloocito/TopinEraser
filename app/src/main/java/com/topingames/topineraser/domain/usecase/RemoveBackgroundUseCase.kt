package com.topingames.topineraser.domain.usecase

import android.graphics.Bitmap
import com.topingames.topineraser.data.repository.BackgroundRemovalRepository
import com.topingames.topineraser.domain.model.EraserResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoveBackgroundUseCase @Inject constructor(
    private val repository: BackgroundRemovalRepository
) {
    operator fun invoke(bitmap: Bitmap, tolerance: Int = 45): Flow<EraserResult> =
        repository.removeBackground(bitmap, tolerance)
}
