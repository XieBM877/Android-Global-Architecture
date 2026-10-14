package com.example.globalarch.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {
    @Immutable
    data class Plain(
        val value: String,
    ) : UiText

    @Immutable
    data class Resource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    fun asString(context: Context): String = when (this) {
        is Plain -> value
        is Resource -> context.getString(id, *args.toTypedArray())
    }

    @Composable
    fun asString(): String = when (this) {
        is Plain -> value
        is Resource -> stringResource(id, *args.toTypedArray())
    }
}

fun uiText(
    @StringRes id: Int,
    vararg args: Any,
): UiText = UiText.Resource(id, args.toList())
