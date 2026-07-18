package io.github.lucasfaiska.kmpdf.reader

import android.content.Context
import android.os.ParcelFileDescriptor
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngineProvider
import io.github.lucasfaiska.kmpdf.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AndroidPdfReader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : PdfReader {
    override suspend fun open(
        bytes: ByteArray,
        password: String?,
    ): PdfLoadStatus =
        withContext(dispatcher) {
            if (isEncrypted(bytes) && password == null) {
                return@withContext PdfLoadStatus.PasswordRequired
            }

            val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
            try {
                FileOutputStream(tempFile).use { it.write(bytes) }
                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)

                try {
                    val engine = AndroidPdfEngineProvider.provideEngine(pfd, password)
                    PdfLoadStatus.Success(AndroidPdfDocument(engine, tempFile, dispatcher))
                } catch (e: SecurityException) {
                    pfd.close()
                    PdfLoadStatus.InvalidPassword
                } catch (e: IOException) {
                    pfd.close()
                    PdfLoadStatus.Error(PdfError(PdfErrorType.IO_ERROR, e.message, e))
                } catch (e: Exception) {
                    pfd.close()
                    PdfLoadStatus.Error(PdfError(PdfErrorType.GENERIC, e.message, e))
                }
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                PdfLoadStatus.Error(PdfError(PdfErrorType.IO_ERROR, e.message, e))
            }
        }

    private fun isEncrypted(bytes: ByteArray): Boolean {
        val content = bytes.decodeToString(endIndex = minOf(bytes.size, 1024))
        return content.contains("/Encrypt") ||
            bytes
                .takeLast(1024)
                .toByteArray()
                .decodeToString()
                .contains("/Encrypt")
    }

    private companion object {
        private const val TEMP_FILE_PREFIX = "kmpdf_"
        private const val TEMP_FILE_SUFFIX = ".pdf"
    }
}
