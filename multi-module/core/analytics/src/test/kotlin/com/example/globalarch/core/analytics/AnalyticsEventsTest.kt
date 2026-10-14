package com.example.globalarch.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsEventsTest {
    @Test
    fun screenView_carriesScreenName() {
        assertEquals(
            AnalyticsEvent("screen_view", mapOf("screen" to "home")),
            AnalyticsEvents.screenView("home"),
        )
    }

    @Test
    fun noteSaved_encodesIsNewFlag() {
        assertEquals("true", AnalyticsEvents.noteSaved(isNew = true).params["is_new"])
        assertEquals("false", AnalyticsEvents.noteSaved(isNew = false).params["is_new"])
    }

    @Test
    fun noteDeleted_hasNoParams() {
        assertEquals(AnalyticsEvent("note_deleted"), AnalyticsEvents.noteDeleted())
    }
}
