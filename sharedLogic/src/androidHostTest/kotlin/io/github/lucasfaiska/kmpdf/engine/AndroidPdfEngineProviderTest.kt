package io.github.lucasfaiska.kmpdf.engine

import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
class AndroidPdfEngineProviderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    @Config(sdk = [34])
    fun `given api 34 when providing engine then it should return an engine instance`() {
        val file = getSamplePdfFile()
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        
        val engine = AndroidPdfEngineProvider.provideEngine(pfd, null)
        
        assertNotNull(engine)
        engine.close()
    }

    @Test
    @Config(sdk = [28])
    fun `given api 28 when providing engine then it should return modern engine`() {
        val file = getSamplePdfFile()
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        
        val engine = AndroidPdfEngineProvider.provideEngine(pfd, null)
        
        assertTrue(engine is ModernPdfEngine)
        engine.close()
    }

    private fun getSamplePdfFile(): File {
        val inputStream = javaClass.classLoader?.getResourceAsStream("sample.pdf")
            ?: throw IllegalStateException("sample.pdf not found in resources")
        val file = File(context.cacheDir, "provider_test_sample.pdf")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
