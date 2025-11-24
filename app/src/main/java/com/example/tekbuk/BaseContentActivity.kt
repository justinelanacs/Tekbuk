package com.example.tekbuk.PaksaContent

import android.annotation.SuppressLint
import android.os.Bundle
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

    // Newly added views for dynamic title & subtitle
    protected lateinit var textTitle: TextView
    protected lateinit var textSubtitle: TextView

    companion object {
        var scrollListener: ScrollProgressListener? = null
    }

    // Subclasses MUST provide raw content file, title, and subtitle
    abstract val contentRawRes: Int
    abstract val pageTitle: String
    abstract val pageSubtitle: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_content)

        // Apply edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize main views
        hiddenProgress = findViewById(R.id.hiddenProgress)
        scrollView = findViewById(R.id.scrollView)
        btnFinishReading = findViewById(R.id.btnFinishReading)
        textContent = findViewById(R.id.textContent)

        // Initialize new Title and Subtitle (YOU ADDED THESE IDs)
        textTitle = findViewById(R.id.Title)
        textSubtitle = findViewById(R.id.Subtitle)

        // APPLY TITLE & SUBTITLE DYNAMICALLY
        textTitle.text = pageTitle
        textSubtitle.text = pageSubtitle

        // Load TEXT CONTENT from raw .txt
        val inputStream = resources.openRawResource(contentRawRes)
        textContent.text = inputStream.bufferedReader().use { it.readText() }

        hiddenProgress.max = 100

        // Scroll progress logic
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val contentHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height
            val totalScroll = contentHeight - scrollViewHeight

            if (totalScroll > 0) {
                val progress = (scrollY * 100f / totalScroll).toInt().coerceIn(0, 100)
                hiddenProgress.progress = progress

                // Notify PaksaPageActivity
                scrollListener?.onProgressUpdate(
                    intent.getIntExtra("paksa_index", -1),
                    progress
                )
            }
        }

        // Finish Reading Button
        btnFinishReading.setOnClickListener { finish() }

        // Back Button Handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
