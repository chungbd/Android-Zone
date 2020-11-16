package com.example.democamera

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ResizeBitmap {
    fun resize(bitmap: Bitmap, maxWidth: Int, maxHeight:Int):Bitmap {
        val scale =
            (maxHeight.toFloat() / bitmap.width).coerceAtMost(maxWidth.toFloat() / bitmap.height)

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeForUpload(bitmap: Bitmap):Bitmap {
        val maxHeight = 480 * 2
        val maxWidth = 640 * 2
        return resize(bitmap,maxWidth,maxHeight)
    }

    fun resizeForThumb(bitmap: Bitmap):Bitmap {
        val maxHeight = 120
        val maxWidth = 180
        return resize(bitmap,maxWidth,maxHeight)
    }


    fun getThumb(bitmap: Bitmap, outputFile: File, callback: (result: String) -> Unit) {
        Thread {
            val resizeBitmap = resizeForThumb(bitmap)
            saveImage(resizeBitmap,outputFile,callback)
        }.start()
    }

    fun saveImage(bitmap: Bitmap, outputFile: File, callback: (result: String) -> Unit) {
        Thread {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpStream)

            FileOutputStream(outputFile).use {
                it.write(bmpStream.toByteArray())
                callback(outputFile.absolutePath)
            }
        }.start()
    }

    fun downQuality(bitmap: Bitmap, outputFile: File, callback: (result: String) -> Unit) {
        Thread {
            val maxImageSize = 1024000
            val bmpStream = ByteArrayOutputStream()

            if (bitmap.byteCount > maxImageSize) {
                var streamLength = maxImageSize
                var compressQuality = 100

                while (streamLength >= maxImageSize) {
                    bmpStream.use {
                        it.flush()
                        it.reset()
                    }

                    compressQuality -= 8

                    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                    streamLength = bmpStream.toByteArray().size
                }
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpStream)
            }

            FileOutputStream(outputFile).use {
                it.write(bmpStream.toByteArray())
                callback(outputFile.absolutePath)
            }
        }.start()
    }
}