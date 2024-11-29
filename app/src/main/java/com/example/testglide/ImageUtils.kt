package com.example.testglide

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

object ImageUtils {


    // Function to handle rotation and downscale if the Bitmap is too large
    fun handleRotationAndScale(bitmap: Bitmap): Bitmap {
        val maxDimension = 1080 // Adjust this max size as needed

        // Check if the bitmap needs scaling
        val scaledBitmap = downscaleBitmapIfNeeded(bitmap, maxDimension, maxDimension)

        // Rotate the bitmap if needed
        return rotateBitmapIfNecessary(scaledBitmap)
    }

    // Function to downscale the bitmap if it exceeds the max dimensions
    private fun downscaleBitmapIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width > maxWidth || height > maxHeight) {
            val scaleFactor = Math.min(maxWidth / width.toFloat(), maxHeight / height.toFloat())
            val newWidth = (width * scaleFactor).toInt()
            val newHeight = (height * scaleFactor).toInt()
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }

        return bitmap // No scaling needed
    }

    // Function to rotate the bitmap if it’s in portrait mode
    private fun rotateBitmapIfNecessary(bitmap: Bitmap): Bitmap {
        return if (bitmap.height > bitmap.width) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix(), true)
        } else {
            bitmap
        }
    }

    // Function to scale the frame bitmap to match the dimensions of the original bitmap
    fun scaleBitmapToMatch(original: Bitmap, frame: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(frame, original.width, original.height, false)
    }

    // Function to add the sticker (frame) on top of the original bitmap
    fun addStickerToImage(bitmapOriginal: Bitmap, frameBitmap: Bitmap): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(
            bitmapOriginal.width,
            bitmapOriginal.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmapOriginal, 0f, 0f, null)
        canvas.drawBitmap(frameBitmap, 0f, 0f, null)
        return combinedBitmap
    }

//
//    private fun combineImageAndFrame(bitmapOriginal: Bitmap, frameBitmap: Bitmap): Bitmap {
//        val combinedBitmap = Bitmap.createBitmap(
//            bitmapOriginal.width,
//            bitmapOriginal.height,
//            Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(combinedBitmap)
//
//        // Draw the original image first
//        canvas.drawBitmap(bitmapOriginal, 0f, 0f, null)
//
//        // Draw the scaled sticker on top of the image
//        canvas.drawBitmap(frameBitmap, 0f, 0f, null)
//
//        return combinedBitmap
//    }

    /**
     * Resizes the Bitmap if it exceeds the maximum width or height.
     */
    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            // No resizing needed
            return bitmap
        }

        // Calculate the scaling factor to fit within max dimensions
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    private fun combineImageAndFrame(bitmapOriginal: Bitmap, frameBitmap: Bitmap): Bitmap {
        val scaledFrame = Bitmap.createScaledBitmap(
            frameBitmap,
            bitmapOriginal.width,
            bitmapOriginal.height,
            false
        )

        // Create a new bitmap to combine image and frame
        val combinedBitmap = Bitmap.createBitmap(
            bitmapOriginal.width,
            bitmapOriginal.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combinedBitmap)

        // Draw the original image first
        canvas.drawBitmap(bitmapOriginal, 0f, 0f, null)

        // Draw the scaled frame on top of the image
        canvas.drawBitmap(scaledFrame, 0f, 0f, null)

        return combinedBitmap
    }
}
