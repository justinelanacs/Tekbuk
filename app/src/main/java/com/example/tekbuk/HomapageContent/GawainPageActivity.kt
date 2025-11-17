package com.example.tekbuk.HomapageContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.tekbuk.R
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class GawainPageActivity : AppCompatActivity() {
    private lateinit var levelResultLauncher: ActivityResultLauncher<Intent>

    // keep references to buttons so we can dynamically unlock levels
    private val paksaButtons = mutableMapOf<String, Triple<Button?, Button?, Button?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Windows boarder format
        enableEdgeToEdge()
        setContentView(R.layout.gawain_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            val paksaIds = listOf("dagli", "sanaysay", "tula", "talumpati", "kwentongbayan")
            paksaIds.forEach { paksa ->
                sharedPref.edit().putInt("unlocked_$paksa", 1).apply()
            }
            sharedPref.edit().putBoolean("isFirstRun", false).apply()
        }


        // Colors
        val unlockedColor = ContextCompat.getColor(this, R.color.two)
        val lockedColor = Color.parseColor("#96838383")

        val cardIds = listOf(
            R.id.cardDagli,
            R.id.cardSanaysay,
            R.id.cardTula,
            R.id.cardTalumpati,
            R.id.cardKwentongBayan
        )

        val displayTitles = mapOf(
            "dagli" to "Dagli",
            "sanaysay" to "Sanaysay",
            "tula" to "Tula",
            "talumpati" to "Talumpati",
            "kwentongbayan" to "Kwentong Bayan",
            "kwentong_bayan" to "Kwentong Bayan"
        )

        // Loop through all cards
        for (cardId in cardIds) {
            val card = findViewById<CardView>(cardId) ?: continue
            val paksaIdRaw = card.tag?.toString() ?: resources.getResourceEntryName(cardId)
            val paksaId = paksaIdRaw.removePrefix("card").replace("_", "").lowercase()

            val tvTitle = card.findViewById<TextView>(R.id.paksaTitle)
            val btnLevel1 = card.findViewById<Button>(R.id.btnLevel1)
            val btnLevel2 = card.findViewById<Button>(R.id.btnLevel2)
            val btnLevel3 = card.findViewById<Button>(R.id.btnLevel3)

            tvTitle?.text = displayTitles[paksaId] ?: paksaId.replaceFirstChar { it.uppercaseChar() }

            // Keep reference for dynamic updates
            paksaButtons[paksaId] = Triple(btnLevel1, btnLevel2, btnLevel3)

            val unlocked = sharedPref.getInt("unlocked_$paksaId", 1)

            // Helper function to update button style
            fun updateButton(button: Button?, level: Int) {
                button?.apply {
                    if (level <= unlocked) {
                        // Animate to unlocked state if previously locked
                        if (!isEnabled) {
                            isEnabled = true
                            animateButtonColor(this, lockedColor, unlockedColor)
                        } else {
                            setBackgroundColor(unlockedColor)
                            alpha = 1f
                        }
                    } else {
                        isEnabled = false
                        setBackgroundColor(lockedColor)
                        alpha = 0.7f
                    }
                }
            }


            // Apply style to all levels
            updateButton(btnLevel1, 1)
            updateButton(btnLevel2, 2)
            updateButton(btnLevel3, 3)

            // Click listeners
            btnLevel1?.setOnClickListener { if (btnLevel1.isEnabled) startLevelActivity(paksaId, 1) }
            btnLevel2?.setOnClickListener { if (btnLevel2.isEnabled) startLevelActivity(paksaId, 2) }
            btnLevel3?.setOnClickListener { if (btnLevel3.isEnabled) startLevelActivity(paksaId, 3) }
        }

        // Register result launcher to unlock next levels dynamically
        levelResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult
            val paksaId = data.getStringExtra("paksa_id") ?: return@registerForActivityResult
            val levelCompleted = data.getIntExtra("level_completed", -1)
            if (levelCompleted <= 0) return@registerForActivityResult

            Log.i("GawainPage", "Returned from LevelActivity paksa=$paksaId completedLevel=$levelCompleted")

            val currentlyUnlocked = sharedPref.getInt("unlocked_$paksaId", 1)

            // Only unlock the next level if the user completed the current sequential level
            if (levelCompleted == currentlyUnlocked) {
                val nextLevel = levelCompleted + 1
                sharedPref.edit { putInt("unlocked_$paksaId", nextLevel) }

                paksaButtons[paksaId]?.apply {
                    // Helper function to refresh buttons
                    fun refreshButton(button: Button?, level: Int) {
                        button?.apply {
                            if (level <= nextLevel) {
                                if (!isEnabled) {
                                    // Animate locked -> unlocked
                                    animateButtonColor(this, lockedColor, unlockedColor)
                                    isEnabled = true
                                }
                                alpha = 1f
                            }
                        }
                    }
                    refreshButton(first, 1)
                    refreshButton(second, 2)
                    refreshButton(third, 3)
                }
            }
        }

    }

    private fun animateButtonColor(button: Button, fromColor: Int, toColor: Int) {
        val colorAnimation = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnimation.duration = 500 // half a second
        colorAnimation.interpolator = DecelerateInterpolator()
        colorAnimation.addUpdateListener { animator ->
            button.setBackgroundColor(animator.animatedValue as Int)
            button.alpha = 1f
        }
        colorAnimation.start()
    }


    private fun startLevelActivity(paksaId: String, level: Int) {
        Log.i("GawainPage", "Starting LevelActivity paksa=$paksaId level=$level")
        val intent = when (paksaId) {
            "tula" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.TULA_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.TULA_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.TULA_level3::class.java)
                else -> null
            }
            "sanaysay" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.SANAYSAY_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.SANAYSAY_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.SANAYSAY_level3::class.java)
                else -> null
            }
            "dagli" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.DAGLI_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.DAGLI_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.DAGLI_level3::class.java)
                else -> null
            }
            "talumpati" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.TALUMPATI_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.TALUMPATI_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.TALUMPATI_level3::class.java)
                else -> null
            }
            "kwentongbayan", "kwentong_bayan" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level3::class.java)
                else -> null
            }
            else -> Intent(this, LevelActivity::class.java).apply {
                putExtra("paksa_id", paksaId)
                putExtra("level", level)
            }
        }

        intent?.let { levelResultLauncher.launch(it) }
    }
}
