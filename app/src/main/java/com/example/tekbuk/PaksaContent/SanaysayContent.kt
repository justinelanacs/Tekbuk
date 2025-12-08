package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class SanaysayContent : BaseContentActivity() {

    // IMPORTANT: Make sure this points to the correct content file for Sanaysay!
    override val contentRawRes: Int = R.raw.sanaysaycontent

    override val pageTitle: String = "SANAYSAY"
    override val pageSubtitle: String = "SUMANlangit Nawa"

    // Provide the alignment value. NO_GRAVITY allows the XML's "inter_word" to work.
    // If "inter_word" isn't working, use Gravity.START for standard left-alignment.
    override val textAlignment: Int = Gravity.NO_GRAVITY
}
