// Modified by Wodecj, 2026-10-31: wallpaper-aware home cards, including bound status colors.
package com.github.kr328.clash.design.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.Canvas
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.model.BackgroundTheme
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.ComponentLargeActionLabelBinding
import com.github.kr328.clash.design.util.*
import com.google.android.material.card.MaterialCardView

class LargeActionCard @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : MaterialCardView(context, attributeSet, defStyleAttr) {
    private val store = UiStore(context)
    private var originalColor: Int? = null
    private var lastRenderedColor: Int? = null
    var surfaceOpacityPercent: Int? = null
        set(value) { field = value; refreshSurface() }

    fun refreshSurface() {
        val actual = cardBackgroundColor.defaultColor
        // Data binding can replace the running/stopped fill after the view is attached.
        if (actual != lastRenderedColor) originalColor = actual
        val enabled = store.backgroundTheme != BackgroundTheme.Default
        val color = backgroundSurfaceColor(originalColor ?: actual, enabled,
            surfaceOpacityPercent ?: store.backgroundOpacityPercent, id == R.id.main_status_card)
        if (actual != color) setCardBackgroundColor(color)
        lastRenderedColor = color
        val targetElevation = if (color ushr 24 == 0) 0f else context.getPixels(R.dimen.large_action_card_elevation).toFloat()
        if (elevation != targetElevation) elevation = targetElevation
    }

    override fun draw(canvas: Canvas) {
        refreshSurface()
        super.draw(canvas)
    }

    private val binding = ComponentLargeActionLabelBinding
        .inflate(context.layoutInflater, this, true)

    var text: CharSequence?
        get() = binding.textView.text
        set(value) {
            binding.textView.text = value
        }

    var subtext: CharSequence?
        get() = binding.subtextView.text
        set(value) {
            binding.subtextView.text = value
        }

    var icon: Drawable?
        get() = binding.iconView.background
        set(value) {
            binding.iconView.background = value
        }

    init {
        context.resolveClickableAttrs(attributeSet, defStyleAttr) {
            isFocusable = focusable(true)
            isClickable = clickable(true)
            foreground = foreground() ?: context.selectableItemBackground
        }

        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.LargeActionCard,
            defStyleAttr,
            0
        ).apply {
            try {
                icon = getDrawable(R.styleable.LargeActionCard_icon)
                text = getString(R.styleable.LargeActionCard_text)
                subtext = getString(R.styleable.LargeActionCard_subtext)
            } finally {
                recycle()
            }
        }

        minimumHeight = context.getPixels(R.dimen.large_action_card_min_height)
        radius = context.getPixels(R.dimen.large_action_card_radius).toFloat()
        elevation = context.getPixels(R.dimen.large_action_card_elevation).toFloat()
        setCardBackgroundColor(context.resolveThemedColor(com.google.android.material.R.attr.colorSurface))
    }
}
