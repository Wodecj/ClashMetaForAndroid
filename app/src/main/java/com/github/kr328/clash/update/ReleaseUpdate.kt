// SPDX-License-Identifier: GPL-3.0-or-later
// Added by Wodecj, 2026-10-30: GitHub release discovery; no third-party app code copied.
package com.github.kr328.clash.update

import org.json.JSONObject
import java.net.URI

internal data class ThemeVersion(val parts: List<Long>) : Comparable<ThemeVersion> {
    override fun compareTo(other: ThemeVersion): Int {
        for (index in parts.indices) {
            val result = parts[index].compareTo(other.parts[index])
            if (result != 0) return result
        }
        return 0
    }

    companion object {
        private val pattern = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)-themes\\.(\\d+)(?:\\.(?:Meta|Alpha))?(?:\\.debug)?$")
        fun parse(value: String): ThemeVersion? {
            val match = pattern.matchEntire(value) ?: return null
            val parts = match.groupValues.drop(1).map { it.toLongOrNull() ?: return null }
            return ThemeVersion(parts)
        }
    }
}

internal data class ReleaseUpdate(val tag: String, val notes: String, val downloadUrl: String)

internal object ReleaseUpdates {
    const val REPOSITORY = "Wodecj/ClashMetaForAndroid"
    const val API = "https://api.github.com/repos/$REPOSITORY/releases/latest"

    fun shouldPrompt(tag: String, ignored: String, snoozed: String, until: Long, now: Long): Boolean =
        tag != ignored && (tag != snoozed || now >= until)

    fun parse(json: String, installed: String, abis: List<String>): ReleaseUpdate? {
        val release = JSONObject(json)
        if (release.optBoolean("draft") || release.optBoolean("prerelease")) return null
        val tag = release.optString("tag_name")
        val latest = ThemeVersion.parse(tag) ?: return null
        val current = ThemeVersion.parse(installed) ?: return null
        if (latest <= current) return null
        // Only accept the canonical tag format used by this fork's release workflow.
        val canonical = "v" + latest.parts.take(3).joinToString(".") + "-themes." + latest.parts[3]
        if (tag != canonical) return null
        val assets = release.optJSONArray("assets") ?: return null
        val candidates = (0 until assets.length()).map { assets.getJSONObject(it) }
        val prefix = "cmfa-${tag.removePrefix("v")}-meta-"
        val names = (abis + "universal").distinct().map { "$prefix$it-release.apk" }
        for (name in names) {
            val asset = candidates.firstOrNull { it.optString("name") == name } ?: continue
            val url = asset.optString("browser_download_url")
            val uri = try { URI(url) } catch (_: Exception) { continue }
            if (uri.scheme != "https" || uri.host != "github.com" || uri.userInfo != null ||
                uri.port != -1 || uri.rawQuery != null || uri.rawFragment != null ||
                uri.path != "/$REPOSITORY/releases/download/$tag/$name") continue
            return ReleaseUpdate(tag, release.optString("body").take(2500), url)
        }
        return null
    }
}
