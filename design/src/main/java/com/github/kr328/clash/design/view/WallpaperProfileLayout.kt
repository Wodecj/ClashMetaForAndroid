// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-31: profile cards with background-only transparency.
package com.github.kr328.clash.design.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.kr328.clash.design.model.BackgroundTheme
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.util.backgroundTextShadow

class WallpaperProfileLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    private val store = UiStore(context)
    var surfaceOpacityPercent: Int? = null
        set(value) { field = value; refreshSurface() }
    private val originalElevation = elevation

    init { background = background?.mutate() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        surfaceOpacityPercent = null
        refreshSurface()
    }

    fun refreshSurface() {
        val enabled = store.backgroundTheme != BackgroundTheme.Default
        val percent = if (enabled) surfaceOpacityPercent ?: store.backgroundOpacityPercent else 100
        background?.alpha = (percent.coerceIn(0, 100) * 255 + 50) / 100
        elevation = if (percent == 0) 0f else originalElevation
        fun shade(view: View) {
            if (view is TextView) view.setShadowLayer(if (enabled && percent < 50) resources.displayMetrics.density * 1.5f else 0f,
                0f, 0f, backgroundTextShadow(view.currentTextColor))
            if (view is ViewGroup) for (i in 0 until view.childCount) shade(view.getChildAt(i))
        }
        shade(this)
        invalidate()
    }
}
