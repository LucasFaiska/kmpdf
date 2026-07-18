package io.github.lucasfaiska.kmpdf.reader

import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfLoadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidPdfReaderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val reader = AndroidPdfReader(context, Dispatchers.Unconfined)

    @Test
    fun `given a valid PDF byte array when opening the document then the document should be created`() =
        runTest {
            val minimalPdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2d, 0x31, 0x2e, 0x30, 0x0a)
            val status = reader.open(minimalPdfBytes)

            if (status is PdfLoadStatus.Error) {
                println("Load Error: ${status.error.type}")
            }
            assertTrue("Expected Success but got $status", status is PdfLoadStatus.Success)
            val document = (status as PdfLoadStatus.Success).document
            assertNotNull(document)

            document.close()
        }

    @Test
    fun `given an encrypted PDF byte array when opening without password then password required status is returned`() =
        runTest {
            // Minimal PDF with /Encrypt
            val encryptedPdfBytes = " %PDF-1.0\n/Encrypt 123 0 R\n ".toByteArray()
            val status = reader.open(encryptedPdfBytes)

            assertTrue(status is PdfLoadStatus.PasswordRequired)
        }
}
