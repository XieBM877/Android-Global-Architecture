package com.example.globalarch.core.analytics

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
)

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

object AnalyticsEvents {
    fun screenView(screen: String) = AnalyticsEvent("screen_view", mapOf("screen" to screen))

    fun noteSaved(isNew: Boolean) = AnalyticsEvent("note_saved", mapOf("is_new" to isNew.toString()))

    fun noteDeleted() = AnalyticsEvent("note_deleted")

    fun themeChanged(mode: String) = AnalyticsEvent("theme_changed", mapOf("mode" to mode))
}
