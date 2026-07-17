package io.github.lucasfaiska.kmpdf.reader

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import io.github.lucasfaiska.kmpdf.model.AndroidPdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import io.github.lucasfaiska.kmpdf.model.PdfInvalidPasswordException
import io.github.lucasfaiska.kmpdf.model.PdfPasswordRequiredException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidPdfReader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : PdfReader {
    override suspend fun open(bytes: ByteArray, password: String?): PdfDocument =
        withContext(dispatcher) {
            val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
            FileOutputStream(tempFile).use { it.write(bytes) }

            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)

            try {
                val renderer = PdfRenderer(pfd)
                AndroidPdfDocument(renderer, tempFile, dispatcher)
            } catch (e: SecurityException) {
                pfd.close()
                if (tempFile.exists()) tempFile.delete()
                throw PdfPasswordRequiredException()
            } catch (e: Exception) {
                pfd.close()
                if (tempFile.exists()) tempFile.delete()
                throw e
            }
        }

    private companion object {
        private const val TEMP_FILE_PREFIX = "kmpdf_"
        private const val TEMP_FILE_SUFFIX = ".pdf"
    }
}
