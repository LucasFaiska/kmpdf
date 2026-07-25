package io.github.lucasfaiska.kmpdf.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

actual fun ByteArray.toImageBitmap(
    width: Int,
    height: Int,
): ImageBitmap {
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(this))
    return bitmap.asImageBitmap()
}
