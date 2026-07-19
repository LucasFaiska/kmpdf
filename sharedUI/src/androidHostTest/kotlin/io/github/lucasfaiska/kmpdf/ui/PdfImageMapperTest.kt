package io.github.lucasfaiska.kmpdf.ui

import androidx.compose.ui.graphics.asAndroidBitmap
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class PdfImageMapperTest {
    @Test
    fun `given a byte array when mapping to image bitmap then it should create a bitmap with correct dimensions`() {
        val width = 100
        val height = 200
        val bytes = ByteArray(width * height * 4)

        val bitmap = bytes.toImageBitmap(width, height)

        val androidBitmap = bitmap.asAndroidBitmap()
        assertEquals(width, androidBitmap.width)
        assertEquals(height, androidBitmap.height)
    }
}
