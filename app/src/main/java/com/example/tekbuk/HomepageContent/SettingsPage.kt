package com.example.tekbuk.HomepageContent

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R
import com.example.tekbuk.databinding.ActivitySettingsPageBinding

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Must be called before setting content view with binding

        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ⭐ 2. LOAD USER PROFILE ON START
        // This ensures that the saved name and section are displayed when the page opens.
        loadUserProfile()

        // Set OnClick Listeners
        binding.btnlogin.setOnClickListener {
            showLoginDialog()
        }

        binding.btnAddName.setOnClickListener {
            showNameAndSectionDialog()
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = prefs.getString("StudentName", "STUDENT NAME")
        val section = prefs.getString("StudentSection", "SECTION")
        binding.stdname.text = name
        binding.stdsection.text = section
    }

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnDialogSubmit)
        val tvForgot = dialogView.findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = dialogView.findViewById<TextView>(R.id.tvRegister)

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNameAndSectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_name, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val nameInput = dialogView.findViewById<EditText>(R.id.etName)
        val sectionInput = dialogView.findViewById<EditText>(R.id.etSection)
        val btnSave = dialogView.findViewById<Button>(R.id.btnDialogSave)
        // ⭐ 1. FIX: The Cancel button is a TextView in the new layout.
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnDialogCancel)

        // Pre-fill EditTexts if there's already a name and section
        if (binding.stdname.text.isNotEmpty() && binding.stdname.text != "STUDENT NAME") {
            nameInput.setText(binding.stdname.text)
        }
        if (binding.stdsection.text.isNotEmpty() && binding.stdsection.text != "SECTION") {
            sectionInput.setText(binding.stdsection.text)
        }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim().uppercase()
            val section = sectionInput.text.toString().trim().uppercase()

            if (name.isEmpty()) {
                Toast.makeText(this, "Maglagay ng pangalan bago i-save!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (section.isEmpty()) {
                Toast.makeText(this, "Maglagay ng seksyon bago i-save!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.stdname.text = name
            binding.stdsection.text = section

            val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString("StudentName", name)
                putString("StudentSection", section)
                apply()
            }

            Toast.makeText(this, "Datos ay nai-save!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
