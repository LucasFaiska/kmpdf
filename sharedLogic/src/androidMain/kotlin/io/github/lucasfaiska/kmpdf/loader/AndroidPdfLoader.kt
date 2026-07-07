package io.github.lucasfaiska.kmpdf.loader

import android.content.Context
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android-specific implementation of [SharedPdfLoader].
 * Handles local source resolution using [android.content.ContentResolver].
 */
class AndroidPdfLoader(
    private val context: Context,
    httpClient: HttpClient = HttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SharedPdfLoader(httpClient) {
    override suspend fun loadLocal(identifier: String): ByteArray =
        withContext(dispatcher) {
            if (identifier.startsWith("file:///android_asset/")) {
                val assetPath = identifier.removePrefix("file:///android_asset/")
                context.assets.open(assetPath).use { it.readBytes() }
            } else {
                val uri = identifier.toUri()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: throw IllegalArgumentException("Could not open input stream for identifier: $identifier")
            }
        }
}
