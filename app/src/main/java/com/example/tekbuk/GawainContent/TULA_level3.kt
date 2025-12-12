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

class TULA_level3 : AppCompatActivity() {

    private lateinit var answerEditText: EditText

    companion object {
        const val PREFS_NAME = "TULA_Level3_Progress"
        const val KEY_ESSAY_TEXT = "essay_text"
        // Defines the specific topic ID for this activity
        const val TOPIC_KEY = "TULA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_level3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        answerEditText = findViewById(R.id.etLevel3Answer)

        // Load any previously saved draft logic
        loadSavedText()

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
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnSubmitToTeacher = dialogView.findViewById<Button>(R.id.btnSubmitToTeacher)
        btnSubmitToTeacher.setOnClickListener {
            val answer = answerEditText.text.toString().trim()

            // 1. Save the final answer for the MarkaPage/Teacher Dashboard
            saveReflectionForGrading(answer)

            // 2. Clear the local draft since it is submitted
            clearSavedText()

            Toast.makeText(this, "Ipinadala na sa guro!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // 3. Return result to previous menu
            val resultIntent = Intent().apply {
                putExtra("paksa_id", "tula")
                putExtra("level_completed", 3)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        dialog.show()
    }

    // --- HELPER FUNCTIONS ---

    private fun saveCurrentText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ESSAY_TEXT, answerEditText.text.toString()).apply()
    }

    private fun loadSavedText() {
        // 1. First try to load from the draft
        val draftPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedText = draftPrefs.getString(KEY_ESSAY_TEXT, null)

        if (savedText != null) {
            answerEditText.setText(savedText)
        } else {
            // 2. If no draft, check if they already submitted an answer previously (UserScores)
            // This matches the logic in RepleksyonPageActivity
            val scoresPrefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
            val submittedAnswer = scoresPrefs.getString("${TOPIC_KEY}_LEVEL_3_ANSWER", "")
            if (!submittedAnswer.isNullOrEmpty()) {
                answerEditText.setText(submittedAnswer)
            }
        }
    }

    private fun clearSavedText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ESSAY_TEXT).apply()
    }

    private fun saveReflectionForGrading(answer: String) {
        // We use "UserScores" to match what MarkaPageActivity reads
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save the answer text
        // Key format: "TULA_LEVEL_3_ANSWER"
        editor.putString("${TOPIC_KEY}_LEVEL_3_ANSWER", answer)

        // Save the submitted status flag
        // Key format: "TULA_LEVEL_3_SUBMITTED"
        editor.putBoolean("${TOPIC_KEY}_LEVEL_3_SUBMITTED", true)

        editor.apply()
    }
}
