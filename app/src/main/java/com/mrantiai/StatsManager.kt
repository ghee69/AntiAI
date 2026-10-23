package com.mrantiai.data

import android.content.Context
import android.content.SharedPreferences

object StatsManager {
    private const val PREF_NAME = "mr_antiai_stats"
    private const val KEY_YT_COUNT = "key_yt_count"
    private const val KEY_IG_COUNT = "key_ig_count"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun incrementSkipped(context: Context, isYouTube: Boolean) {
        val prefs = getPrefs(context)
        val key = if (isYouTube) KEY_YT_COUNT else KEY_IG_COUNT
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getCounts(context: Context): Pair<Int, Int> {
        val prefs = getPrefs(context)
        val yt = prefs.getInt(KEY_YT_COUNT, 0)
        val ig = prefs.getInt(KEY_IG_COUNT, 0)
        return Pair(yt, ig)
    }

    fun resetStats(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
