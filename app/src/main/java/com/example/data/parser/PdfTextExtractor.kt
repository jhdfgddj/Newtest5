package com.example.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern

data class PdfExtractionResult(
    val extractedText: String,
    val pageCount: Int,
    val pageBitmaps: List<Bitmap> = emptyList(),
    val isScanned: Boolean = false,
    val errorMessage: String? = null
)

class PdfTextExtractor(private val context: Context) {

    suspend fun extractFromUri(uri: Uri): PdfExtractionResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext PdfExtractionResult("", 0, errorMessage = "Could not open file stream.")

            // Create temporary file to pass to ParcelFileDescriptor for PdfRenderer & text inspection
            val tempFile = File(context.cacheDir, "temp_routine_${System.currentTimeMillis()}.pdf")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            extractFromFile(tempFile)
        } catch (e: Exception) {
            PdfExtractionResult("", 0, errorMessage = "Error reading PDF file: ${e.localizedMessage}")
        }
    }

    suspend fun extractFromFile(file: File): PdfExtractionResult = withContext(Dispatchers.IO) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        val pageBitmaps = mutableListOf<Bitmap>()

        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount

            // Render first 3 pages into bitmaps for AI analysis or preview
            val maxPagesToRender = minOf(pageCount, 3)
            for (i in 0 until maxPagesToRender) {
                pdfRenderer.openPage(i).use { page ->
                    val bitmap = Bitmap.createBitmap(
                        page.width / 2,
                        page.height / 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pageBitmaps.add(bitmap)
                }
            }

            // Extract text from stream data of the PDF file
            val fileBytes = file.readBytes()
            val extractedText = extractTextFromPdfBytes(fileBytes)

            val isScanned = extractedText.trim().length < 50 && pageCount > 0

            PdfExtractionResult(
                extractedText = extractedText,
                pageCount = pageCount,
                pageBitmaps = pageBitmaps,
                isScanned = isScanned
            )
        } catch (e: Exception) {
            PdfExtractionResult("", 0, errorMessage = "Failed to parse PDF structure: ${e.localizedMessage}")
        } finally {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
                file.delete()
            } catch (ignored: Exception) {}
        }
    }

    private fun extractTextFromPdfBytes(bytes: ByteArray): String {
        val sb = StringBuilder()
        val contentStr = String(bytes, Charsets.ISO_8859_1)

        // Find Tj and TJ operators in PDF text streams
        val tjPattern = Pattern.compile("\\(([^()]*)\\)\\s*Tj", Pattern.CASE_INSENSITIVE)
        val tjMatcher = tjPattern.matcher(contentStr)
        while (tjMatcher.find()) {
            val text = tjMatcher.group(1)
            if (!text.isNullOrBlank()) {
                sb.append(cleanPdfText(text)).append(" ")
            }
        }

        val tjArrayPattern = Pattern.compile("\\[((?:\\([^()]*\\)|[^\\]])*)\\]\\s*TJ", Pattern.CASE_INSENSITIVE)
        val tjArrayMatcher = tjArrayPattern.matcher(contentStr)
        while (tjArrayMatcher.find()) {
            val arrayContent = tjArrayMatcher.group(1) ?: ""
            val innerPattern = Pattern.compile("\\(([^()]*)\\)")
            val innerMatcher = innerPattern.matcher(arrayContent)
            while (innerMatcher.find()) {
                val text = innerMatcher.group(1)
                if (!text.isNullOrBlank()) {
                    sb.append(cleanPdfText(text))
                }
            }
            sb.append("\n")
        }

        var result = sb.toString()
        if (result.isBlank()) {
            // Fallback: search for readable text blocks inside Stream markers
            val streamPattern = Pattern.compile("stream[\\r\\n]+(.*?)endstream", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
            val streamMatcher = streamPattern.matcher(contentStr)
            while (streamMatcher.find()) {
                val streamData = streamMatcher.group(1) ?: ""
                val asciiText = streamData.replace("[^\\x20-\\x7E\\xA0-\\xFF\\n\\r\\t]".toRegex(), " ")
                if (asciiText.trim().length > 20) {
                    sb.append(asciiText).append("\n")
                }
            }
            result = sb.toString()
        }

        return result
    }

    private fun cleanPdfText(text: String): String {
        return text
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
    }
}
