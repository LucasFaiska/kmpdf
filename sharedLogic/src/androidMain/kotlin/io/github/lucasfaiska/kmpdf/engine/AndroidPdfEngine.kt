package io.github.lucasfaiska.kmpdf.engine

import android.graphics.Bitmap

internal interface AndroidPdfEngine {
    val pageCount: Int

    fun openPage(index: Int): AndroidPdfEnginePage

    fun width(index: Int): Int
    fun height(index: Int): Int

    fun close()
}

internal interface AndroidPdfEnginePage {
    val width: Int
    val height: Int

    fun render(bitmap: Bitmap)

    fun close()
}
