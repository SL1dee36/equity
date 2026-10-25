package com.equity.budget.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val releaseUrl: String,
    val body: String
)

object UpdateChecker {

    private const val GITHUB_API_URL = "https://api.github.com/repos/SL1dee36/equity/releases/latest"

    suspend fun checkForUpdates(currentVersion: String): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "EquityBudgetApp")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                conn.disconnect()

                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val htmlUrl = json.optString("html_url", "https://github.com/SL1dee36/equity/releases")
                val body = json.optString("body", "")

                // Find APK download asset if present
                var downloadUrl = htmlUrl
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", htmlUrl)
                            break
                        }
                    }
                }

                val hasUpdate = isNewerVersion(tagName, currentVersion)
                Result.success(
                    UpdateInfo(
                        hasUpdate = hasUpdate,
                        latestVersion = tagName,
                        downloadUrl = downloadUrl,
                        releaseUrl = htmlUrl,
                        body = body
                    )
                )
            } else if (conn.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                conn.disconnect()
                // No releases created yet on GitHub repo
                Result.success(
                    UpdateInfo(
                        hasUpdate = false,
                        latestVersion = currentVersion,
                        downloadUrl = "https://github.com/SL1dee36/equity/releases",
                        releaseUrl = "https://github.com/SL1dee36/equity/releases",
                        body = ""
                    )
                )
            } else {
                conn.disconnect()
                Result.failure(Exception("HTTP error ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        if (remote.isBlank() || current.isBlank()) return false
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
