package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R // 1. Add this import

class TalumpatiContent : BaseContentActivity() {

    override val contentRawRes: Int
        get() = R.raw.talumpaticontent // Make sure filename matches

    override val pageTitle: String
        get() = "TALUMPATI"

    override val pageSubtitle: String
        get() = "Sining ng Pagtatalumpati"

    override val textAlignment: Int
        get() = Gravity.START

    // 2. Add this override
    override val topicKey: String
        get() = "TALUMPATI"
}
