package com.example.tekbuk.HomepageContent

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.databinding.ActivitySettingsPageBinding
import com.example.tekbuk.R
import android.widget.Button
import android.widget.TextView
class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)

        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val btnLogin = findViewById<Button>(R.id.btnlogin)
        btnLogin.setOnClickListener {
            showLoginDialog()
        }

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAddName.setOnClickListener {
            showNameAndSectionDialog()
        }
    }
    private fun showLoginDialog() {
        // 1. Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)

        // 2. Create the AlertDialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Create the dialog instance but don't show it yet
        val dialog = builder.create()

        // Make background transparent so the rounded CardView shows properly
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 3. Initialize views INSIDE the popup
        val etEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnSubmit = dialogView.findViewById<android.widget.Button>(R.id.btnDialogSubmit)
        val tvForgot = dialogView.findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = dialogView.findViewById<TextView>(R.id.tvRegister)

        // 4. Handle Submit Click
        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // TODO: Add your Online Database/Firebase Login Logic Here
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Handle Forgot Password
        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
            // Add logic to reset password
        }

        // 6. Handle Register
        tvRegister.setOnClickListener {
            Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            // Navigate to a Registration Activity if you have one
        }

        // 7. Show the dialog
        dialog.show()
    }

    private fun showNameAndSectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ilagay ang Pangalan at Seksyon")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 30, 40, 30)

        val nameInput = EditText(this)
        nameInput.hint = "Ilagay ang iyong pangalan"
        layout.addView(nameInput)

        val sectionInput = EditText(this)
        sectionInput.hint = "Ilagay ang iyong section"
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 25
        sectionInput.layoutParams = params
        layout.addView(sectionInput)

        builder.setView(layout)

        builder.setPositiveButton("Save", null) // override later
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        // Override the positive button to prevent dismissing automatically
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = nameInput.text.toString().trim().uppercase()
            val section = sectionInput.text.toString().trim().uppercase()

            if (name.isEmpty()) {
                Toast.makeText(this, "Maglagay ng pangalan bago i-save!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (section.isEmpty()) {
                Toast.makeText(this, "Maglagay ng section bago i-save!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.stdname.text = name
            binding.stdsection.text = section
            dialog.dismiss()
        }
    }
}
