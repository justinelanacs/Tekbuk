package com.example.tekbuk

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.HomapageContent.GawainPageActivity
import com.example.tekbuk.HomapageContent.LayuninPageActivity
import com.example.tekbuk.HomapageContent.MarkaPageActivity
import com.example.tekbuk.HomapageContent.PagtatayaPageActivity
import com.example.tekbuk.HomapageContent.PaksaPageActivity
import com.example.tekbuk.HomapageContent.RepleksyonPageActivity
import com.example.tekbuk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 CARD CLICK LISTENERS
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
