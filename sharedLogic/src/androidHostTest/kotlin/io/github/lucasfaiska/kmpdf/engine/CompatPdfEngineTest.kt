package io.github.lucasfaiska.kmpdf.engine

import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompatPdfEngineTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `given a valid PDF file when initialized then it should not throw exception`() =
        runTest {
            val file = getSamplePdfFile()
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val engine = CompatPdfEngine(pfd, null)

            assertNotNull(engine)
            assertEquals(1, engine.pageCount)

            engine.close()
        }

    @Test
    fun `given a valid PDF file when opening a page then it should return a page with dimensions`() =
        runTest {
            val file = getSamplePdfFile()
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val engine = CompatPdfEngine(pfd, null)

            val page = engine.openPage(0)
            assertNotNull(page)
            assertEquals(595, page.width)
            assertEquals(841, page.height)

            page.close()
            engine.close()
        }

    private fun getSamplePdfFile(): File {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream("sample.pdf")
                ?: throw IllegalStateException("sample.pdf not found in resources")
        val file = File(context.cacheDir, "test_sample_compat.pdf")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
