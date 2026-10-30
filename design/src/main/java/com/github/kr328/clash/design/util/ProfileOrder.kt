// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-31: stable, local profile ordering by UUID.
package com.github.kr328.clash.design.util

object ProfileOrder {
    fun <T> sorted(items: List<T>, saved: String, key: (T) -> String): List<T> {
        val ranks = saved.split('|').filter { it.isNotBlank() }.distinct()
            .withIndex().associate { it.value to it.index }
        return items.sortedBy { ranks[key(it)] ?: Int.MAX_VALUE }
    }

    fun <T> moved(items: List<T>, from: Int, to: Int): List<T> {
        if (from !in items.indices || to !in items.indices || from == to) return items
        return items.toMutableList().apply { add(to, removeAt(from)) }
    }
}
