package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class TalumpatiContent : BaseContentActivity() {

    // ⭐ FIX 1: Point to the correct Talumpati content file.
    override val contentRawRes: Int = R.raw.talumpaticontent

    override val pageTitle: String = "TALUMPATI"
    override val pageSubtitle: String = "ATIN TO!"

    // ⭐ FIX 2: Provide the alignment value for justified text.
    override val textAlignment: Int = Gravity.NO_GRAVITY
}
