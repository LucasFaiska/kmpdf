package io.github.lucasfaiska.kmpdf.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Represents a single page within a [PdfDocument].
 *
 * @property width The original width of the page in points.
 * @property height The original height of the page in points.
 */
interface PdfPage {
    val width: Int
    val height: Int

    /**
     * Renders the page into an [ImageBitmap].
     *
     * @param width The target width of the rendered image.
     * @param height The target height of the rendered image.
     * @return An [ImageBitmap] containing the rendered page.
     */
    fun render(width: Int, height: Int): ImageBitmap
}
