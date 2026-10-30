// Modified by Wodecj, 2026-10-30: background image import and reset.
package com.github.kr328.clash

import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.github.kr328.clash.design.model.BackgroundTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.design.AppSettingsDesign
import com.github.kr328.clash.design.model.Behavior
import com.github.kr328.clash.design.store.UiStore.Companion.mainActivityAlias
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.util.ApplicationObserver
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class AppSettingsActivity : BaseActivity<AppSettingsDesign>(), Behavior {
    private var importing = false
    private val pickBackground = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) launch {
            importing = true
            try {
                ThemeBackground.importImage(this@AppSettingsActivity, uri)
                uiStore.backgroundTheme = BackgroundTheme.Custom
                recreateAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(this@AppSettingsActivity,
                    com.github.kr328.clash.design.R.string.background_import_error, Toast.LENGTH_LONG).show()
            } finally { importing = false }
        }
    }

    private fun recreateAll() {
        ApplicationObserver.createdActivities.toList().forEach { it.recreate() }
    }
    override suspend fun main() {
        val design = AppSettingsDesign(
            this,
            uiStore,
            ServiceStore(this),
            this,
            clashRunning,
            ::onHideIconChange,
            { percent ->
                ApplicationObserver.createdActivities.filterIsInstance<BaseActivity<*>>()
                    .forEach { it.previewBackgroundOpacity(percent) }
            },
        )

        setContentDesign(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ClashStart, Event.ClashStop, Event.ServiceRecreated ->
                            recreate()
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    if (!importing) when (it) {
                        AppSettingsDesign.Request.PickBackground -> pickBackground.launch("image/*")
                        AppSettingsDesign.Request.ResetBackground -> {
                            ThemeBackground.reset(this@AppSettingsActivity)
                            uiStore.backgroundTheme = BackgroundTheme.Default
                            uiStore.backgroundOpacityPercent = 85
                            recreateAll()
                        }
                        AppSettingsDesign.Request.ReCreateAllActivities -> recreateAll()
                    }
                }
            }
        }
    }

    override var autoRestart: Boolean
        get() {
            val status = packageManager.getComponentEnabledSetting(
                RestartReceiver::class.componentName
            )

            return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        set(value) {
            val status = if (value)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            packageManager.setComponentEnabledSetting(
                RestartReceiver::class.componentName,
                status,
                PackageManager.DONT_KILL_APP,
            )
        }

    private fun onHideIconChange(hide: Boolean) {
        val newState = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        packageManager.setComponentEnabledSetting(
            mainActivityAlias,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}
