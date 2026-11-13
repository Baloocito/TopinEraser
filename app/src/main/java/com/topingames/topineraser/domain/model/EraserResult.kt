package com.topingames.topineraser.domain.model

import android.graphics.Bitmap

sealed class EraserResult {
    data class Success(val bitmap: Bitmap) : EraserResult()
    data class Error(val message: String) : EraserResult()
    data object Loading : EraserResult()
}
