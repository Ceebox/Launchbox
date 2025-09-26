package com.chadderbox.launchbox.utils.compose

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.annotation.Keep
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.createBitmap

object BitmapHelper {
    @Keep
    @JvmStatic
    fun bitmapToImageBitmap(bitmap: Bitmap): ImageBitmap {
        return bitmap.asImageBitmap()
    }

    @Keep
    @JvmStatic
    fun bitmapPainter(imageBitmap: ImageBitmap): Painter {
        return BitmapPainter(imageBitmap)
    }

    @Keep
    @JvmStatic
    fun bitmapToBitmapPainter(bitmap: Bitmap) : Painter {
        return bitmapPainter(bitmapToImageBitmap(bitmap))
    }

    @Keep
    @JvmStatic
    fun drawableToPainter(drawable: Drawable): BitmapPainter? {
        val bitmap: Bitmap? = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                // Create bitmap from other drawables
                val b = createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1)
                )
                val canvas = android.graphics.Canvas(b)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                b
            }
        }
        return bitmap?.let { BitmapPainter(it.asImageBitmap()) }
    }
}
