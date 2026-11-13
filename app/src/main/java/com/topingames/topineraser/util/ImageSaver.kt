package com.topingames.topineraser.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageSaver @Inject constructor() {

    suspend fun savePng(context: Context, bitmap: Bitmap): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val fileName = "topineraser_${System.currentTimeMillis()}.png"
                val outputStream: OutputStream

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/TopinEraser"
                        )
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: error("No se pudo crear el archivo")

                    outputStream = context.contentResolver.openOutputStream(uri)
                        ?: error("No se pudo abrir el stream")
                } else {
                    val dir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + "/TopinEraser"
                    )
                    dir.mkdirs()
                    val file = java.io.File(dir, fileName)
                    outputStream = file.outputStream()
                }

                outputStream.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                fileName
            }
        }
}
