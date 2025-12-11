package com.example.tekbuk.HomepageContent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.R
import com.example.tekbuk.databinding.ActivitySettingsPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding
    private val teacherPrefsName = "TeacherProfile"
    private val keyTeacherPassword = "TeacherPassword"

    // --- ⭐ 1. NEW: DECLARE GOOGLE SIGN-IN CLIENT AND LAUNCHER ---
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val authorizedEmail = "justinelana6@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // No need to apply insets again if done in XML with fitsSystemWindows
        // ViewCompat.setOnApplyWindowInsetsListener(...)

        // --- ⭐ 2. NEW: CONFIGURE GOOGLE SIGN-IN ---
        configureGoogleSignIn()

        loadUserProfile()

        binding.btnlogin.setOnClickListener {
            showLoginDialog()
        }

        binding.btnAddName.setOnClickListener {
            showNameAndSectionDialog()
        }
    }

    private fun configureGoogleSignIn() {
        // Configure Google Sign-In to request the user's ID, email address, and basic profile.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                Toast.makeText(this, "Google Sign-In cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // --- ⭐ 3. NEW: CHECK IF THE SIGNED-IN EMAIL IS THE AUTHORIZED ONE ---
            if (account.email == authorizedEmail) {
                Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()
                // Sign-out immediately so it asks for login next time
                googleSignInClient.signOut().addOnCompleteListener {
                    // Now that authentication is successful, show the change password dialog
                    showChangePasswordDialog()
                }
            } else {
                Toast.makeText(this, "Authentication Failed: This Google account is not authorized.", Toast.LENGTH_LONG).show()
                // Sign out the unauthorized user
                googleSignInClient.signOut()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startGoogleSignInFlow() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
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

        val etPassword = dialogView.findViewById<EditText>(R.id.etDialogPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnDialogLogin)
        val tvChangePassword = dialogView.findViewById<TextView>(R.id.tvChangePassword)

        val prefs = getSharedPreferences(teacherPrefsName, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(keyTeacherPassword, "administrator2025")

        btnLogin.setOnClickListener {
            val enteredPassword = etPassword.text.toString()
            if (enteredPassword == savedPassword) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, TeacherDashboardActivity::class.java))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }

        // --- ⭐ 4. MODIFIED: "Change Password" now starts Google Sign-In ---
        tvChangePassword.setOnClickListener {
            dialog.dismiss() // Close the login dialog first
            // Check if user is already signed in with the correct account
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            if (lastSignedInAccount != null && lastSignedInAccount.email == authorizedEmail) {
                // If already authenticated, go directly to change password
                showChangePasswordDialog()
            } else {
                // Otherwise, start the full sign-in flow
                startGoogleSignInFlow()
            }
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val newDialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val builder = AlertDialog.Builder(this).setView(newDialogView)
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

            val prefs = getSharedPreferences(teacherPrefsName, Context.MODE_PRIVATE)
            prefs.edit().putString(keyTeacherPassword, newPassword).apply()

            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNameAndSectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_name, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
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
