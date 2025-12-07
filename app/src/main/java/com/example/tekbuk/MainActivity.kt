package com.example.tekbuk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.HomepageContent.*
import com.example.tekbuk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Handle system bars properly
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Set click actions for each card (fixed positions)
        findViewById<View>(R.id.cardPaksa).setOnClickListener {
            startActivity(Intent(this, PaksaPageActivity::class.java))
        }

        findViewById<View>(R.id.cardGawain).setOnClickListener {
            startActivity(Intent(this, GawainPageActivity::class.java))
        }

        findViewById<View>(R.id.cardPagtataya).setOnClickListener {
            startActivity(Intent(this, PagtatayaPageActivity::class.java))
        }

        findViewById<View>(R.id.cardRepleksyon).setOnClickListener {
            startActivity(Intent(this, RepleksyonPageActivity::class.java))
        }

        findViewById<View>(R.id.cardMarka).setOnClickListener {
            startActivity(Intent(this, MarkaPageActivity::class.java))
        }

        // ✅ Make menuButton clickable
        findViewById<ImageView>(R.id.menuButton).setOnClickListener {
            startActivity(Intent(this, SettingsPage::class.java))
        }
    }
}
