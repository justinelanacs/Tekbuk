package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class DagliContent : BaseContentActivity() {

    // ⭐ FIX 1: Point to the correct Dagli content file.
    override val contentRawRes: Int = R.raw.daglicontent

    override val pageTitle: String = "DAGLI"
    override val pageSubtitle: String = "Foul"

    // ⭐ FIX 2: Provide the alignment value for justified text.
    override val textAlignment: Int = Gravity.NO_GRAVITY
}
