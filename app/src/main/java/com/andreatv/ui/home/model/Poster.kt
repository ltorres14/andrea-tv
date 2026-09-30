package com.andreatv.ui.home.model

import androidx.annotation.DrawableRes

data class Poster(
    val title: String,
    val metadata: String,
    @DrawableRes val artwork: Int
)
