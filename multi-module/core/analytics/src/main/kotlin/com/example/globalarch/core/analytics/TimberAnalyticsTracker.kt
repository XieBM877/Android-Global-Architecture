package com.example.globalarch.core.analytics

import javax.inject.Inject
import timber.log.Timber

internal class TimberAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {
        Timber.tag(TAG).d("%s %s", event.name, event.params)
    }

    private companion object {
        const val TAG = "Analytics"
    }
}
