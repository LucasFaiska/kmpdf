package io.github.lucasfaiska.kmpdf.reader

import io.github.lucasfaiska.kmpdf.model.PdfDocument

/**
 * Interface for reading and opening PDF documents.
 */
interface PdfReader {
    /**
     * Opens a PDF document from a [ByteArray].
     *
     * @param bytes The byte array containing the PDF document data.
     * @return A [PdfDocument] instance.
     */
    suspend fun open(bytes: ByteArray): PdfDocument
}
