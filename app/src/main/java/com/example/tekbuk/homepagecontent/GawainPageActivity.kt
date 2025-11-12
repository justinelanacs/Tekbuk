package com.example.tekbuk.homepagecontent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import com.example.tekbuk.R

class GawainPageActivity : AppCompatActivity() {
    private lateinit var levelResultLauncher: ActivityResultLauncher<Intent>

    // keep references so we can update button enabled-state after a level is finished
    private val paksaButtons = mutableMapOf<String, Triple<Button?, Button?, Button?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gawain_page)

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // register result launcher
        levelResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult
            val paksaId = data.getStringExtra("paksa_id") ?: return@registerForActivityResult
            val levelCompleted = data.getIntExtra("level_completed", -1)
            if (levelCompleted <= 0) return@registerForActivityResult

            Log.i("GawainPage", "Returned from LevelActivity paksa=$paksaId completedLevel=$levelCompleted")

            // unlock next level
            val currentlyUnlocked = sharedPref.getInt("unlocked_$paksaId", 1)
            val unlockCandidate = levelCompleted + 1
            if (unlockCandidate > currentlyUnlocked) {
                sharedPref.edit {
                    putInt("unlocked_$paksaId", unlockCandidate)
                }
                val buttons = paksaButtons[paksaId]
                buttons?.first?.isEnabled = true
                buttons?.second?.isEnabled = unlockCandidate >= 2
                buttons?.third?.isEnabled = unlockCandidate >= 3
            }
        }

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

        for (cardId in cardIds) {
            val card = findViewById<CardView>(cardId) ?: continue

            val paksaIdRaw = card.tag?.toString() ?: resources.getResourceEntryName(cardId)
            val paksaId = paksaIdRaw.removePrefix("card").replace("_", "").lowercase()

            Log.d("GawainPage", "Found card id=$cardId paksa='$paksaId'")

            val tvTitle = card.findViewById<TextView>(R.id.paksaTitle)
            val btnLevel1 = card.findViewById<Button>(R.id.btnLevel1)
            val btnLevel2 = card.findViewById<Button>(R.id.btnLevel2)
            val btnLevel3 = card.findViewById<Button>(R.id.btnLevel3)

            paksaButtons[paksaId] = Triple(btnLevel1, btnLevel2, btnLevel3)

            tvTitle?.text = displayTitles[paksaId] ?: paksaId.replaceFirstChar { it.uppercaseChar() }

            val unlocked = sharedPref.getInt("unlocked_$paksaId", 1)

            btnLevel1?.isEnabled = true
            btnLevel2?.isEnabled = unlocked >= 2
            btnLevel3?.isEnabled = unlocked >= 3

            btnLevel1?.setOnClickListener { startLevelActivity(paksaId, 1) }
            btnLevel2?.setOnClickListener { if (btnLevel2.isEnabled) startLevelActivity(paksaId, 2) }
            btnLevel3?.setOnClickListener { if (btnLevel3.isEnabled) startLevelActivity(paksaId, 3) }
        }
    }

    private fun startLevelActivity(paksaId: String, level: Int) {
        Log.i("GawainPage", "Starting LevelActivity paksa=$paksaId level=$level")
        // Make sure LevelActivity class is in the project and the package is correct.
        val intent = Intent(this, LevelActivity::class.java).apply {
            putExtra("paksa_id", paksaId)
            putExtra("level", level)
        }
        levelResultLauncher.launch(intent)
    }
}