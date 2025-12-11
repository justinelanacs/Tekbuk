package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R // 1. Add this import

class DagliContent : BaseContentActivity() {

    override val contentRawRes: Int
        get() = R.raw.daglicontent // Make sure filename matches

    override val pageTitle: String
        get() = "DAGLI"

    override val pageSubtitle: String
        get() = "Maikling Salaysay"

    override val textAlignment: Int
        get() = Gravity.START

    // 2. Add this override
    override val topicKey: String
        get() = "DAGLI"
}
