package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap

internal interface NativePdfEngineWrapper {
    val pageCount: Int

    fun openPage(index: Int): NativePdfPageWrapper

    fun close()
}

internal interface NativePdfPageWrapper {
    val width: Int

    val height: Int

    fun render(bitmap: Bitmap)

    fun close()
}
