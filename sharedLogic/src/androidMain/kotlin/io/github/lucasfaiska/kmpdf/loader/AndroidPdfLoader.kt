package io.github.lucasfaiska.kmpdf.loader

import android.content.Context
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Android-specific implementation of [SharedPdfLoader].
 * Handles local source resolution using [android.content.ContentResolver].
 */
class AndroidPdfLoader(
    private val context: Context,
    httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) : SharedPdfLoader(httpClient) {
    override suspend fun loadLocal(identifier: String): ByteArray =
        withContext(dispatcher) {
            val uri = identifier.toUri()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw IllegalArgumentException("Could not open input stream for identifier: $identifier")
        }
}
