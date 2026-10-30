// Modified by Wodecj, 2026-10-30: update reminder preferences.
package com.github.kr328.clash.store

import android.content.Context
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider

class AppStore(context: Context) {
    private val store = Store(
        context
            .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .asStoreProvider()
    )

    var updatedAt: Long by store.long(
        key = "updated_at",
        defaultValue = -1,
    )

    var ignoredUpdateTag by store.string("ignored_update_tag", "")
    var snoozedUpdateTag by store.string("snoozed_update_tag", "")
    var updateSnoozeUntil by store.long("update_snooze_until", 0L)

    companion object {
        private const val FILE_NAME = "app"
    }
}
