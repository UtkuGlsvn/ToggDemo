package com.example.testglide

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.testglide.ImageUtils.handleRotationAndScale
import com.example.testglide.ImageUtils.scaleBitmapToMatch

class MainActivity : AppCompatActivity() {


    private val imgUrl =
        //          "https://changegl.co.uk/wp-content/uploads/2017/12/600x300.png"
        "https://kinsta.com/wp-content/uploads/2020/06/half-page-1-1.png"
    //    "https://plus.unsplash.com/premium_photo-1661962685099-c6a685e6c61d?w=900&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8aG9yaXpvbnRhbHxlbnwwfHwwfHx8MA%3D%3D"

    private lateinit var photoImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoImageView = findViewById(R.id.frameImageView)


        val frameDrawable = ContextCompat.getDrawable(
            this,
            R.drawable.frame_test // you send me photo 9png
        ) // Replace with your frame resource

        Glide.with(this)
            .asBitmap()
            .load(imgUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {


                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

    }

}

// I use it ToggSlide Util
/*
val combinedBitmap = createScaledFramedImage(it, frameBitmap)
                binding.imgViewCover.setImageBitmap(combinedBitmap)

Like this use it also i have try it frame cut
 merge cut merge but slowly maybe need optimize and not %100 work
 */

// My try code not important


fun calculateFrameAndImageCoordinates(
    frameBitmap: Bitmap,
    imageBitmap: Bitmap,
    borderThickness: Int
): Pair<Bitmap, Rect> {
    // Step 1: Calculate the target size for the image (after border)
    val frameWidth = frameBitmap.width
    val frameHeight = frameBitmap.height

    val imageWidth = imageBitmap.width
    val imageHeight = imageBitmap.height

    // Calculate the available space for the photo inside the frame (consider border thickness)
    val availableWidth = frameWidth - 2 * borderThickness
    val availableHeight = frameHeight - 2 * borderThickness

    // Step 2: Scale the image to fit within the available space inside the frame
    val aspectRatioImage = imageWidth.toFloat() / imageHeight.toFloat()
    val aspectRatioFrame = availableWidth.toFloat() / availableHeight.toFloat()

    var scaledImageWidth = availableWidth
    var scaledImageHeight = availableHeight

    if (aspectRatioImage > aspectRatioFrame) {
        // Scale based on width
        scaledImageWidth = availableWidth
        scaledImageHeight = (scaledImageWidth / aspectRatioImage).toInt()
    } else {
        // Scale based on height
        scaledImageHeight = availableHeight
        scaledImageWidth = (scaledImageHeight * aspectRatioImage).toInt()
    }

    // Create the scaled bitmap
    val scaledImageBitmap =
        Bitmap.createScaledBitmap(imageBitmap, scaledImageWidth, scaledImageHeight, true)

    // Step 3: Calculate the position to center the image within the frame
    val left = (frameWidth - scaledImageWidth) / 2
    val top = (frameHeight - scaledImageHeight) / 2

    // Return the scaled image bitmap and the position of the image inside the frame
    val rect = Rect(left, top, left + scaledImageWidth, top + scaledImageHeight)
    return Pair(scaledImageBitmap, rect)
}

fun mergeFrameAndImage(
    frameBitmap: Bitmap,
    imageBitmap: Bitmap,
    borderThickness: Int
): Bitmap {
    // Step 1: Get the frame coordinates and the scaled image
    val (scaledImageBitmap, imageRect) = calculateFrameAndImageCoordinates(
        frameBitmap,
        imageBitmap,
        borderThickness
    )

    // Step 2: Create a new bitmap that combines both the frame and the scaled image
    val outputBitmap =
        Bitmap.createBitmap(frameBitmap.width, frameBitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    val paint = Paint()

    // Step 3: Draw the frame (we assume frameBitmap is a border with transparent inner area)
    canvas.drawBitmap(frameBitmap, 0f, 0f, paint)

    // Step 4: Draw the image within the frame, at the calculated position
    canvas.drawBitmap(scaledImageBitmap, imageRect.left.toFloat(), imageRect.top.toFloat(), paint)

    return outputBitmap
}


fun addFrameToImage(context: Context, image: Bitmap, frameResId: Int): Bitmap {
    // Load the 9-patch drawable
    val frameDrawable = context.resources.getDrawable(frameResId, null) as NinePatchDrawable

    // Get dimensions of the original image
    val imageWidth = image.width
    val imageHeight = image.height

    // Create a new bitmap with space for the frame
    val framedBitmap = Bitmap.createBitmap(
        imageWidth, imageHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(framedBitmap)

    // Set bounds for the frame to cover the entire image
    frameDrawable.setBounds(0, 0, imageWidth, imageHeight)

    // Draw the 9-patch frame
    frameDrawable.draw(canvas)

    // Draw the image inside the frame
    canvas.drawBitmap(image, 0f, 0f, null)

    return framedBitmap
}

fun dpToPx(dp: Float): Int {
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    ).toInt()
    Log.d("createFramedImage", "dpToPx: $dp dp = $px px")
    return px
}

fun calculateFrameThickness(imageWidth: Int, imageHeight: Int): Int {
    // Create an aspect ratio from the image dimensions
    val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

    // Adjust frame thickness based on the aspect ratio of the image
    return when {
        aspectRatio > 2 -> dpToPx(6f) // For very wide images
        aspectRatio > 1.5 -> dpToPx(10f) // For medium-width images
        else -> dpToPx(15f) // For smaller or portrait images
    }
    // i try static , dynamic not work all of them :)
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


fun createFramedImage(imageBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
    // Get the fixed frame thickness (in dp)
    val frameThickness = 15
    //calculateFrameThickness(imageBitmap.width, imageBitmap.height) // Fixed frame thickness
    Log.i("createFramedImage", "Frame Thickness: $frameThickness")

    // Get the dimensions of the original image
    val imageWidth = imageBitmap.width
    val imageHeight = imageBitmap.height

    // Ensure the frame size adjusts according to the image
    val availableWidth = imageWidth + 2 * frameThickness  // Increase width by the frame thickness
    val availableHeight = imageHeight + 2 * frameThickness // Increase height by the frame thickness
    Log.i(
        "createFramedImage",
        "Available Width: $availableWidth, Available Height: $availableHeight"
    )

    // Create a new Bitmap with the adjusted size to fit the frame
    val finalBitmap = Bitmap.createBitmap(availableWidth, availableHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(finalBitmap)

    // Set the frame's bounding box (position it around the image)
    val frameLeft = 0f
    val frameTop = 0f
    val frameRight = availableWidth.toFloat()
    val frameBottom = availableHeight.toFloat()

    // Create a Paint object for drawing the frame
    val paint = Paint()

    // Draw the frame around the image (only the edges)
    drawFrameAroundImage(
        canvas,
        frameBitmap,
        frameThickness,
        frameLeft,
        frameTop,
        frameRight,
        frameBottom
    )

    // Set the position for the image inside the frame
    val scaledImageLeft = frameThickness.toFloat()
    val scaledImageTop = frameThickness.toFloat()
    Log.i("createFramedImage", "Final Bitmap created with frame")

    // Draw the image inside the frame
    canvas.drawBitmap(imageBitmap, scaledImageLeft, scaledImageTop, paint)

    // Return the final bitmap with the frame and image
    return finalBitmap
}

fun drawFrameAroundImage(
    canvas: Canvas,
    frameBitmap: Bitmap,
    frameThickness: Int,
    frameLeft: Float,
    frameTop: Float,
    frameRight: Float,
    frameBottom: Float
) {
    val paint = Paint()

    // Draw the four corners of the frame (top-left, top-right, bottom-left, bottom-right)
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

    // Draw the top, bottom, left, and right edges of the frame
    val topEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            frameThickness,
            0,
            frameBitmap.width - 2 * frameThickness,
            frameThickness
        ),
        (frameRight - frameLeft - 2 * frameThickness).toInt(),
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
        (frameRight - frameLeft - 2 * frameThickness).toInt(),
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
        (frameBottom - frameTop - 2 * frameThickness).toInt(),
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
        (frameBottom - frameTop - 2 * frameThickness).toInt(),
        false
    )

    // Draw the four corners on the canvas
    canvas.drawBitmap(topLeftCorner, frameLeft, frameTop, paint)
    canvas.drawBitmap(topRightCorner, frameRight - frameThickness, frameTop, paint)
    canvas.drawBitmap(bottomLeftCorner, frameLeft, frameBottom - frameThickness, paint)
    canvas.drawBitmap(
        bottomRightCorner,
        frameRight - frameThickness,
        frameBottom - frameThickness,
        paint
    )

    // Draw the four edges on the canvas
    canvas.drawBitmap(topEdge, frameLeft + frameThickness, frameTop, paint)
    canvas.drawBitmap(bottomEdge, frameLeft + frameThickness, frameBottom - frameThickness, paint)
    canvas.drawBitmap(leftEdge, frameLeft, frameTop + frameThickness, paint)
    canvas.drawBitmap(rightEdge, frameRight - frameThickness, frameTop + frameThickness, paint)
}

// Çerçeveli resmi oluşturma fonksiyonu
// dp -> px dönüşümü için fonksiyon (sabit 40px değerinde çerçeve kalınlığı)
//
//fun createFramedImage(imageBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
//    // Sabit bir çerçeve kalınlığı (dp cinsinden)
//    val x = if (imageBitmap.width > imageBitmap.height) {
//        imageBitmap.width / imageBitmap.height
//    } else
//        imageBitmap.height / imageBitmap.width
//    val frameThickness = dpToPx((10f * x))  // Sabit bir çerçeve kalınlığı (40px)
//    Log.d("createFramedImage", "Frame thickness (px): $frameThickness")
//
//    val imageWidth = imageBitmap.width
//    val imageHeight = imageBitmap.height
//
//    Log.d("createFramedImage", "Image width: $imageWidth, Image height: $imageHeight")
//
//    // Çerçevenin toplam boyutları: resim boyutlarına çerçeve kalınlıklarını ekliyoruz
//    val combinedWidth = imageWidth + 2 * frameThickness
//    val combinedHeight = imageHeight + 2 * frameThickness
//    Log.d("createFramedImage", "Combined width: $combinedWidth, Combined height: $combinedHeight")
//
//    // Yeni bir Bitmap oluşturuyoruz (resim + çerçeve)
//    val finalBitmap = Bitmap.createBitmap(combinedWidth, combinedHeight, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(finalBitmap)
//
//    // Çerçevenin köşe parçalarını kesiyoruz
//    val topLeftCorner = Bitmap.createBitmap(frameBitmap, 0, 0, frameThickness, frameThickness)
//    val topRightCorner = Bitmap.createBitmap(
//        frameBitmap,
//        frameBitmap.width - frameThickness,
//        0,
//        frameThickness,
//        frameThickness
//    )
//    val bottomLeftCorner = Bitmap.createBitmap(
//        frameBitmap,
//        0,
//        frameBitmap.height - frameThickness,
//        frameThickness,
//        frameThickness
//    )
//    val bottomRightCorner = Bitmap.createBitmap(
//        frameBitmap,
//        frameBitmap.width - frameThickness,
//        frameBitmap.height - frameThickness,
//        frameThickness,
//        frameThickness
//    )
//
//    Log.d("createFramedImage", "Corners created: TopLeft, TopRight, BottomLeft, BottomRight")
//
//    // Çerçeve kenarlarını kesiyoruz (üst, alt, sol, sağ)
//    val topEdge = Bitmap.createScaledBitmap(
//        Bitmap.createBitmap(
//            frameBitmap,
//            frameThickness,
//            0,
//            frameBitmap.width - 2 * frameThickness,
//            frameThickness
//        ),
//        combinedWidth - 2 * frameThickness,
//        frameThickness,
//        false
//    )
//    val bottomEdge = Bitmap.createScaledBitmap(
//        Bitmap.createBitmap(
//            frameBitmap,
//            frameThickness,
//            frameBitmap.height - frameThickness,
//            frameBitmap.width - 2 * frameThickness,
//            frameThickness
//        ),
//        combinedWidth - 2 * frameThickness,
//        frameThickness,
//        false
//    )
//    val leftEdge = Bitmap.createScaledBitmap(
//        Bitmap.createBitmap(
//            frameBitmap,
//            0,
//            frameThickness,
//            frameThickness,
//            frameBitmap.height - 2 * frameThickness
//        ),
//        frameThickness,
//        combinedHeight - 2 * frameThickness,
//        false
//    )
//    val rightEdge = Bitmap.createScaledBitmap(
//        Bitmap.createBitmap(
//            frameBitmap,
//            frameBitmap.width - frameThickness,
//            frameThickness,
//            frameThickness,
//            frameBitmap.height - 2 * frameThickness
//        ),
//        frameThickness,
//        combinedHeight - 2 * frameThickness,
//        false
//    )
//
//    Log.d("createFramedImage", "Edges created: TopEdge, BottomEdge, LeftEdge, RightEdge")
//
//    // Çerçevenin kenarlarını çiziyoruz
//    canvas.drawBitmap(topLeftCorner, 0f, 0f, Paint())
//    canvas.drawBitmap(topRightCorner, (imageWidth + frameThickness).toFloat(), 0f, Paint())
//    canvas.drawBitmap(bottomLeftCorner, 0f, (imageHeight + frameThickness).toFloat(), Paint())
//    canvas.drawBitmap(
//        bottomRightCorner,
//        (imageWidth + frameThickness).toFloat(),
//        (imageHeight + frameThickness).toFloat(),
//        Paint()
//    )
//
//    // Çerçevenin üst, alt, sol ve sağ kenarlarını çiziyoruz
//    canvas.drawBitmap(topEdge, frameThickness.toFloat(), 0f, Paint())
//    canvas.drawBitmap(
//        bottomEdge,
//        frameThickness.toFloat(),
//        (imageHeight + frameThickness).toFloat(),
//        Paint()
//    )
//    canvas.drawBitmap(leftEdge, 0f, frameThickness.toFloat(), Paint())
//    canvas.drawBitmap(
//        rightEdge,
//        (imageWidth + frameThickness).toFloat(),
//        frameThickness.toFloat(),
//        Paint()
//    )
//
//    Log.d("createFramedImage", "Frame edges and corners drawn")
//
//    // Resmi, çerçevenin içine yerleştiriyoruz
//    canvas.drawBitmap(imageBitmap, frameThickness.toFloat(), frameThickness.toFloat(), Paint())
//
//    Log.d("createFramedImage", "Image placed inside the frame")
//
//    return finalBitmap
//}


// Çerçeve kalınlığını dp cinsinden belirleyin ve px'e dönüştürün


/**
 * Calculate dynamic frame thickness in dp and convert to pixels
 */


/**
 * Scales the image to fill the inner area of the frame, ensuring the image reaches the frame edges.
 * @param imageBitmap The Bitmap of the image to be framed.
 * @param frameBitmap The Bitmap of the frame to place around the image.
 * @return A new Bitmap with the image scaled to fill the inner area of the frame.
 */
//fun createFramedImage(imageBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
//    // Sabit çerçeve kalınlığını alıyoruz (dp cinsinden)
//    val frameThickness = getFixedFrameThickness()  // Sabit çerçeve kalınlığı
//
//    // Çerçeve boyutlarını ve resmin boyutlarını alıyoruz
//    val frameWidth = frameBitmap.width
//    val frameHeight = frameBitmap.height
//
//    val imageWidth = imageBitmap.width
//    val imageHeight = imageBitmap.height
//
//    // Çerçeveyi parçalara ayıralım: üst, alt, sol, sağ, köşeler
//    val topEdge = Bitmap.createBitmap(frameBitmap, frameThickness, 0, frameBitmap.width - 2 * frameThickness, frameThickness)
//    val bottomEdge = Bitmap.createBitmap(frameBitmap, frameThickness, frameBitmap.height - frameThickness, frameBitmap.width - 2 * frameThickness, frameThickness)
//    val leftEdge = Bitmap.createBitmap(frameBitmap, 0, frameThickness, frameThickness, frameBitmap.height - 2 * frameThickness)
//    val rightEdge = Bitmap.createBitmap(frameBitmap, frameBitmap.width - frameThickness, frameThickness, frameThickness, frameBitmap.height - 2 * frameThickness)
//
//    // Çerçevenin dört köşesini alıyoruz
//    val topLeftCorner = Bitmap.createBitmap(frameBitmap, 0, 0, frameThickness, frameThickness)
//    val topRightCorner = Bitmap.createBitmap(frameBitmap, frameBitmap.width - frameThickness, 0, frameThickness, frameThickness)
//    val bottomLeftCorner = Bitmap.createBitmap(frameBitmap, 0, frameBitmap.height - frameThickness, frameThickness, frameThickness)
//    val bottomRightCorner = Bitmap.createBitmap(frameBitmap, frameBitmap.width - frameThickness, frameBitmap.height - frameThickness, frameThickness, frameThickness)
//
//    // Çerçeve boyutlarını ayarlayarak resmi yerleştiriyoruz
//    val availableWidth = imageWidth + 2 * frameThickness
//    val availableHeight = imageHeight + 2 * frameThickness
//
//    // Final bitmap oluşturuyoruz
//    val finalBitmap = Bitmap.createBitmap(availableWidth, availableHeight, Bitmap.Config.ARGB_8888)
//    val finalCanvas = Canvas(finalBitmap)
//
//    // Çerçeveyi çiziyoruz (kenarları ve köşeleri)
//    finalCanvas.drawBitmap(topEdge, frameThickness.toFloat(), 0f, null)
//    finalCanvas.drawBitmap(bottomEdge, frameThickness.toFloat(), availableHeight - frameThickness.toFloat(), null)
//    finalCanvas.drawBitmap(leftEdge, 0f, frameThickness.toFloat(), null)
//    finalCanvas.drawBitmap(rightEdge, availableWidth - frameThickness.toFloat(), frameThickness.toFloat(), null)
//
//    // Çerçevenin köşelerini çiziyoruz
//    finalCanvas.drawBitmap(topLeftCorner, 0f, 0f, null)
//    finalCanvas.drawBitmap(topRightCorner, availableWidth - frameThickness.toFloat(), 0f, null)
//    finalCanvas.drawBitmap(bottomLeftCorner, 0f, availableHeight - frameThickness.toFloat(), null)
//    finalCanvas.drawBitmap(bottomRightCorner, availableWidth - frameThickness.toFloat(), availableHeight - frameThickness.toFloat(), null)
//
//    // Resmi çerçevenin ortasına yerleştiriyoruz
//    finalCanvas.drawBitmap(imageBitmap, frameThickness.toFloat(), frameThickness.toFloat(), null)
//
//    return finalBitmap
//}


fun drawFrameAroundImage(canvas: Canvas, frameBitmap: Bitmap, frameThickness: Int) {
    val paint = Paint()

    // Çerçeve kenarlarını çizen kod
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

    // Çerçevenin üst, alt, sol ve sağ kenarlarını çiziyoruz
    val topEdge = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(
            frameBitmap,
            frameThickness,
            0,
            frameBitmap.width - 2 * frameThickness,
            frameThickness
        ),
        frameBitmap.width - 2 * frameThickness,
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
        frameBitmap.width - 2 * frameThickness,
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
        frameBitmap.height - 2 * frameThickness,
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
        frameBitmap.height - 2 * frameThickness,
        false
    )

    // Çerçevenin kenarlarını çiziyoruz
    canvas.drawBitmap(topLeftCorner, 0f, 0f, paint)
    canvas.drawBitmap(topRightCorner, (frameBitmap.width - frameThickness).toFloat(), 0f, paint)
    canvas.drawBitmap(bottomLeftCorner, 0f, (frameBitmap.height - frameThickness).toFloat(), paint)
    canvas.drawBitmap(
        bottomRightCorner,
        (frameBitmap.width - frameThickness).toFloat(),
        (frameBitmap.height - frameThickness).toFloat(),
        paint
    )

    // Çerçevenin kenarlarını çiziyoruz
    canvas.drawBitmap(topEdge, frameThickness.toFloat(), 0f, paint)
    canvas.drawBitmap(
        bottomEdge,
        frameThickness.toFloat(),
        (frameBitmap.height - frameThickness).toFloat(),
        paint
    )
    canvas.drawBitmap(leftEdge, 0f, frameThickness.toFloat(), paint)
    canvas.drawBitmap(
        rightEdge,
        (frameBitmap.width - frameThickness).toFloat(),
        frameThickness.toFloat(),
        paint
    )
}


