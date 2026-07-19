package io.github.lucasfaiska.kmpdf.reader

import android.content.Context
import android.os.ParcelFileDescriptor
import io.github.lucasfaiska.kmpdf.engine.AndroidPdfEngineProvider
import io.github.lucasfaiska.kmpdf.model.AndroidPdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfError
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidPdfReader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : PdfReader {
    override suspend fun open(
        bytes: ByteArray,
        password: String?,
    ): PdfLoadStatus =
        withContext(dispatcher) {
            val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
            try {
                FileOutputStream(tempFile).use { it.write(bytes) }
                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)

                try {
                    val engine = AndroidPdfEngineProvider.provideEngine(pfd, password)
                    PdfLoadStatus.Success(AndroidPdfDocument(engine, tempFile, dispatcher))
                } catch (_: SecurityException) {
                    pfd.close()
                    if (password == null) PdfLoadStatus.PasswordRequired else PdfLoadStatus.InvalidPassword
                } catch (e: Exception) {
                    pfd.close()
                    val errorType =
                        when {
                            e.message?.contains("format", ignoreCase = true) == true ||
                                e.message?.contains("corrupt", ignoreCase = true) == true -> PdfErrorType.CORRUPTED
                            e.message?.contains("unsupported", ignoreCase = true) == true -> PdfErrorType.UNSUPPORTED
                            else -> PdfErrorType.GENERIC
                        }
                    PdfLoadStatus.Error(PdfError(errorType, e.message, e))
                }
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                PdfLoadStatus.Error(PdfError(PdfErrorType.IO_ERROR, e.message, e))
            }
        }

    private companion object {
        private const val TEMP_FILE_PREFIX = "kmpdf_"
        private const val TEMP_FILE_SUFFIX = ".pdf"
    }
}
