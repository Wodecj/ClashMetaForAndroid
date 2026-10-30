// Modified by Wodecj, 2026-10-30: persistent background settings.
package com.github.kr328.clash.design.store

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.design.model.AppInfoSort
import com.github.kr328.clash.design.model.DarkMode

class UiStore(context: Context) {
    private val store = Store(
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).asStoreProvider()
    )
    var backgroundTheme by store.enum(
        key = "background_theme",
        defaultValue = com.github.kr328.clash.design.model.BackgroundTheme.Default,
        values = com.github.kr328.clash.design.model.BackgroundTheme.values()
    )

    private var savedBackgroundOpacityPercent by store.int("background_opacity_percent", -1)

    var backgroundOpacityPercent: Int
        get() {
            val saved = savedBackgroundOpacityPercent
            if (saved >= 0) return saved.coerceIn(0, 100)
            // Version 1 saved enum names; retain those users' selected concentration.
            return when (store.provider.getString("background_opacity", "Balanced")) {
                "Soft" -> 70
                "Strong" -> 95
                else -> 85
            }
        }
        set(value) { savedBackgroundOpacityPercent = value.coerceIn(0, 100) }


    var enableVpn: Boolean by store.boolean(
        key = "enable_vpn",
        defaultValue = true
    )

    var darkMode: DarkMode by store.enum(
        key = "dark_mode",
        defaultValue = DarkMode.Auto,
        values = DarkMode.values()
    )

    var hideAppIcon: Boolean by store.boolean(
        key = "hide_app_icon",
        defaultValue = context.packageManager.getComponentEnabledSetting(context.mainActivityAlias)
            .let { state ->
                state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED &&
                        state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            },
    )

    var hideFromRecents: Boolean by store.boolean(
        key = "hide_from_recents",
        defaultValue = false,
    )

    var proxyExcludeNotSelectable by store.boolean(
        key = "proxy_exclude_not_selectable",
        defaultValue = false,
    )

    var proxyLine: Int by store.int(
        key = "proxy_line",
        defaultValue = 2
    )

    var proxySort: ProxySort by store.enum(
        key = "proxy_sort",
        defaultValue = ProxySort.Default,
        values = ProxySort.values()
    )

    var proxyLastGroup: String by store.string(
        key = "proxy_last_group",
        defaultValue = ""
    )

    var accessControlSort: AppInfoSort by store.enum(
        key = "access_control_sort",
        defaultValue = AppInfoSort.Label,
        values = AppInfoSort.values(),
    )

    var accessControlReverse: Boolean by store.boolean(
        key = "access_control_reverse",
        defaultValue = false
    )

    var accessControlSystemApp: Boolean by store.boolean(
        key = "access_control_system_app",
        defaultValue = false,
    )

    companion object {
        private const val PREFERENCE_NAME = "ui"

        val Context.mainActivityAlias: ComponentName
            get() = ComponentName(this, "com.github.kr328.clash.MainActivityAlias")
    }
}
