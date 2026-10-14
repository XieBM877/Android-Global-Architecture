package com.example.globalarch.core.designsystem.component

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.globalarch.core.designsystem.theme.AppDimens

fun Modifier.constrainContentWidth(): Modifier = this
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = AppDimens.ContentMaxWidth)
