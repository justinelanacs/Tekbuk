package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class TulaContent : BaseContentActivity() {

    override val contentRawRes: Int
        get() = R.raw.tulacontent

    override val pageTitle: String
        get() = "TULA"

    override val pageSubtitle: String
        get() = "Pag-unawa sa Sining ng Tula"

    override val textAlignment: Int
        get() = Gravity.CENTER

    // ⭐ ADD THIS LINE (and do it for all other content activities)
    override val topicKey: String
        get() = "TULA"
}
