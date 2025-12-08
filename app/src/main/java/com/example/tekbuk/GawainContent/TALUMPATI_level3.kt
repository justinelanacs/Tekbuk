package com.example.tekbuk.GawainContent

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R

class TALUMPATI_level3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_talumpati_level3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSubmit = findViewById<Button>(R.id.btnSubmitLevel3)
        btnSubmit.setOnClickListener {
            showScoreDialog()
        }
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
            // Here you would implement the logic to send the answer to the teacher
            Toast.makeText(this, "Ipinadala na sa guro!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish() // Optional: close the activity after submission
        }

        dialog.show()
    }
}