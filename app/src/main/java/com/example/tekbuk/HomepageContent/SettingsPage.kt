package com.example.tekbuk.HomepageContent

import android.app.Activity // ⭐ ADD THIS IMPORT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // ⭐ ADD THIS IMPORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.R
import com.example.tekbuk.databinding.ActivitySettingsPageBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding
    private lateinit var auth: FirebaseAuth

    private val PREFS_SESSION = "TeacherSession"
    private val KEY_LAST_LOGIN = "LastLoginTime"
    private val SESSION_TIMEOUT_MS = 15 * 60 * 1000L

    // ⭐ STEP 1: Create the launcher
    private val dashboardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // This block will execute when TeacherDashboardActivity closes.
        if (result.resultCode == Activity.RESULT_OK) {
            // This means we came back after a successful logout.
            // We can show a toast or simply do nothing, as the user is now logged out.
            Toast.makeText(this, "Ready to log in again.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Removed enableEdgeToEdge and manual insets for simplicity with this logic
        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        loadUserProfile()

        binding.btnlogin.setOnClickListener {
            showLoginDialog()
        }

        binding.btnAddName.setOnClickListener {
            showNameAndSectionDialog()
        }
    }

    // ⭐ STEP 2: Create a helper function to start the dashboard
    private fun goToDashboard() {
        val intent = Intent(this, TeacherDashboardActivity::class.java)
        dashboardLauncher.launch(intent)
    }

    private fun showLoginDialog() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (isSessionValid()) {
                updateSessionTimestamp()
                goToDashboard() // Use the new launcher method
                return
            } else {
                auth.signOut()
                clearSessionTimestamp()
                Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnDialogLogin)
        val tvChangePassword = dialogView.findViewById<TextView>(R.id.tvChangePassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            updateSessionTimestamp()
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            goToDashboard() // Use the new launcher method
                        } else {
                            Toast.makeText(this, "Login Failed: Incorrect email or password.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        tvChangePassword.setOnClickListener {
            dialog.dismiss()
            showChangePasswordDialog()
        }

        dialog.show()
    }

    // ... The rest of your code (updateSessionTimestamp, clearSessionTimestamp, etc.) remains exactly the same ...
    // HELPER: Save the current time when the teacher logs in
    private fun updateSessionTimestamp() {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_LOGIN, System.currentTimeMillis()).apply()
    }

    // HELPER: Clear the session timestamp on logout/expiration
    private fun clearSessionTimestamp() {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LAST_LOGIN).apply()
    }

    // HELPER: Check if the session has expired
    private fun isSessionValid(): Boolean {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN, 0)

        // If lastLoginTime is 0, it means user was logged out, so session is invalid
        if (lastLoginTime == 0L) return false

        val currentTime = System.currentTimeMillis()

        // If (Current Time - Last Login) is bigger than Timeout, it's expired
        return (currentTime - lastLoginTime) < SESSION_TIMEOUT_MS
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = prefs.getString("StudentName", "STUDENT NAME")
        val section = prefs.getString("StudentSection", "SECTION")
        binding.stdname.text = name
        binding.stdsection.text = section
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

            if (user == null || user.email == null) {
                Toast.makeText(this, "You must be logged in to change your password. Please log in first.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                showLoginDialog()
                return@setOnClickListener
            }
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

            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
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
                        Toast.makeText(this, "Authentication failed: Incorrect Current Password", Toast.LENGTH_SHORT).show()
                    }
                }
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
