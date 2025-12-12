package com.example.tekbuk.HomepageContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.R
import com.example.tekbuk.databinding.ActivitySettingsPageBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding
    private lateinit var auth: FirebaseAuth

    // Constants for SharedPreferences keys for better maintenance
    private val PREFS_SESSION = "TeacherSession"
    private val KEY_LAST_LOGIN = "LastLoginTime"
    private val SESSION_TIMEOUT_MS = 15 * 60 * 1000L

    private val PREFS_USER_PROFILE = "UserProfile"
    private val KEY_STUDENT_NAME = "StudentName"
    private val KEY_STUDENT_SECTION = "StudentSection"
    private val KEY_NAME_IS_SET = "NameIsSet" // Flag to check if the name is permanently set

    private val dashboardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Ready to log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        loadUserProfile() // This function now contains the logic to show/hide the button

        binding.btnlogin.setOnClickListener {
            showLoginDialog()
        }

        binding.btnAddName.setOnClickListener {
            // The button click now starts the two-step process by showing the notice first
            showNameNoticeDialog()
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences(PREFS_USER_PROFILE, Context.MODE_PRIVATE)
        val isNameSet = prefs.getBoolean(KEY_NAME_IS_SET, false)

        if (isNameSet) {
            // If the name is permanently set, load it and hide the button
            val name = prefs.getString(KEY_STUDENT_NAME, "")
            val section = prefs.getString(KEY_STUDENT_SECTION, "")
            binding.stdname.text = name
            binding.stdsection.text = section

            binding.btnAddName.visibility = View.GONE // Hide the button permanently
            binding.stdname.visibility = View.VISIBLE
            binding.stdsection.visibility = View.VISIBLE

        } else {
            // If the name is not set, show the button and hide the text views
            binding.btnAddName.visibility = View.VISIBLE
            binding.stdname.visibility = View.GONE
            binding.stdsection.visibility = View.GONE
        }
    }

    /**
     * Step 1 of the workflow: Show a warning dialog.
     */
    private fun showNameNoticeDialog() {
        // You must have 'dialog_name_notice.xml' in your layout folder for this to work.
        val dialogView = layoutInflater.inflate(R.layout.dialog_name_notice, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnNoticeCancel)
        val btnContinue = dialogView.findViewById<Button>(R.id.btnNoticeContinue)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnContinue.setOnClickListener {
            dialog.dismiss()
            showNameAndSectionInputDialog() // On continue, proceed to the input dialog
        }

        dialog.show()
    }

    /**
     * Step 2 of the workflow: Show the actual input dialog.
     */
    private fun showNameAndSectionInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_name, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val nameInput = dialogView.findViewById<EditText>(R.id.etName)
        val sectionInput = dialogView.findViewById<EditText>(R.id.etSection)
        val btnSave = dialogView.findViewById<Button>(R.id.btnDialogSave)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnDialogCancel)

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim().uppercase()
            val section = sectionInput.text.toString().trim().uppercase()

            if (name.isEmpty() || section.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the data and set the permanent flag
            val prefs = getSharedPreferences(PREFS_USER_PROFILE, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString(KEY_STUDENT_NAME, name)
                putString(KEY_STUDENT_SECTION, section)
                putBoolean(KEY_NAME_IS_SET, true) // This flag ensures the button will be hidden next time
                apply()
            }

            Toast.makeText(this, "Datos ay nai-save!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // Reload the UI immediately to hide the button and show the name/section
            loadUserProfile()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // --- All other functions (Login, Password Change, etc.) remain unchanged ---

    private fun goToDashboard() {
        val intent = Intent(this, TeacherDashboardActivity::class.java)
        dashboardLauncher.launch(intent)
    }

    private fun showLoginDialog() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (isSessionValid()) {
                updateSessionTimestamp()
                goToDashboard()
                return
            } else {
                auth.signOut()
                clearSessionTimestamp()
                Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_teacher_login, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
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
                            goToDashboard()
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

    private fun updateSessionTimestamp() {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_LOGIN, System.currentTimeMillis()).apply()
    }

    private fun clearSessionTimestamp() {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LAST_LOGIN).apply()
    }

    private fun isSessionValid(): Boolean {
        val prefs = getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN, 0)
        if (lastLoginTime == 0L) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastLoginTime) < SESSION_TIMEOUT_MS
    }

    private fun showChangePasswordDialog() {
        val newDialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val builder = AlertDialog.Builder(this).setView(newDialogView)
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
            user.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
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
}
