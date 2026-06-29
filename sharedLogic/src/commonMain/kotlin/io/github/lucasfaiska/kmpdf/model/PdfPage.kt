package io.github.lucasfaiska.kmpdf.model

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
     * Renders the page into a [ByteArray] of pixels.
     *
     * @param width The target width of the rendered image.
     * @param height The target height of the rendered image.
     * @return A [ByteArray] containing the rendered page pixels in ARGB format.
     */
    suspend fun render(width: Int, height: Int): ByteArray
}
