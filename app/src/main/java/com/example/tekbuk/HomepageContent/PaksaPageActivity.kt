package com.example.tekbuk.HomepageContent

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tekbuk.R
import com.example.tekbuk.adapter.PaksaAdapter
import com.example.tekbuk.model.PaksaItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// ⭐ 1. REMOVED ScrollProgressListener - It's no longer needed here.
class PaksaPageActivity : AppCompatActivity() {

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

        // Initialize paksa items with a default progress of 0
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

        viewPager.setPageTransformer { page, position ->
            // Get the offsetPx and pageMarginPx again inside the transformer for clarity
            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.viewpager_page_margin)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.viewpager_offset)

            // This is the key calculation for translation
            // It moves the page based on its natural position and the desired margins/offsets
            val viewPagerMarginOffset = pageMarginPx + offsetPx
            val offset = position * -viewPagerMarginOffset

            page.translationX = offset

            // Your scaling logic is perfect and remains the same
            val scaleFactor = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
            page.scaleY = scaleFactor
            page.scaleX = scaleFactor // It's good practice to scale both X and Y for a uniform look
        }

        // Attach TabLayout as dot indicator
        TabLayoutMediator(dotIndicator, viewPager) { _, _ -> }.attach()

        // ⭐ 2. REMOVED THE SCROLL LISTENER - It is no longer set here.
    }

    // onResume is the best place to load data that needs to be fresh
    override fun onResume() {
        super.onResume()
        // Load the saved progress every time the user returns to this screen
        loadProgress()
    }

    /**
     * Loads progress for all paksa items from SharedPreferences.
     */
    private fun loadProgress() {
        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)
        var hasChanges = false
        for (item in paksaItems) {
            val key = "${item.title}_progress"
            val savedProgress = prefs.getInt(key, 0)
            if (item.progress != savedProgress) {
                item.progress = savedProgress
                hasChanges = true
            }
        }

        // Only update the adapter if any progress value actually changed
        if (hasChanges) {
            // ⭐ FIX: Save the current item's position before resetting the adapter.
            val currentPosition = viewPager.currentItem

            adapter.notifyDataSetChanged()

            // Reset the adapter to fix the rendering bug
            viewPager.adapter = null
            viewPager.adapter = adapter

            // ⭐ FIX: Restore the ViewPager to its previous position.
            // The 'false' argument tells it to jump instantly without a smooth scroll animation.
            viewPager.setCurrentItem(currentPosition, false)
        }
    }

    // ⭐ 4. REMOVED saveProgress() and onProgressUpdate()
    // These functions are no longer needed in this activity.
}

