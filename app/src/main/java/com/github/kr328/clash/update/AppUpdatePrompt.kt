// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-30: non-blocking startup update prompt.
package com.github.kr328.clash.update

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.github.kr328.clash.BuildConfig
import com.github.kr328.clash.design.R
import com.github.kr328.clash.store.AppStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class AppUpdatePrompt(private val activity: AppCompatActivity) {
    private val store = AppStore(activity)
    private var dialog: AlertDialog? = null

    suspend fun checkAndShow() {
        val update = latest() ?: return
        if (activity.isFinishing || activity.isDestroyed ||
            !activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
            update.tag in shown || !ReleaseUpdates.shouldPrompt(update.tag, store.ignoredUpdateTag,
                store.snoozedUpdateTag, store.updateSnoozeUntil, System.currentTimeMillis())) return
        val notes = update.notes.ifBlank { activity.getString(R.string.update_no_notes) }
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(activity.getString(R.string.update_available, update.tag))
            .setMessage(activity.getString(R.string.update_message, BuildConfig.VERSION_NAME, notes))
            .setPositiveButton(R.string.update_download) { _, _ ->
                try {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(activity, R.string.update_no_browser, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.update_later) { _, _ ->
                store.snoozedUpdateTag = update.tag
                store.updateSnoozeUntil = System.currentTimeMillis() + 24L * 60 * 60 * 1000
            }
            .setNeutralButton(R.string.update_ignore) { _, _ -> store.ignoredUpdateTag = update.tag }
            .show()
        shown.add(update.tag)
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    companion object {
        private val mutex = Mutex()
        private var lastAttempt = 0L
        private var cached: ReleaseUpdate? = null
        private val shown = mutableSetOf<String>()

        private suspend fun latest(): ReleaseUpdate? = withContext(Dispatchers.IO) {
            mutex.withLock {
                val now = android.os.SystemClock.elapsedRealtime()
                if (lastAttempt > 0 && now - lastAttempt < 60_000) return@withLock cached
                lastAttempt = now
                cached = null
                try {
                    val connection = URL(ReleaseUpdates.API).openConnection() as HttpsURLConnection
                    try {
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.instanceFollowRedirects = false
                        connection.setRequestProperty("Accept", "application/vnd.github+json")
                        connection.setRequestProperty("User-Agent", "ClashMetaThemes/${BuildConfig.VERSION_NAME}")
                        if (connection.responseCode != 200) return@withLock null
                        val bytes = ByteArrayOutputStream()
                        connection.inputStream.use { input ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val size = input.read(buffer)
                                if (size < 0) break
                                if (bytes.size() + size > 1024 * 1024) return@withLock null
                                bytes.write(buffer, 0, size)
                            }
                        }
                        cached = ReleaseUpdates.parse(bytes.toString("UTF-8"),
                            BuildConfig.VERSION_NAME, Build.SUPPORTED_ABIS.toList())
                    } finally { connection.disconnect() }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Offline, rate limiting and invalid responses never interrupt the main app.
                }
                cached
            }
        }
    }
}
