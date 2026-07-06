package io.github.lucasfaiska.kmpdf.loader

import io.github.lucasfaiska.kmpdf.model.PdfSource

/**
 * Interface for loading PDF data from various sources into a [ByteArray].
 */
interface PdfLoader {
    /**
     * Loads the PDF data from the specified [source].
     *
     * @param source The [PdfSource] to load data from.
     * @return A [ByteArray] containing the raw PDF data.
     * @throws Exception if the data cannot be loaded (e.g., network error, file not found).
     */
    suspend fun load(source: PdfSource): ByteArray
}
