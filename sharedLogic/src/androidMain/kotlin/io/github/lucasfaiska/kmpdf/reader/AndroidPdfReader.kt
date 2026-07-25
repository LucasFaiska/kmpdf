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
            val tempFile = createTempFile()
            try {
                writeBytesToFile(tempFile, bytes)
                openAndProcessPdf(tempFile, password)
            } catch (e: Exception) {
                handleGlobalError(tempFile, e)
            }
        }

    private fun createTempFile(): File = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)

    private fun writeBytesToFile(
        file: File,
        bytes: ByteArray,
    ) = FileOutputStream(file).use { it.write(bytes) }

    private fun openAndProcessPdf(
        tempFile: File,
        password: String?,
    ): PdfLoadStatus {
        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        return try {
            val engine = AndroidPdfEngineProvider.provideEngine(pfd, password)
            PdfLoadStatus.Success(AndroidPdfDocument(engine, tempFile, dispatcher))
        } catch (e: Exception) {
            pfd.close()
            handlePdfEngineError(e, password)
        }
    }

    private fun handlePdfEngineError(
        e: Exception,
        password: String?,
    ): PdfLoadStatus =
        when (e) {
            is SecurityException -> if (password == null) PdfLoadStatus.PasswordRequired else PdfLoadStatus.InvalidPassword
            else -> PdfLoadStatus.Error(PdfError(PdfErrorType.GENERIC, e.message, e))
        }

    private fun handleGlobalError(
        tempFile: File,
        e: Exception,
    ): PdfLoadStatus {
        if (tempFile.exists()) tempFile.delete()
        return PdfLoadStatus.Error(PdfError(PdfErrorType.IO_ERROR, e.message, e))
    }

    private companion object {
        private const val TEMP_FILE_PREFIX = "kmpdf_"
        private const val TEMP_FILE_SUFFIX = ".pdf"
    }
}
