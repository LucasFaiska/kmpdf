package io.github.lucasfaiska.kmpdf.reader

import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus

interface PdfReader {
    /**
     * Opens a PDF document from the provided byte array.
     *
     * @param bytes The byte array containing the PDF document data.
     * @param password An optional password for encrypted documents.
     * @return A [PdfLoadStatus] instance.
     */
    suspend fun open(
        bytes: ByteArray,
        password: String? = null,
    ): PdfLoadStatus
}
