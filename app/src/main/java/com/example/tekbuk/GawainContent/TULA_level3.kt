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

    // ⭐ Define keys for saving and loading the text and completion status
    companion object {
        const val PREFS_NAME = "TULA_Level3_Progress"
        const val KEY_ESSAY_TEXT = "essay_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_level3)

        // Standard window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the EditText from your layout
        answerEditText = findViewById(R.id.etLevel3Answer)

        // Set up the submit button
        val btnSubmit = findViewById<Button>(R.id.btnSubmitLevel3)
        btnSubmit.setOnClickListener {
            // Check if the user has written anything before allowing submission
            if (answerEditText.text.toString().trim().isNotEmpty()) {
                showScoreDialog()
            } else {
                Toast.makeText(this, "Mangyaring isulat muna ang iyong sagot.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ⭐ [FIX] Restore the saved text when the user returns to the activity
        loadSavedText()
    }

    override fun onPause() {
        super.onPause()
        // ⭐ [FIX] Save the current text when the user leaves the activity (e.g., presses back)
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
            // ⭐ [ADD] Save a score to MarkaPage when the user submits
            // Assuming a full score of 10 for completion. You can change this value.
            saveFinalScore("TULA", 3, 3)

            // Clear the saved essay text since it's now "submitted"
            clearSavedText()

            Toast.makeText(this, "Ipinadala na sa guro!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // ⭐ [ADD] Set the activity result to unlock the next section in GawainPage
            val resultIntent = Intent().apply {
                putExtra("paksa_id", "tula")
                putExtra("level_completed", 3)
                putExtra("score", 3) // Sending the score back
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Close the activity
        }

        dialog.show()
    }

    /**
     * Saves the current text from the EditText to SharedPreferences.
     */
    private fun saveCurrentText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ESSAY_TEXT, answerEditText.text.toString()).apply()
    }

    /**
     * Loads the saved text from SharedPreferences and sets it into the EditText.
     */
    private fun loadSavedText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedText = prefs.getString(KEY_ESSAY_TEXT, null)
        if (savedText != null) {
            answerEditText.setText(savedText)
        }
    }

    /**
     * Clears the saved essay text from SharedPreferences after submission.
     */
    private fun clearSavedText() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ESSAY_TEXT).apply()
    }

    /**
     * Saves the final score to the shared "UserScores" file so MarkaPageActivity can find it.
     */
    private fun saveFinalScore(topic: String, level: Int, scoreToSave: Int) {
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val key = "${topic}_LEVEL_${level}" // Creates key like "TULA_LEVEL_3"
        editor.putInt(key, scoreToSave)
        editor.apply()
    }
}
