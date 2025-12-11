package com.barikoi.cnlapp.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.ResponseBody
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    private fun getFileName(fileName: String, etx: String): String {
        return "$fileName (${getCurrentDateForFileName()}) - ${System.currentTimeMillis()}.$etx"
    }

    private fun getCurrentDateForFileName(): String {
        val currentDateTime = LocalDateTime.now()
        val fileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss_SS")
        return currentDateTime.format(fileNameFormatter)
    }

    fun saveFileToDownloads(
        context: Context,
        body: ResponseBody,
        fileName: String,
        etx: String
    ): String {
        val fileNameWithExtension = getFileName(fileName, etx)
        val downloadsDir = Environment.DIRECTORY_DOWNLOADS
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileNameWithExtension)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, downloadsDir)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(getDownloadsUri(), contentValues)

        uri?.let {
            resolver.openOutputStream(uri)?.use { outputStream ->
                writeStreamToOutput(body.byteStream(), outputStream)
            }
        }
        val downloadsPath = Environment.getExternalStoragePublicDirectory(downloadsDir).absolutePath
        return "$downloadsPath/$fileNameWithExtension"
    }

    private fun getDownloadDirPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }

    fun getDownloadFilePath(fileName: String, etx: String): String {
        return "${getDownloadDirPath()}/${getFileName(fileName, etx)}"
    }

    private fun writeStreamToOutput(inputStream: InputStream, outputStream: OutputStream) {
        try {
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }

    fun getDownloadsUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            // Fallback for older versions (example)
            MediaStore.Files.getContentUri("external")
        }
    }
}