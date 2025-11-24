package com.example.tekbuk.HomapageContent

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tekbuk.R
import com.example.tekbuk.adapter.PaksaAdapter
import com.example.tekbuk.model.PaksaItem
import com.example.tekbuk.PaksaContent.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PaksaPageActivity : AppCompatActivity(), ScrollProgressListener {

    private lateinit var dotIndicator: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var paksaItems: MutableList<PaksaItem>
    private lateinit var adapter: PaksaAdapter

    // Helper function to read raw text files
    private fun readRawText(resId: Int): String =
        resources.openRawResource(resId).bufferedReader().use { it.readText() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.paksa_page)

        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        viewPager = findViewById(R.id.paksaCarousel)
        dotIndicator = findViewById(R.id.dotIndicator)

        // Initialize paksa items with raw text descriptions
        paksaItems = mutableListOf(
            PaksaItem("TULA", readRawText(R.raw.tula_desc), R.drawable.tula, 0),
            PaksaItem("SANAYSAY", readRawText(R.raw.sanaysay_desc), R.drawable.sanaysay, 0),
            PaksaItem("DAGLI", readRawText(R.raw.dagli_desc), R.drawable.dagli, 0),
            PaksaItem("TALUMPATI", readRawText(R.raw.talumpati_desc), R.drawable.talumpati, 0),
            PaksaItem("KWENTONG BAYAN", readRawText(R.raw.kwentongbayan_desc), R.drawable.kwentongbayan, 0)
        )

        // Setup adapter
        adapter = PaksaAdapter(this, paksaItems)
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.offscreenPageLimit = 3
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.getChildAt(0).overScrollMode = ViewPager2.OVER_SCROLL_NEVER

        // Page transform (spacing + scale effect)
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.viewpager_page_margin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.viewpager_offset)
        viewPager.setPadding(offsetPx, 0, offsetPx, 0)
        viewPager.setPageTransformer { page, position ->
            val offset = position * -(0 * offsetPx + pageMarginPx)
            page.translationX = offset
            val scale = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
            page.scaleY = scale
        }

        // Attach TabLayout as dot indicator with spacing
        TabLayoutMediator(dotIndicator, viewPager) { tab, _ ->
            val tabView = tab.view
            val params = tabView.layoutParams as? ViewGroup.MarginLayoutParams
            params?.let {
                val marginDp = 4  // margin in dp
                val marginPx = (marginDp * resources.displayMetrics.density).toInt()
                it.marginStart = marginPx
                it.marginEnd = marginPx
                tabView.layoutParams = it
            }
        }.attach()

        // Set scroll listener for live progress updates
        BaseContentActivity.scrollListener = this
    }

    // Scroll progress callback
    override fun onProgressUpdate(index: Int, progress: Int) {
        if (index in paksaItems.indices) {
            paksaItems[index].progress = progress
            adapter.notifyItemChanged(index)
        }
    }
}
