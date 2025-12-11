package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R // 1. Add this import

class KwentongbayanContent : BaseContentActivity() {

    override val contentRawRes: Int
        get() = R.raw.kwentongbayancontent // Make sure filename matches

    override val pageTitle: String
        get() = "KWENTONG BAYAN"

    override val pageSubtitle: String
        get() = "Mga Salaysay ng Ating Lahi"

    override val textAlignment: Int
        get() = Gravity.START

    // 2. Add this override
    override val topicKey: String
        get() = "KWENTONG BAYAN" // Use underscore to match the check in MainActivity
}
