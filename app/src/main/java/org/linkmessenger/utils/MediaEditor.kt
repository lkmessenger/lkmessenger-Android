package org.linkmessenger.utils

import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import org.thoughtcrime.securesms.ApplicationContext
import java.io.ByteArrayOutputStream
import kotlin.math.floor


class MediaEditor {
    fun createSizedBitmap(uri: Uri, destinationWidth: Int,
                          destinationHeight: Int):ByteArray{
        val b = getBitmapFromUri(uri)
        val sized = scaleCenterCrop(b, destinationHeight, destinationWidth)
        return bitmapToByteArray(sized)
    }
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                ApplicationContext.applicationContext.contentResolver,
                uri
            )
            else -> {
                val source = ImageDecoder.createSource(ApplicationContext.applicationContext.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }
    /**
     * Scale the image preserving the ratio
     * @param imageToScale Image to be scaled
     * @param destinationWidth Destination width after scaling
     * @param destinationHeight Destination height after scaling
     * @return New scaled bitmap preserving the ratio
     */
    fun scalePreserveRatio(
        imageToScale: Bitmap, destinationWidth: Int,
        destinationHeight: Int
    ): Bitmap {
        return if (destinationHeight > 0 && destinationWidth > 0) {//35524
            val width: Int = imageToScale.width
            val height: Int = imageToScale.height

            //Calculate the max changing amount and decide which dimension to use
            val widthRatio = destinationWidth.toFloat() / width.toFloat()
            val heightRatio = destinationHeight.toFloat() / height.toFloat()

            //Use the ratio that will fit the image into the desired sizes
            var finalWidth = floor((width * widthRatio).toDouble()).toInt()
            var finalHeight = floor((height * widthRatio).toDouble()).toInt()
            if (finalWidth > destinationWidth || finalHeight > destinationHeight) {
                finalWidth = floor((width * heightRatio).toDouble()).toInt()
                finalHeight = floor((height * heightRatio).toDouble()).toInt()
            }

            //Scale given bitmap to fit into the desired area
            val tmp = Bitmap.createScaledBitmap(imageToScale, finalWidth, finalHeight, true)
            val tmp2 = tmp.copy(Bitmap.Config.ARGB_8888, false)

            //Created a bitmap with desired sizes
            val scaledImage: Bitmap =
                Bitmap.createBitmap(destinationWidth, destinationHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaledImage)

            //Draw background color
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

            //Calculate the ratios and decide which part will have empty areas (width or height)
            val ratioBitmap = finalWidth.toFloat() / finalHeight.toFloat()
            val destinationRatio = destinationWidth.toFloat() / destinationHeight.toFloat()
            val left: Float =
                if (ratioBitmap >= destinationRatio) 0f else (destinationWidth - finalWidth).toFloat() / 2
            val top: Float =
                if (ratioBitmap < destinationRatio) 0f else (destinationHeight - finalHeight).toFloat() / 2
            canvas.drawBitmap(tmp2, left, top, null)
            scaledImage
        } else {
            imageToScale
        }
    }
    private fun scaleCenterCrop(source: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        val xScale = newWidth.toFloat() / sourceWidth
        val yScale = newHeight.toFloat() / sourceHeight
        val scale = Math.max(xScale, yScale)

        // Now get the size of the source bitmap when scaled
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        val left = (newWidth - scaledWidth) / 2
        val top = (newHeight - scaledHeight) / 2

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        val source2 = source.copy(Bitmap.Config.ARGB_8888, false)
        val dest = Bitmap.createBitmap(newWidth, newHeight, source2.config)

        val canvas = Canvas(dest)
        canvas.drawBitmap(source2, null, targetRect, null)
        return dest
    }
}