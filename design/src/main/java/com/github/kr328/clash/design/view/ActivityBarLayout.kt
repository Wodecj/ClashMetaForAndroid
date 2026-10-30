// Modified by Wodecj, 2026-10-31: enforce chrome transparency after layout/data binding.
package com.github.kr328.clash.design.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.model.BackgroundTheme
import com.github.kr328.clash.design.util.backgroundSurfaceColor
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.github.kr328.clash.design.util.resolveThemedColor

class ActivityBarLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr, defStyleRes) {
    private val store = UiStore(context)
    var surfaceOpacityPercent: Int? = null
        set(value) { field = value; invalidate() }

    override fun draw(canvas: Canvas) {
        if (store.backgroundTheme != BackgroundTheme.Default) {
            val color = backgroundSurfaceColor(context.resolveThemedColor(android.R.attr.windowBackground),
                true, surfaceOpacityPercent ?: store.backgroundOpacityPercent)
            if ((background as? ColorDrawable)?.color != color) setBackgroundColor(color)
            if (alpha != 1f) alpha = 1f
        }
        super.draw(canvas)
    }
    init {
        alpha = 0.96f

        setBackgroundColor(context.resolveThemedColor(android.R.attr.windowBackground))
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)

        return true
    }
}
