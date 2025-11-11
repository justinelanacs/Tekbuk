package com.example.tekbuk.PaksaContent

import android.annotation.SuppressLint
import android.content.Intent
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

class TulaContent : AppCompatActivity() {

    private lateinit var hiddenProgress: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var btnFinishReading: Button
    private lateinit var textContent: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Windows boarder format
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_content)
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

        // ✅ Load text file from res/raw
        val inputStream = resources.openRawResource(R.raw.tulacontent)
        val reader = inputStream.bufferedReader()
        val fileText = reader.use { it.readText() }
        textContent.text = fileText  // ← Set content to TextView

        hiddenProgress.max = 100

        // ✅ Update progress as user scrolls
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val contentHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height

            val totalScroll = contentHeight - scrollViewHeight
            if (totalScroll > 0) {
                val progress = (scrollY * 100f / totalScroll).toInt().coerceIn(0, 100)
                hiddenProgress.progress = progress
            }
        }

        // ✅ “Tapusin ang Pagbasa” button action
        btnFinishReading.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("scroll_progress", hiddenProgress.progress)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // ✅ Handle back button manually
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val resultIntent = Intent()
                resultIntent.putExtra("scroll_progress", hiddenProgress.progress)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        })
    }
}
