package com.example.tekbuk.HomepageContent

import android.content.Context
import android.content.Intent
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
    // Key for storing the teacher password
    private val teacherPrefsName = "TeacherProfile"
    private val keyTeacherPassword = "TeacherPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserProfile()

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

        // Get views from the new layout
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnDialogLogin)
        val tvChangePassword = dialogView.findViewById<TextView>(R.id.tvChangePassword)

        // Get the saved password, with "administrator2025" as the default
        val prefs = getSharedPreferences(teacherPrefsName, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(keyTeacherPassword, "administrator2025")

        // "PASOK" button logic
        btnLogin.setOnClickListener {
            val enteredPassword = etPassword.text.toString()
            if (enteredPassword == savedPassword) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                // Open the new empty activity
                startActivity(Intent(this, TeacherDashboardActivity::class.java))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }

        // "Change Password" text logic
        tvChangePassword.setOnClickListener {
            dialog.dismiss() // Close the current dialog
            showChangePasswordDialog() // Open the new one
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        // Here we'll use a dialog with two password fields and a save button.
        // I will design it programmatically for simplicity, but you can create a new XML.
        val newDialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(newDialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNewPassword = newDialogView.findViewById<EditText>(R.id.etDialogNewPassword)
        val etConfirmPassword = newDialogView.findViewById<EditText>(R.id.etDialogConfirmPassword)
        val btnSave = newDialogView.findViewById<Button>(R.id.btnDialogSavePassword)

        btnSave.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the new password to SharedPreferences
            val prefs = getSharedPreferences(teacherPrefsName, Context.MODE_PRIVATE)
            prefs.edit().putString(keyTeacherPassword, newPassword).apply()

            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNameAndSectionDialog() {
        // This function remains unchanged
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_name, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val nameInput = dialogView.findViewById<EditText>(R.id.etName)
        val sectionInput = dialogView.findViewById<EditText>(R.id.etSection)
        val btnSave = dialogView.findViewById<Button>(R.id.btnDialogSave)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnDialogCancel)

        if (binding.stdname.text.isNotEmpty() && binding.stdname.text != "STUDENT NAME") {
            nameInput.setText(binding.stdname.text)
        }
        if (binding.stdsection.text.isNotEmpty() && binding.stdsection.text != "SECTION") {
            sectionInput.setText(binding.stdsection.text)
        }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim().uppercase()
            val section = sectionInput.text.toString().trim().uppercase()

            if (name.isEmpty() || section.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
