package com.example.tekbuk.PaksaContent

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R

abstract class BaseContentActivity : AppCompatActivity() {

    // Shared Views
    protected lateinit var hiddenProgress: ProgressBar
    protected lateinit var scrollView: ScrollView
    protected lateinit var btnFinishReading: Button
    protected lateinit var textContent: TextView
    protected lateinit var textTitle: TextView
    protected lateinit var textSubtitle: TextView

    abstract val contentRawRes: Int
    abstract val pageTitle: String
    abstract val pageSubtitle: String
    abstract val textAlignment: Int
    abstract val topicKey: String // e.g., "TULA", "SANAYSAY"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_content)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupContent()
        setupScrollListener()

        btnFinishReading.setOnClickListener { finishReading() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun initializeViews() {
        hiddenProgress = findViewById(R.id.hiddenProgress)
        scrollView = findViewById(R.id.scrollView)
        btnFinishReading = findViewById(R.id.btnFinishReading)
        textContent = findViewById(R.id.textContent)
        textTitle = findViewById(R.id.Title)
        textSubtitle = findViewById(R.id.Subtitle)
    }

    private fun setupContent() {
        textTitle.text = pageTitle
        textSubtitle.text = pageSubtitle
        textContent.text = resources.openRawResource(contentRawRes).bufferedReader().use { it.readText() }
        textContent.gravity = textAlignment
        hiddenProgress.max = 100
    }

    private fun setupScrollListener() {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val contentHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height
            if (contentHeight > scrollViewHeight) {
                val scrollY = scrollView.scrollY
                val totalScroll = contentHeight - scrollViewHeight
                val progress = (scrollY * 100f / totalScroll).toInt().coerceIn(0, 100)
                updateAndSaveProgress(progress)
            } else {
                updateAndSaveProgress(100)
            }
        }
    }

    // ⭐ FIX FOR PROBLEM #1: "Tapusin" button logic
    private fun finishReading() {
        // Directly save 100 to SharedPreferences to guarantee completion
        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)
        prefs.edit().putInt("${topicKey}_progress", 100).apply()

        // Also update the UI progress bar
        hiddenProgress.progress = 100

        // Then, finish the activity
        finish()
    }

    private fun updateAndSaveProgress(progress: Int) {
        hiddenProgress.progress = progress

        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)
        val currentSavedProgress = prefs.getInt("${topicKey}_progress", 0)

        if (progress > currentSavedProgress) {
            prefs.edit().putInt("${topicKey}_progress", progress).apply()
        }
    }

    // --- ⭐ FIX FOR PROBLEM #2: Save and Restore Scroll Position ---

    override fun onResume() {
        super.onResume()
        // Load the scroll position when the user returns
        loadScrollPosition()
    }

    override fun onPause() {
        super.onPause()
        // Save the scroll position when the user leaves
        saveScrollPosition()
    }

    private fun saveScrollPosition() {
        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)
        // Use a unique key for the scroll position, e.g., "TULA_scroll"
        prefs.edit().putInt("${topicKey}_scroll", scrollView.scrollY).apply()
    }

    private fun loadScrollPosition() {
        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)
        val savedScrollY = prefs.getInt("${topicKey}_scroll", 0)

        // Use post to make sure the scroll happens after the layout is fully drawn
        scrollView.post {
            scrollView.scrollTo(0, savedScrollY)
        }
    }
}
