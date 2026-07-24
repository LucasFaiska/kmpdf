package io.github.lucasfaiska.kmpdf.engine

import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
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
            // PdfRendererPreV is only available in newer SDKs and Robolectric might not fully support it yet
            assumeTrue(Build.VERSION.SDK_INT >= 35)

            val file = getSamplePdfFile()
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val engine = CompatPdfEngine(pfd, null)

            assertNotNull(engine)
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
