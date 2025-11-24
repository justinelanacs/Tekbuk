package com.example.tekbuk.model

import androidx.appcompat.app.AppCompatActivity

data class PaksaItem(
    val title: String,
    val description: String,
    val iconRes: Int,
    var progress: Int = 0
)
