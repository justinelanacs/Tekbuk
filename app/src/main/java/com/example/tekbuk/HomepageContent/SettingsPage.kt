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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding
    private lateinit var auth: FirebaseAuth
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
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
        // Check if a teacher is ALREADY logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (isSessionValid()) {
                // SESSION IS GOOD: Update time and go to dashboard
                updateSessionTimestamp()
                startActivity(Intent(this, TeacherDashboardActivity::class.java))
                return
            } else {
                // SESSION EXPIRED: Force Logout
                auth.signOut()
                Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
                // The code will continue below to show the login dialog...
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Views from dialog_teacher_login.xml
        // IMPORTANT: You need an EMAIL field in your login dialog for Firebase!
        // Assuming you updated dialog_teacher_login.xml to have an EditText for email
        val etEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnDialogLogin) // or btnDialogSubmit
        val tvChangePassword = dialogView.findViewById<TextView>(R.id.tvChangePassword) // or tvRegister

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // ⭐ IMPORTANT: Save the timestamp on success!
                            updateSessionTimestamp()

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, TeacherDashboardActivity::class.java))
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Change Password Button Logic
        tvChangePassword.setOnClickListener {
            dialog.dismiss()
            showChangePasswordDialog()
        }

        dialog.show()
    }
    // 1. Define the Timeout Duration (e.g., 15 minutes in milliseconds)
    // 1000 ms * 60 sec * 15 min = 900,000 ms
    private val SESSION_TIMEOUT_MS = 15 * 60 * 1000L

    // Key for SharedPreferences
    private val PREFS_SESSION = "TeacherSession"
    private val KEY_LAST_LOGIN = "LastLoginTime"

    // ... existing onCreate ...

    // HELPER: Save the current time when the teacher logs in
    private fun updateSessionTimestamp() {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_LOGIN, System.currentTimeMillis()).apply()
    }

    // HELPER: Check if the session has expired
    private fun isSessionValid(): Boolean {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN, 0)
        val currentTime = System.currentTimeMillis()

        // If (Current Time - Last Login) is bigger than Timeout, it's expired
        return (currentTime - lastLoginTime) < SESSION_TIMEOUT_MS
    }

    private fun showChangePasswordDialog() {
        val newDialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(newDialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etOldPassword = newDialogView.findViewById<EditText>(R.id.etDialogOldPassword)
        val etNewPassword = newDialogView.findViewById<EditText>(R.id.etDialogNewPassword)
        val etConfirmPassword = newDialogView.findViewById<EditText>(R.id.etDialogConfirmPassword)
        val btnSave = newDialogView.findViewById<Button>(R.id.btnDialogSavePassword)

        btnSave.setOnClickListener {
            val oldPassword = etOldPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            val user = auth.currentUser

            // 1. Check if user is logged in
            if (user == null || user.email == null) {
                Toast.makeText(this, "No teacher is currently logged in.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 2. Basic Validation
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. RE-AUTHENTICATE (Security Requirement from Firebase)
            // We must verify the old password is correct before changing to a new one
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        // 4. Update Password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(this, "Error updating password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Incorrect Current Password", Toast.LENGTH_SHORT).show()
                    }
                }
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
