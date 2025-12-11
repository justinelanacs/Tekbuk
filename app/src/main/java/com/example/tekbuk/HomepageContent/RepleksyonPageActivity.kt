package com.example.tekbuk.HomepageContent

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

class RepleksyonPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.repleksyon_page)

        // Handle Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Initialize Views
        val etReflection = findViewById<EditText>(R.id.etReflection)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // 2. Load existing answer if previously saved (Optional but good for UX)
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val savedAnswer = prefs.getString("REPLEKSYON_MAIN_ANSWER", "")
        if (!savedAnswer.isNullOrEmpty()) {
            etReflection.setText(savedAnswer)
            // Optional: Disable button or change text if already submitted
            btnSubmit.text = "I-update ang Repleksyon"
        }

        // 3. Set Click Listener on Submit Button
        btnSubmit.setOnClickListener {
            val answer = etReflection.text.toString().trim()

            if (answer.isEmpty()) {
                Toast.makeText(this, "Please write your reflection first.", Toast.LENGTH_SHORT).show()
            } else {
                // 4. SAVE THE DATA
                // We use the same 'UserScores' preference file used in MarkaPageActivity
                val editor = prefs.edit()

                // Save the text content
                editor.putString("REPLEKSYON_MAIN_ANSWER", answer)

                // Save the status flag so MarkaPage knows to show "Submitted"
                editor.putBoolean("REPLEKSYON_SUBMITTED", true) // Fixed key name to match MarkaPageActivity

                editor.apply()

                Toast.makeText(this, "Repleksyon saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity and go back
            }
        }
    }
}