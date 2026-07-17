package io.github.lucasfaiska.kmpdf.reader

import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfInvalidPasswordException
import io.github.lucasfaiska.kmpdf.model.PdfPasswordRequiredException

interface PdfReader {
    /**
     * Opens a PDF document from the provided byte array.
     *
     * @param bytes The byte array containing the PDF document data.
     * @param password An optional password for encrypted documents.
     * @return A [PdfDocument] instance.
     * @throws PdfPasswordRequiredException if the document is protected and no password was provided.
     * @throws PdfInvalidPasswordException if the provided password is incorrect.
     */
    suspend fun open(bytes: ByteArray, password: String? = null): PdfDocument
}
