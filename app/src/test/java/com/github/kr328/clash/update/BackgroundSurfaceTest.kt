// SPDX-License-Identifier: GPL-3.0-or-later
package com.github.kr328.clash.update

import com.github.kr328.clash.design.util.backgroundSurfaceColor
import org.junit.Assert.assertEquals
import org.junit.Test

class BackgroundSurfaceTest {
    @Test fun zeroPercentRemovesOpaqueWhiteCards() {
        assertEquals(0x00ffffff, backgroundSurfaceColor(0xffffffff.toInt(), true, 0))
    }
    @Test fun fullPercentAndDefaultThemeKeepOriginalSurfaces() {
        val color = 0xff123456.toInt()
        assertEquals(color, backgroundSurfaceColor(color, true, 100))
        assertEquals(color, backgroundSurfaceColor(color, false, 0))
    }
    @Test fun selectedNodeStillHasVisibleHighlightAtZeroPercent() {
        assertEquals(166, backgroundSurfaceColor(0xff204372.toInt(), true, 0, true) ushr 24)
    }
    @Test fun percentageIsClampedAndTransparentRowsStayTransparent() {
        assertEquals(0, backgroundSurfaceColor(0xffffffff.toInt(), true, -50) ushr 24)
        assertEquals(255, backgroundSurfaceColor(0xffffffff.toInt(), true, 200) ushr 24)
        assertEquals(0, backgroundSurfaceColor(0, true, 100) ushr 24)
    }
}
