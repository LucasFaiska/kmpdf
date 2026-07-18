package io.github.lucasfaiska.kmpdf.reader

import android.content.Context
import android.graphics.pdf.LoadParams
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRendererPreV
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.ext.SdkExtensions
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
                    val engine =
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> {
                                createModernEngine(pfd, password)
                            }

                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 13 -> {
                                createCompatEngine(pfd, password)
                            }

                            else -> {
                                if (password != null) {
                                    pfd.close()
                                    return@withContext PdfLoadStatus.Error(
                                        PdfError(
                                            PdfErrorType.UNSUPPORTED,
                                            "Password protected PDFs are not supported on this device version.",
                                        ),
                                    )
                                }
                                ModernPdfEngine(PdfRenderer(pfd))
                            }
                        }
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

    private fun createModernEngine(
        pfd: ParcelFileDescriptor,
        password: String?,
    ): AndroidPdfEngine {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val renderer =
                if (password != null) {
                    val params = LoadParams.Builder().setPassword(password).build()
                    PdfRenderer(pfd, params)
                } else {
                    PdfRenderer(pfd)
                }
            return ModernPdfEngine(renderer)
        } else {
            throw IllegalStateException("Modern renderer requires API 35+")
        }
    }

    private fun createCompatEngine(
        pfd: ParcelFileDescriptor,
        password: String?,
    ): AndroidPdfEngine {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 13
        ) {
            val renderer =
                if (password != null) {
                    val params =
                        LoadParams
                            .Builder()
                            .setPassword(password)
                            .build()
                    PdfRendererPreV(pfd, params)
                } else {
                    PdfRendererPreV(pfd)
                }
            return CompatPdfEngine(renderer)
        } else {
            throw IllegalStateException("Compat renderer requires S Extension 13")
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
