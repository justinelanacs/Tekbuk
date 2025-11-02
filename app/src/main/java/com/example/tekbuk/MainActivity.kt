package com.example.tekbuk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.HomapageContent.*
import com.example.tekbuk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Hide default action bar
        supportActionBar?.hide()

        // ✅ Menu button (ImageView)
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // ✅ Access header layout inside NavigationView
        val headerView = binding.navView.getHeaderView(0)
        val btnAddName = headerView.findViewById<Button>(R.id.btnAddName)
        val tvGuestName = headerView.findViewById<TextView>(R.id.tvGuestName)
        val tvProgressCount = headerView.findViewById<TextView>(R.id.tvProgressCount)
        val tvProgressPercent = headerView.findViewById<TextView>(R.id.tvProgressPercent)
        val progressBar = headerView.findViewById<ProgressBar>(R.id.progressBar)

        // Example data (you can replace with dynamic later)
        val lessonsCompleted = 0
        val totalLessons = 9
        val progressPercent = (lessonsCompleted * 100) / totalLessons

        tvProgressCount.text = "$lessonsCompleted/$totalLessons"
        tvProgressPercent.text = "$progressPercent% Tapos na"
        progressBar.progress = lessonsCompleted

        // ✅ Handle “Maglagay ng Pangalan” button click
        btnAddName.setOnClickListener {
            Toast.makeText(this, "Maglagay ng Pangalan clicked!", Toast.LENGTH_SHORT).show()
        }

        // ✅ NavigationView menu items (drawer stays open)
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.miItem1 -> Toast.makeText(this, "Clicked Item 1", Toast.LENGTH_SHORT).show()
                R.id.miItem2 -> Toast.makeText(this, "Clicked Item 2", Toast.LENGTH_SHORT).show()
                R.id.miItem3 -> Toast.makeText(this, "Clicked Item 3", Toast.LENGTH_SHORT).show()
            }
            false // ❌ Don’t auto-close drawer
        }

        // ✅ Card click listeners
        binding.cardLayunin.setOnClickListener {
            startActivity(Intent(this, LayuninPageActivity::class.java))
        }
        binding.cardPaksa.setOnClickListener {
            startActivity(Intent(this, PaksaPageActivity::class.java))
        }
        binding.cardGawain.setOnClickListener {
            startActivity(Intent(this, GawainPageActivity::class.java))
        }
        binding.cardPagtataya.setOnClickListener {
            startActivity(Intent(this, PagtatayaPageActivity::class.java))
        }
        binding.cardRepleksyon.setOnClickListener {
            startActivity(Intent(this, RepleksyonPageActivity::class.java))
        }
        binding.cardIskor.setOnClickListener {
            startActivity(Intent(this, MarkaPageActivity::class.java))
        }
    }
}
