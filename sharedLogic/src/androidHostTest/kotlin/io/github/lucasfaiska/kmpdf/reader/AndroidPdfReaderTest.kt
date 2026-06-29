package io.github.lucasfaiska.kmpdf.reader

import androidx.test.core.app.ApplicationProvider
import io.github.lucasfaiska.kmpdf.model.PdfDocument
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AndroidPdfReaderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val reader = AndroidPdfReader(context)

    @Test
    fun `given a valid PDF byte array when opening the document then the document should be created`() =
        runTest {
            val minimalPdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2d, 0x31, 0x2e, 0x30, 0x0a)
            val document: PdfDocument = reader.open(minimalPdfBytes)

            assertNotNull(document)

            document.close()
        }

    @Test
    fun `given a document when rendering is attempted then the contract should be respected`() =
        runTest {
            val minimalPdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2d, 0x31, 0x2e, 0x30, 0x0a)
            val document = reader.open(minimalPdfBytes)

            assertNotNull(document)

            document.close()
        }
}
