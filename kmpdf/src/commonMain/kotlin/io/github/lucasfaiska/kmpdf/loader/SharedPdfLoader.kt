package io.github.lucasfaiska.kmpdf.loader

import io.github.lucasfaiska.kmpdf.model.PdfSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes

/**
 * Base implementation of [PdfLoader] that handles network sources
 * and delegates local sources to platform-specific implementations.
 *
 * @property httpClient The [HttpClient] used for network requests.
 */
abstract class SharedPdfLoader(
    private val httpClient: HttpClient,
) : PdfLoader {
    override suspend fun load(source: PdfSource): ByteArray =
        when (source) {
            is PdfSource.Url -> loadFromUrl(source.url)
            is PdfSource.Local -> loadLocal(source.identifier)
        }

    /**
     * Loads PDF data from a remote URL using the provided [HttpClient].
     */
    private suspend fun loadFromUrl(url: String): ByteArray = httpClient.get(url).readRawBytes()

    /**
     * Abstract method to be implemented by each platform to handle local resources.
     */
    protected abstract suspend fun loadLocal(identifier: String): ByteArray
}
