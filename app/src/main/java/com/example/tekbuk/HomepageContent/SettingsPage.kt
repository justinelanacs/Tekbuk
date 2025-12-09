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
import com.google.firebase.auth.FirebaseAuth

class SettingsPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

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
        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnSubmit = dialogView.findViewById<android.widget.Button>(R.id.btnDialogSubmit)
        val tvForgot = dialogView.findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = dialogView.findViewById<TextView>(R.id.tvRegister)


        // 1. LOGIN LOGIC
        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Determine if we are logging in or registering based on button text
                if (btnSubmit.text == "MAG-LOGIN") {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                // TODO: Navigate to Teacher Dashboard Activity here
                            } else {
                                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // REGISTRATION LOGIC
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                // Optionally save teacher details to Firestore here
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. SWITCH TO REGISTER MODE
        tvRegister.setOnClickListener {
            if (btnSubmit.text == "MAG-LOGIN") {
                // Switch UI to Register
                btnSubmit.text = "REGISTER"
                tvRegister.text = "Already have an account? Login"
                // Optional: Change title if you added an ID to the TextView "Teacher Login"
                // tvTitle.text = "Create Account"
            } else {
                // Switch UI back to Login
                btnSubmit.text = "MAG-LOGIN"
                tvRegister.text = "Create Teacher Account"
                // tvTitle.text = "Teacher Login"
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

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
