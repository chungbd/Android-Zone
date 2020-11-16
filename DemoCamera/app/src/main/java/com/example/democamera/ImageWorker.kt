package com.example.democamera

import android.graphics.Bitmap
import androidx.annotation.UiThread
import java.io.File

typealias ProcessComplete = (Boolean) -> Void
typealias ImageProcessComplete = (ImageInfor) -> Void

interface ProcessHanded {
    @UiThread
    fun onProcessing(status: Boolean)
}

interface ImageProcessHanded {
    @UiThread
    fun onComplete(infor: ImageInfor)
}


class ImageInfor(var originalUrl: String, var resizeUrl: String, var thumbUrl: String) {
    var isProcessing: Boolean = false

    override fun toString(): String {
        return originalUrl + "\n" + resizeUrl + "\n" + this.thumbUrl
    }
}

class ImageWorker(var directory: File) {

    private var isProcessing: Boolean = false
    set(value) {
        onCompleteProcess?.onProcessing(value)
    }

    var onCompleteImageProcess: ImageProcessHanded? = null
    var onCompleteProcess: ProcessHanded? = null

    fun addBitmap(img: Bitmap) {
        isProcessing = true

        getInfor(img)
    }

    fun setCompleteProcess(complete: ProcessHanded) {
        this.onCompleteProcess = complete
    }

    fun getInfor(bitmap: Bitmap) {
        val fName = System.currentTimeMillis().toString()
        val extension = ".webp"
        val originalName = fName + extension
        val thumbName = fName + "_thumb" + extension
        val resizeName = fName + "_resize" + extension

        val originalFile = File(directory, originalName)
        val thumbFile = File(directory, thumbName)
        val resizeFile = File(directory, resizeName)

        val uploadBitmap = ResizeBitmap.resizeForUpload(bitmap)

        ResizeBitmap.downQuality(uploadBitmap,resizeFile) { resize ->
            ResizeBitmap.getThumb(uploadBitmap,thumbFile) { thumb ->
                ResizeBitmap.saveImage(bitmap, originalFile) { original ->
                    val info = ImageInfor(original, resize, thumb)
                    onCompleteImageProcess?.onComplete(info)
                    isProcessing = false
                }
            }
        }

    }

}