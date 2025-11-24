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

    // Views shared by all content pages
    protected lateinit var hiddenProgress: ProgressBar
    protected lateinit var scrollView: ScrollView
    protected lateinit var btnFinishReading: Button
    protected lateinit var textContent: TextView

    companion object {
        var scrollListener: ScrollProgressListener? = null
    }

    // Each subclass must provide its own raw resource file
    abstract val contentRawRes: Int

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_content) // All content pages share this layout

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        hiddenProgress = findViewById(R.id.hiddenProgress)
        scrollView = findViewById(R.id.scrollView)
        btnFinishReading = findViewById(R.id.btnFinishReading)
        textContent = findViewById(R.id.textContent)

        // Load content from raw resource
        val inputStream = resources.openRawResource(contentRawRes)
        textContent.text = inputStream.bufferedReader().use { it.readText() }

        hiddenProgress.max = 100

        // Update progress as user scrolls
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val contentHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height
            val totalScroll = contentHeight - scrollViewHeight

            if (totalScroll > 0) {
                val progress = (scrollY * 100f / totalScroll).toInt().coerceIn(0, 100)
                hiddenProgress.progress = progress

                // Notify PaksaPageActivity about scroll progress
                scrollListener?.onProgressUpdate(intent.getIntExtra("paksa_index", -1), progress)
            }
        }

        // Finish reading button
        btnFinishReading.setOnClickListener { finish() }

        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
