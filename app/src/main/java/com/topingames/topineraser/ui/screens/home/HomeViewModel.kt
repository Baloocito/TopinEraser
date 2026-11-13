package com.topingames.topineraser.ui.screens.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.topingames.topineraser.util.BitmapHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bitmapHolder: BitmapHolder
) : ViewModel() {

    fun setSourceBitmap(bitmap: Bitmap) {
        bitmapHolder.sourceBitmap = bitmap
    }
}
