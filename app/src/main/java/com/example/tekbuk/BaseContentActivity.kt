package com.example.tekbuk.PaksaContent

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity // ⭐ 1. IMPORT THIS
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

    companion object {
        var scrollListener: ScrollProgressListener? = null
    }

    // Subclasses MUST provide these values
    abstract val contentRawRes: Int
    abstract val pageTitle: String
    abstract val pageSubtitle: String
    // ⭐ 2. ADD THIS ABSTRACT PROPERTY
    // This forces child classes (TulaContent, SanaysayContent) to specify an alignment.
    abstract val textAlignment: Int

    private var paksaIndex: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // It's okay to use activity_tula_content, as we will override its properties
        setContentView(R.layout.activity_tula_content)

        paksaIndex = intent.getIntExtra("paksa_index", -1)

        // Apply edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize all views
        hiddenProgress = findViewById(R.id.hiddenProgress)
        scrollView = findViewById(R.id.scrollView)
        btnFinishReading = findViewById(R.id.btnFinishReading)
        textContent = findViewById(R.id.textContent)
        textTitle = findViewById(R.id.Title)
        textSubtitle = findViewById(R.id.Subtitle)

        // APPLY DYNAMIC CONTENT
        textTitle.text = pageTitle
        textSubtitle.text = pageSubtitle
        textContent.text = resources.openRawResource(contentRawRes).bufferedReader().use { it.readText() }

        // ⭐ 3. OVERRIDE THE XML ALIGNMENT
        // This line programmatically sets the gravity of the TextView,
        // ignoring what's in the XML file.
        textContent.gravity = textAlignment

        hiddenProgress.max = 100

        // Scroll progress logic
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val contentHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height
            // Check if there is anything to scroll
            if (contentHeight > scrollViewHeight) {
                val scrollY = scrollView.scrollY
                val totalScroll = contentHeight - scrollViewHeight
                val progress = (scrollY * 100f / totalScroll).toInt().coerceIn(0, 100)
                notifyProgress(progress)
            } else {
                // ⭐ FIX: If content is not scrollable, it's considered 100% read
                notifyProgress(100)
            }
        }

        // ⭐ FIX: "Finish Reading" button now properly updates progress to 100%
        btnFinishReading.setOnClickListener { finishReading() }

        // ⭐ FIX: The Back Button now properly updates progress to 100%
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishReading()
            }
        })
    }

    /**
     * ⭐ ADDED: Centralized method to ensure progress is set to 100% on finish.
     */
    private fun finishReading() {
        notifyProgress(100) // Notify that the progress is 100%
        finish() // Then, finish the activity
    }

    /**
     * ⭐ ADDED: A cleaner helper function to send progress updates.
     */
    private fun notifyProgress(progress: Int) {
        hiddenProgress.progress = progress
        if (paksaIndex != -1) {
            scrollListener?.onProgressUpdate(paksaIndex, progress)
        }
    }
}
