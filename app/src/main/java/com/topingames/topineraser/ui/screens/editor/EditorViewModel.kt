package com.topingames.topineraser.ui.screens.editor

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topingames.topineraser.domain.model.EraserResult
import com.topingames.topineraser.domain.usecase.RemoveBackgroundUseCase
import com.topingames.topineraser.util.BitmapHolder
import com.topingames.topineraser.util.ImageSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val sourceBitmap: Bitmap? = null,
    val resultBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedFileName: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val removeBackgroundUseCase: RemoveBackgroundUseCase,
    private val bitmapHolder: BitmapHolder,
    private val imageSaver: ImageSaver
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        val source = bitmapHolder.sourceBitmap
        if (source != null) {
            _uiState.value = _uiState.value.copy(sourceBitmap = source)
            processImage(source)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        removeBackgroundUseCase(bitmap)
            .onEach { result ->
                when (result) {
                    is EraserResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = true,
                            error = null
                        )
                    }
                    is EraserResult.Success -> {
                        bitmapHolder.resultBitmap = result.bitmap
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            resultBitmap = result.bitmap
                        )
                    }
                    is EraserResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            error = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun savePng(context: Context) {
        val bitmap = _uiState.value.resultBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            imageSaver.savePng(context, bitmap)
                .onSuccess { fileName ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedFileName = fileName
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "Error al guardar"
                    )
                }
        }
    }

    fun dismissSavedMessage() {
        _uiState.value = _uiState.value.copy(savedFileName = null)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        bitmapHolder.clear()
    }
}
