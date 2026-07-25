package io.github.lucasfaiska.kmpdf.reader

import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfErrorType
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfReaderErrorTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val reader = AndroidPdfReader(context, Dispatchers.Unconfined)

    @Test
    fun `given a security exception without password when processing then it should return password required`() =
        runTest {
            val result = invokeHandlePdfEngineError(SecurityException(), null)
            assertTrue(result is PdfLoadStatus.PasswordRequired)
        }

    @Test
    fun `given a security exception with password when processing then it should return invalid password`() =
        runTest {
            val result = invokeHandlePdfEngineError(SecurityException(), "wrong_password")
            assertTrue(result is PdfLoadStatus.InvalidPassword)
        }

    @Test
    fun `given a generic exception when processing then it should return generic error status`() =
        runTest {
            val result = invokeHandlePdfEngineError(Exception("Native crash"), null)
            assertTrue(result is PdfLoadStatus.Error)
            assertEquals(PdfErrorType.GENERIC, (result as PdfLoadStatus.Error).error.type)
        }

    @Test
    fun `given a global io exception when handling then it should delete temp file and return io error`() =
        runTest {
            val tempFile = File.createTempFile("error_test", ".pdf")
            val result = invokeHandleGlobalError(tempFile, Exception("Disk full"))

            assertTrue(result is PdfLoadStatus.Error)
            assertEquals(PdfErrorType.IO_ERROR, (result as PdfLoadStatus.Error).error.type)
            assertTrue(!tempFile.exists())
        }

    private fun invokeHandlePdfEngineError(
        e: Exception,
        password: String?,
    ): PdfLoadStatus {
        val method =
            AndroidPdfReader::class.java.getDeclaredMethod(
                "handlePdfEngineError",
                Exception::class.java,
                String::class.java,
            )
        method.isAccessible = true
        return method.invoke(reader, e, password) as PdfLoadStatus
    }

    private fun invokeHandleGlobalError(
        tempFile: File,
        e: Exception,
    ): PdfLoadStatus {
        val method =
            AndroidPdfReader::class.java.getDeclaredMethod(
                "handleGlobalError",
                File::class.java,
                Exception::class.java,
            )
        method.isAccessible = true
        return method.invoke(reader, tempFile, e) as PdfLoadStatus
    }
}
