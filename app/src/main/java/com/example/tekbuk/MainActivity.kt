package com.example.tekbuk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardPaksa.setOnClickListener {
            startActivity(Intent(this, PaksaPageActivity::class.java))
        }

        binding.cardGawain.setOnClickListener {
            startActivity(Intent(this, GawainPageActivity::class.java))
        }

        // ⭐ MODIFIED THIS CLICK LISTENER ⭐
        binding.cardPagtataya.setOnClickListener {
            // Check if all "Paksa" topics are 100% complete
            if (areAllPaksaCompleted()) {
                // If complete, open the Pagtataya page
                startActivity(Intent(this, PagtatayaPageActivity::class.java))
            } else {
                // If not complete, show the same pop-up dialog
                showPaksaIncompleteDialog()
            }
        }

        binding.cardRepleksyon.setOnClickListener {
            // Check if all "Paksa" topics are 100% complete
            if (areAllPaksaCompleted()) {
                // If complete, open the Repleksyon page
                startActivity(Intent(this, RepleksyonPageActivity::class.java))
            } else {
                // If not complete, show the pop-up dialog
                showPaksaIncompleteDialog()
            }
        }

        binding.cardMarka.setOnClickListener {
            startActivity(Intent(this, MarkaPageActivity::class.java))
        }

        binding.menuButton.setOnClickListener {
            startActivity(Intent(this, SettingsPage::class.java))
        }
    }

    /**
     * Checks SharedPreferences to see if all Paksa topics have a progress of 100.
     * This function is now reused for both Pagtataya and Repleksyon.
     */
    private fun areAllPaksaCompleted(): Boolean {
        val topics = listOf("TULA", "SANAYSAY", "DAGLI", "TALUMPATI", "KWENTONG BAYAN")
        val prefs = getSharedPreferences("PaksaProgress", Context.MODE_PRIVATE)

        return topics.all { topic ->
            prefs.getInt("${topic}_progress", 0) == 100
        }
    }

    /**
     * Displays a custom CardView dialog informing the user to complete all topics.
     * This function is also reused.
     */
    private fun showPaksaIncompleteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_paksa_incomplete, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnUnderstood: Button = dialogView.findViewById(R.id.btnDialogUnderstood)
        btnUnderstood.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
