package com.kronos.multiplatform.weatherapp.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

/**
 * Decodes [inputStream] downsampled to roughly [reqWidth]x[reqHeight], avoiding a full-resolution
 * allocation. The stream is fully buffered first since [BitmapFactory] needs to read it twice
 * (once for bounds, once for the sampled decode) and network streams aren't seekable.
 */
fun decodeSampledBitmapFromStream(
    inputStream: InputStream,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    val bytes = inputStream.use { it.readBytes() }

    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}
