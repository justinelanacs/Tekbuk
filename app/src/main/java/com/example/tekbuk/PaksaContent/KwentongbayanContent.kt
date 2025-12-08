package com.example.tekbuk.PaksaContent

import android.view.Gravity
import com.example.tekbuk.R

class KwentongbayanContent : BaseContentActivity() {

    // ⭐ FIX 1: Point to the correct Kwentong Bayan content file.
    override val contentRawRes: Int = R.raw.kwentongbayancontent

    override val pageTitle: String = "KWENTONG BAYAN"
    override val pageSubtitle: String = "ANG ALAMAT NG MANGUMIT"

    // ⭐ FIX 2: Provide the alignment value for justified text.
    override val textAlignment: Int = Gravity.NO_GRAVITY
}
