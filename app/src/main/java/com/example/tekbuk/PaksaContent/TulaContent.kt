package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class TulaContent : BaseContentActivity() {

    override val contentRawRes: Int = R.raw.tulacontent
    override val pageTitle: String = "TULA"
    override val pageSubtitle: String = "PIKIT NA KATOTOHANAN"

    // Provide the alignment value
    override val textAlignment: Int = Gravity.CENTER
}
