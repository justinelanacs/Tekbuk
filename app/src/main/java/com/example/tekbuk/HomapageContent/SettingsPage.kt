package com.example.tekbuk.HomapageContent

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

class SettingsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

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
