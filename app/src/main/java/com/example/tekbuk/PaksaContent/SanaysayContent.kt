package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R // 1. Add this import

class SanaysayContent : BaseContentActivity() {

    override val contentRawRes: Int
        get() = R.raw.sanaysaycontent // Make sure filename matches

    override val pageTitle: String
        get() = "SANAYSAY"

    override val pageSubtitle: String
        get() = "Pagpapahayag ng Opinyon"

    override val textAlignment: Int
        get() = Gravity.START

    // 2. Add this override
    override val topicKey: String
        get() = "SANAYSAY"
}
