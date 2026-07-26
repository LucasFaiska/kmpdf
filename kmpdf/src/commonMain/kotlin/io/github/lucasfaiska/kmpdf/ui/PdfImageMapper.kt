package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-specific function to convert a [ByteArray] of pixels into an [ImageBitmap].
 *
 * @param width The width of the image.
 * @param height The height of the image.
 * @return An [ImageBitmap] representing the pixels.
 */
expect fun ByteArray.toImageBitmap(
    width: Int,
    height: Int,
): ImageBitmap
