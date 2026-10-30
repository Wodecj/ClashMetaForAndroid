// SPDX-License-Identifier: GPL-3.0-or-later
package com.github.kr328.clash.update

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReleaseUpdatesTest {
    private val installed = "2.11.36-themes.3.Meta"
    private val tag = "v2.11.36-themes.4"

    private fun release(version: String = tag): JSONObject = JSONObject()
        .put("tag_name", version).put("draft", false).put("prerelease", false)
        .put("body", "Update notes")
        .put("assets", JSONArray().put(asset("arm64-v8a", version)).put(asset("universal", version)))

    private fun asset(abi: String, version: String = tag): JSONObject {
        val name = "cmfa-${version.removePrefix("v")}-meta-$abi-release.apk"
        return JSONObject().put("name", name).put("browser_download_url",
            "https://github.com/${ReleaseUpdates.REPOSITORY}/releases/download/$version/$name")
    }

    @Test fun numericVersionsAreNotComparedAsStrings() {
        assertTrue(ThemeVersion.parse("v2.11.36-themes.10")!! > ThemeVersion.parse("2.11.36-themes.9.Meta")!!)
        assertTrue(ThemeVersion.parse("v2.11.37-themes.1")!! > ThemeVersion.parse("2.11.36-themes.99")!!)
        assertEquals(ThemeVersion.parse("v2.11.36-themes.3"), ThemeVersion.parse(installed))
        assertNull(ThemeVersion.parse("v2.11.36"))
        assertNull(ThemeVersion.parse("v2.11.36-themes.99999999999999999999999"))
    }

    @Test fun newerVersionSelectsCompatibleApk() {
        val update = ReleaseUpdates.parse(release().toString(), installed, listOf("arm64-v8a", "armeabi-v7a"))!!
        assertEquals(tag, update.tag)
        assertTrue(update.downloadUrl.endsWith("-arm64-v8a-release.apk"))
        assertEquals("Update notes", update.notes)
    }

    @Test fun universalIsUsedWhenArchitectureAssetIsMissing() {
        val update = ReleaseUpdates.parse(release().toString(), installed, listOf("x86_64"))!!
        assertTrue(update.downloadUrl.endsWith("-universal-release.apk"))
    }

    @Test fun sameAndOlderVersionsDoNotNotify() {
        assertNull(ReleaseUpdates.parse(release("v2.11.36-themes.3").toString(), installed, listOf("arm64-v8a")))
        assertNull(ReleaseUpdates.parse(release("v2.11.36-themes.2").toString(), installed, listOf("arm64-v8a")))
    }

    @Test fun previewsAndDraftsAreIgnored() {
        for (flag in listOf("draft", "prerelease")) {
            assertNull(ReleaseUpdates.parse(release().put(flag, true).toString(), installed, listOf("arm64-v8a")))
        }
    }

    @Test fun sourceOnlyReleaseIsIgnored() {
        assertNull(ReleaseUpdates.parse(release().put("assets", JSONArray()).toString(), installed, listOf("arm64-v8a")))
    }

    @Test fun UnrelatedTagsAndMalformedInstalledVersionsAreIgnored() {
        assertNull(ReleaseUpdates.parse(release("v99.0.0").toString(), installed, listOf("arm64-v8a")))
        assertNull(ReleaseUpdates.parse(release().toString(), "unknown", listOf("arm64-v8a")))
    }

    @Test fun foreignAndInsecureDownloadsAreRejected() {
        for (url in listOf("http://github.com/a.apk", "https://evil.example/a.apk",
            "https://github.com/SomeoneElse/App/releases/download/$tag/app.apk")) {
            val assets = JSONArray().put(asset("arm64-v8a").put("browser_download_url", url))
            assertNull(ReleaseUpdates.parse(release().put("assets", assets).toString(), installed, listOf("arm64-v8a")))
        }
    }

    @Test fun longReleaseNotesAreBounded() {
        val update = ReleaseUpdates.parse(release().put("body", "x".repeat(10000)).toString(), installed, listOf("arm64-v8a"))!!
        assertEquals(2500, update.notes.length)
    }

    @Test fun ignoringOrSnoozingOneVersionDoesNotHideANewerVersion() {
        assertFalse(ReleaseUpdates.shouldPrompt(tag, tag, "", 0L, 100L))
        assertFalse(ReleaseUpdates.shouldPrompt(tag, "", tag, 200L, 199L))
        assertTrue(ReleaseUpdates.shouldPrompt(tag, "", tag, 200L, 200L))
        assertTrue(ReleaseUpdates.shouldPrompt("v2.11.36-themes.5", tag, tag, 200L, 100L))
    }
}
