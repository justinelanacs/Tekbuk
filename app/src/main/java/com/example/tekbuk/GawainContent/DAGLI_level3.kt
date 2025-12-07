package com.example.tekbuk.GawainContent

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R

class DAGLI_level3 : AppCompatActivity() {

    private lateinit var answerEditText: EditText

    companion object {
        const val PREFS_NAME = "DAGLI_Level3_Progress"
        const val KEY_ESSAY_TEXT = "essay_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dagli_level3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        answerEditText = findViewById(R.id.etLevel3Answer)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitLevel3)
        btnSubmit.setOnClickListener {
            if (answerEditText.text.toString().trim().isNotEmpty()) {
                showScoreDialog()
            } else {
                Toast.makeText(this, "Mangyaring isulat muna ang iyong sagot.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedText()
    }

    override fun onPause() {
        super.onPause()
        saveCurrentText()
    }

    private fun showScoreDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_level3_score, null)
        val builder = AlertDialog.Builder(this).setView(dialogView).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnSubmitToTeacher = dialogView.findViewById<Button>(R.id.btnSubmitToTeacher)
        btnSubmitToTeacher.setOnClickListener {
            saveFinalScore("DAGLI", 3, 3) // Temporary score of 3
            clearSavedText()

            Toast.makeText(this, "Ipinadala na sa guro!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            val resultIntent = Intent().apply {
                putExtra("paksa_id", "dagli")
                putExtra("level_completed", 3)
                putExtra("score", 3)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        dialog.show()
    }

    private fun saveCurrentText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ESSAY_TEXT, answerEditText.text.toString()).apply()
    }

    private fun loadSavedText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedText = prefs.getString(KEY_ESSAY_TEXT, null)
        if (savedText != null) {
            answerEditText.setText(savedText)
        }
    }

    private fun clearSavedText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ESSAY_TEXT).apply()
    }

    private fun saveFinalScore(topic: String, level: Int, scoreToSave: Int) {
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val key = "${topic}_LEVEL_${level}"
        prefs.edit().putInt(key, scoreToSave).apply()
    }
}
