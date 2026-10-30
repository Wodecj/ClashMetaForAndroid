// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-31: keep wallpaper visible through content surfaces.
package com.github.kr328.clash.design.util

import kotlin.math.roundToInt

fun backgroundSurfaceColor(color: Int, enabled: Boolean, percent: Int, selected: Boolean = false): Int {
    if (!enabled) return color
    val concentration = percent.coerceIn(0, 100).coerceAtLeast(if (selected) 65 else 0)
    val alpha = ((color ushr 24) * concentration / 100f).roundToInt()
    return (color and 0x00ffffff) or (alpha shl 24)
}

fun backgroundTextShadow(color: Int): Int {
    val brightness = ((color ushr 16) and 255) + ((color ushr 8) and 255) + (color and 255)
    return if (brightness > 384) 0xb4000000.toInt() else 0xb4ffffff.toInt()
}
