// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-30: continuous background overlay control.
package com.github.kr328.clash.design.preference

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.util.layoutInflater
import kotlin.reflect.KMutableProperty0

fun PreferenceScreen.backgroundOpacitySlider(
    value: KMutableProperty0<Int>,
    onPreview: (Int) -> Unit,
) {
    val rootView = context.layoutInflater.inflate(R.layout.preference_background_opacity, root, false)
    val slider = rootView.findViewById<SeekBar>(R.id.background_opacity_slider)
    val label = rootView.findViewById<TextView>(R.id.background_opacity_value)
    fun updateLabel(progress: Int) {
        label.text = context.getString(R.string.background_opacity_value, progress)
    }
    slider.keyProgressIncrement = 1
    slider.progress = value.get().coerceIn(0, 100)
    updateLabel(slider.progress)
    slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        private var tracking = false

        override fun onStartTrackingTouch(seekBar: SeekBar) { tracking = true }

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            updateLabel(progress)
            if (fromUser) {
                onPreview(progress)
                // Accessibility and keyboard changes do not start a touch gesture.
                if (!tracking) value.set(progress)
            }
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            tracking = false
            value.set(seekBar.progress)
        }
    })
    addElement(object : Preference { override val view: View = rootView })
}
