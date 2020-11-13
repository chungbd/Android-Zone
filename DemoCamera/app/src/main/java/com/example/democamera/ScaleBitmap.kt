package com.example.democamera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ScaleBitmap {
    @JvmStatic
    fun scaleBitmapCallBack(file: File, callback: (result: String?) -> Unit) {
        Thread {
            val imagePath: String = file.absolutePath // photoFile is a File class.
            val myBitmap = BitmapFactory.decodeFile(imagePath)
            val orientedBitmap = ExifUtil.rotateBitmap(imagePath, myBitmap)

            val MAX_IMAGE_SIZE = 1024000

            if (file.length() > MAX_IMAGE_SIZE) {
                var streamLength = MAX_IMAGE_SIZE
                var compressQuality = 100
                val bmpStream = ByteArrayOutputStream()
                while (streamLength >= MAX_IMAGE_SIZE) {
                    bmpStream.use {
                        it.flush()
                        it.reset()
                    }

                    compressQuality -= 8

                    orientedBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                    streamLength = bmpStream.toByteArray().size
                }

                FileOutputStream(file).use {
                    it.write(bmpStream.toByteArray())
                    callback(file.absolutePath)
                }
            } else {
                callback(file.absolutePath)
            }
        }.start()
    }

    @JvmStatic
    fun scaleBitmap(file: File) {
        val MAX_IMAGE_SIZE = 1024000
        if (file.length() > MAX_IMAGE_SIZE) {
            var streamLength = MAX_IMAGE_SIZE
            var compressQuality = 100
            val bmpStream = ByteArrayOutputStream()
            while (streamLength >= MAX_IMAGE_SIZE) {
                bmpStream.use {
                    it.flush()
                    it.reset()
                }

                compressQuality -= 12
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options())
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                streamLength = bmpStream.toByteArray().size
            }

            FileOutputStream(file).use {
                it.write(bmpStream.toByteArray())
            }
        }
    }


}