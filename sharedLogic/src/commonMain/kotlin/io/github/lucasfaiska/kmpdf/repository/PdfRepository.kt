package io.github.lucasfaiska.kmpdf.repository

import io.github.lucasfaiska.kmpdf.loader.PdfLoader
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.github.lucasfaiska.kmpdf.reader.PdfReader

interface PdfRepository {
    /**
     * Loads a PDF document from the provided source.
     *
     * @param source The [PdfSource] to load from.
     * @param password An optional password for encrypted documents.
     * @return A [PdfLoadStatus] instance.
     */
    suspend fun loadDocument(
        source: PdfSource,
        password: String? = null,
    ): PdfLoadStatus
}

class PdfRepositoryImpl(
    private val loader: PdfLoader,
    private val reader: PdfReader,
) : PdfRepository {
    override suspend fun loadDocument(
        source: PdfSource,
        password: String?,
    ): PdfLoadStatus =
        try {
            val bytes = loader.load(source)
            reader.open(bytes, password)
        } catch (e: Exception) {
            PdfLoadStatus.Error(
                PdfError(
                    type = PdfErrorType.GENERIC,
                    message = e.message,
                    throwable = e,
                ),
            )
        }
}
