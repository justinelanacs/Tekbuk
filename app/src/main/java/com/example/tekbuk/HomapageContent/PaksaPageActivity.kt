package com.example.tekbuk.HomapageContent

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.PaksaContent.*
import com.example.tekbuk.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator


class PaksaPageActivity : AppCompatActivity() {

    private lateinit var tulaProgress: CircularProgressIndicator
    private lateinit var sanaysayProgress: CircularProgressIndicator
    private lateinit var dagliProgress: CircularProgressIndicator
    private lateinit var talumpatiProgress: CircularProgressIndicator
    private lateinit var kwentongBayanProgress: CircularProgressIndicator

    private lateinit var tulaPercent: TextView
    private lateinit var sanaysayPercent: TextView
    private lateinit var dagliPercent: TextView
    private lateinit var talumpatiPercent: TextView
    private lateinit var kwentongBayanPercent: TextView

    // ✅ Register activity results for each content page
    private val tulaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val progress = result.data?.getIntExtra("scroll_progress", 0) ?: 0
            updateProgress(tulaProgress, tulaPercent, progress)
        }
    }

    private val sanaysayLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val progress = result.data?.getIntExtra("scroll_progress", 0) ?: 0
            updateProgress(sanaysayProgress, sanaysayPercent, progress)
        }
    }

    private val dagliLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val progress = result.data?.getIntExtra("scroll_progress", 0) ?: 0
            updateProgress(dagliProgress, dagliPercent, progress)
        }
    }

    private val talumpatiLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val progress = result.data?.getIntExtra("scroll_progress", 0) ?: 0
            updateProgress(talumpatiProgress, talumpatiPercent, progress)
        }
    }

    private val kwentongBayanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val progress = result.data?.getIntExtra("scroll_progress", 0) ?: 0
            updateProgress(kwentongBayanProgress, kwentongBayanPercent, progress)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Windows boarder format
        enableEdgeToEdge()
        setContentView(R.layout.paksa_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // ✅ Connect progress bars and text views
        tulaProgress = findViewById(R.id.tulaProgress)
        sanaysayProgress = findViewById(R.id.sanaysayProgress)
        dagliProgress = findViewById(R.id.dagliProgress)
        talumpatiProgress = findViewById(R.id.talumpatiProgress)
        kwentongBayanProgress = findViewById(R.id.kwentongbayanProgress) // fixed typo

        tulaPercent = findViewById(R.id.tulaprogressPercent)
        sanaysayPercent = findViewById(R.id.sanaysayprogressPercent)
        dagliPercent = findViewById(R.id.dagliprogressPercent)
        talumpatiPercent = findViewById(R.id.talumpatiprogressPercent)
        kwentongBayanPercent = findViewById(R.id.kwentongbayanprogressPercent)

        // ✅ Open each content page
        findViewById<MaterialCardView>(R.id.cardPaksa).setOnClickListener {
            tulaLauncher.launch(Intent(this, TulaContent::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSanaysay).setOnClickListener {
            sanaysayLauncher.launch(Intent(this, SanaysayContent::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardDagli).setOnClickListener {
            dagliLauncher.launch(Intent(this, DagliContent::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardTalumpati).setOnClickListener {
            talumpatiLauncher.launch(Intent(this, TalumpatiContent::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardKwentongbayan).setOnClickListener {
            kwentongBayanLauncher.launch(Intent(this, KwentongbayanContent::class.java))
        }
    }

    // ✅ Helper to smoothly update progress
    private fun updateProgress(progressBar: CircularProgressIndicator, text: TextView, value: Int) {
        progressBar.setProgress(value, true)
        text.text = "$value%"
    }
}
