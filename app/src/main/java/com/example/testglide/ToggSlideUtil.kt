package com.example.testglide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView


fun ImageView.getBitmapFromImageView(): Bitmap? {
    val drawable = this.drawable ?: return null

    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        // Otherwise, create a Bitmap from the drawable
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

private fun calculateDynamicFrameThickness(
    frameWidth: Int,
    frameHeight: Int,
    imageWidth: Int,
    imageHeight: Int
): Int {
    // Take the smaller dimension to determine proportional thickness
    val smallestDimension = minOf(frameWidth, frameHeight, imageWidth, imageHeight)

    // Calculate thickness as a proportion of the smallest dimension
    var frameThickness = (smallestDimension * 0.03).toInt()

    // Ensure the thickness is within a reasonable range
    val minThickness = 10   // Set a minimum thickness (in pixels)
    val maxThickness = 50   // Set a maximum thickness (in pixels)

    // Clamp thickness within the defined range
    frameThickness = frameThickness.coerceIn(minThickness, maxThickness)

    return frameThickness
}

/**
 * Scales the image to fill the inner area of the frame, ensuring the image reaches the frame edges.
 * @param imageBitmap The Bitmap of the image to be framed.
 * @param frameBitmap The Bitmap of the frame to place around the image.
 * @return A new Bitmap with the image scaled to fill the inner area of the frame.
 */
fun createScaledFramedImage(imageBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
    // Calculate frame thickness based on the target resolution
    val frameThickness = calculateDynamicFrameThickness(
        frameBitmap.width,
        frameBitmap.height,
        imageBitmap.width,
        imageBitmap.height
    )


    // Define the available space inside the frame for the image
    val availableWidth = imageBitmap.width - 2 * frameThickness
    val availableHeight = imageBitmap.height - 2 * frameThickness

    // Scale the image to fit within the available space in the frame
    val scaledImage = Bitmap.createScaledBitmap(
        imageBitmap,
        availableWidth,
        availableHeight,
        true
    )

    // Create a new bitmap with the fixed dimensions (800 x 1200)
    val combinedBitmap =
        Bitmap.createBitmap(imageBitmap.width, imageBitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(combinedBitmap)

    // Draw the scaled image centered inside the frame
    canvas.drawBitmap(scaledImage, frameThickness.toFloat(), frameThickness.toFloat(), null)

    // Draw the frame around the scaled image, keeping the frame thickness intact
    drawFrameAroundImage(canvas, frameBitmap, frameThickness, imageBitmap.width, imageBitmap.height)

    return combinedBitmap
}

/**
 * Draws the frame around the canvas, keeping borders intact and trimming empty areas around the image.
 * @param canvas Canvas to draw the frame on.
 * @param frameBitmap Bitmap of the frame.
 * @param frameThickness Thickness of the frame border.
 * @param combinedWidth Total width of the final image with frame.
 * @param combinedHeight Total height of the final image with frame.
 */
private fun drawFrameAroundImage(
    canvas: Canvas,
    frameBitmap: Bitmap,
    frameThickness: Int,
    combinedWidth: Int,
    combinedHeight: Int
) {
    val paint = Paint()

    // Define corners of the frame
    val topLeftCorner = Bitmap.createBitmap(frameBitmap, 0, 0, frameThickness, frameThickness)
    val topRightCorner = Bitmap.createBitmap(
        frameBitmap,
        frameBitmap.width - frameThickness,
        0,
        frameThickness,
        frameThickness
    )
    val bottomLeftCorner = Bitmap.createBitmap(
        frameBitmap,
        0,
        frameBitmap.height - frameThickness,
        frameThickness,
        frameThickness
    )
    val bottomRightCorner = Bitmap.createBitmap(
        frameBitmap,
        frameBitmap.width - frameThickness,
        frameBitmap.height - frameThickness,
        frameThickness,
        frameThickness
    )

    // Define edges and scale them to match the image width and height
    val topEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            frameThickness,
            0,
            frameBitmap.width - 2 * frameThickness,
            frameThickness
        ),
        combinedWidth - 2 * frameThickness,
        frameThickness,
        false
    )
    val bottomEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            frameThickness,
            frameBitmap.height - frameThickness,
            frameBitmap.width - 2 * frameThickness,
            frameThickness
        ),
        combinedWidth - 2 * frameThickness,
        frameThickness,
        false
    )
    val leftEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            0,
            frameThickness,
            frameThickness,
            frameBitmap.height - 2 * frameThickness
        ),
        frameThickness,
        combinedHeight - 2 * frameThickness,
        false
    )
    val rightEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            frameBitmap.width - frameThickness,
            frameThickness,
            frameThickness,
            frameBitmap.height - 2 * frameThickness
        ),
        frameThickness,
        combinedHeight - 2 * frameThickness,
        false
    )

    // Draw corners
    canvas.drawBitmap(topLeftCorner, 0f, 0f, paint)
    canvas.drawBitmap(topRightCorner, (combinedWidth - frameThickness).toFloat(), 0f, paint)
    canvas.drawBitmap(bottomLeftCorner, 0f, (combinedHeight - frameThickness).toFloat(), paint)
    canvas.drawBitmap(
        bottomRightCorner,
        (combinedWidth - frameThickness).toFloat(),
        (combinedHeight - frameThickness).toFloat(),
        paint
    )

    // Draw edges
    canvas.drawBitmap(topEdge, frameThickness.toFloat(), 0f, paint)
    canvas.drawBitmap(
        bottomEdge,
        frameThickness.toFloat(),
        (combinedHeight - frameThickness).toFloat(),
        paint
    )
    canvas.drawBitmap(leftEdge, 0f, frameThickness.toFloat(), paint)
    canvas.drawBitmap(
        rightEdge,
        (combinedWidth - frameThickness).toFloat(),
        frameThickness.toFloat(),
        paint
    )
}