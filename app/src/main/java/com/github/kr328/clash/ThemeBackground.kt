// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-30.
package com.github.kr328.clash

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AtomicFile
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.kr328.clash.design.model.BackgroundTheme
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.util.resolveThemedColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/** A shared, bounded bitmap; no persistable URI permission or storage permission needed. */
internal object ThemeBackground {
    private val imageLock = Mutex()
    private var cached: Bitmap? = null
    private fun imageFile(context: Context) = File(context.filesDir, "theme-background.png")

    suspend fun importImage(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        imageLock.withLock {
        val temp = File.createTempFile("background-", ".tmp", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= 20L * 1024 * 1024) { "Image exceeds 20 MiB" }
                        output.write(buffer, 0, read)
                    }
                }
            } ?: error("Cannot open image")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temp.path, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Invalid image" }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1920) sample *= 2
            val decoded = BitmapFactory.decodeFile(temp.path,
                BitmapFactory.Options().apply { inSampleSize = sample }) ?: error("Invalid image")
            val orientation = try {
                ExifInterface(temp.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            } catch (_: Exception) { 1 }
            val matrix = Matrix().apply {
                when (orientation) {
                    2 -> setScale(-1f, 1f)
                    3 -> setRotate(180f)
                    4 -> setScale(1f, -1f)
                    5 -> { setRotate(90f); postScale(-1f, 1f) }
                    6 -> setRotate(90f)
                    7 -> { setRotate(-90f); postScale(-1f, 1f) }
                    8 -> setRotate(-90f)
                }
            }
            val bitmap = if (matrix.isIdentity) decoded else
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                    .also { if (it !== decoded) decoded.recycle() }
            try {
                val target = AtomicFile(imageFile(context))
                val output = target.startWrite()
                try {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                    target.finishWrite(output)
                } catch (e: Exception) {
                    target.failWrite(output)
                    throw e
                }
                cached = null
            } finally { bitmap.recycle() }
        } finally { temp.delete() }
        }
    }

    suspend fun reset(context: Context) = withContext(Dispatchers.IO) {
        imageLock.withLock {
        AtomicFile(imageFile(context)).delete()
        cached = null
        }
    }

    suspend fun load(context: Context): Bitmap? = withContext(Dispatchers.IO) {
        imageLock.withLock {
            cached ?: BitmapFactory.decodeFile(imageFile(context).path)?.also { cached = it }
        }
    }

    fun wrap(context: Context, content: View, store: UiStore): Pair<View, ImageView?> {
        if (store.backgroundTheme == BackgroundTheme.Default) return content to null
        val frame = FrameLayout(context)
        val image = ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        val colors = when (store.backgroundTheme) {
            BackgroundTheme.Ocean -> intArrayOf(0xff1565c0.toInt(), 0xff80deea.toInt())
            BackgroundTheme.Forest -> intArrayOf(0xff1b5e20.toInt(), 0xffa5d6a7.toInt())
            BackgroundTheme.Sunset -> intArrayOf(0xffad1457.toInt(), 0xffffcc80.toInt())
            else -> intArrayOf(context.resolveThemedColor(android.R.attr.windowBackground),
                context.resolveThemedColor(android.R.attr.windowBackground))
        }
        image.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
        frame.addView(image, FrameLayout.LayoutParams(-1, -1))
        val base = context.resolveThemedColor(android.R.attr.windowBackground)
        frame.addView(View(context).apply {
            background = ColorDrawable(Color.argb(store.backgroundOpacity.alpha,
                Color.red(base), Color.green(base), Color.blue(base)))
        }, FrameLayout.LayoutParams(-1, -1))
        frame.addView(content, FrameLayout.LayoutParams(-1, -1))
        return frame to image.takeIf { store.backgroundTheme == BackgroundTheme.Custom }
    }
}
