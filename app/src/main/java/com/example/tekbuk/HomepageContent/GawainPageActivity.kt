package com.example.tekbuk.HomepageContent

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import com.example.tekbuk.R
import com.google.android.material.card.MaterialCardView
import android.widget.ImageView
import android.widget.TextView

class GawainPageActivity : AppCompatActivity() {

    private lateinit var levelResultLauncher: ActivityResultLauncher<Intent>
    private val paksaCards = mutableMapOf<String, Triple<MaterialCardView?, MaterialCardView?, MaterialCardView?>>()
    private val paksaStars = mutableMapOf<String, Triple<List<ImageView>, List<ImageView>, List<ImageView>>>()
    private lateinit var sharedPref: SharedPreferences

    // Colors can be lazy-initialized
    private val unlockedColor by lazy { ContextCompat.getColor(this, R.color.two) }
    private val lockedColor by lazy { Color.parseColor("#96838383") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.gawain_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences once
        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        initializeFirstRun()
        setupCardViews()
        setupLevelResultLauncher()
    }

    // ⭐ [ADDITION] onResume is crucial for updating the UI after returning from a level.
    override fun onResume() {
        super.onResume()
        // Refresh the lock status every time the activity is shown.
        updateAllCardStates()
    }

    private fun initializeFirstRun() {
        if (sharedPref.getBoolean("isFirstRun", true)) {
            listOf("dagli", "sanaysay", "tula", "talumpati", "kwentongbayan").forEach { paksa ->
                sharedPref.edit { putInt("unlocked_$paksa", 1) }
            }
            sharedPref.edit { putBoolean("isFirstRun", false) }
        }
    }

    private fun setupCardViews() {
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
            "kwentongbayan" to "Kwentong Bayan"
        )

        for (cardId in cardIds) {
            val card = findViewById<CardView>(cardId) ?: continue
            val paksaIdRaw = card.tag?.toString() ?: resources.getResourceEntryName(cardId)
            // ⭐ [RELIABLE FIX] Always create the ID without underscores.
            val paksaId = paksaIdRaw.removePrefix("card").replace("_", "").lowercase()

            val tvTitle = card.findViewById<TextView>(R.id.paksaTitle)
            val level1Card = card.findViewById<MaterialCardView>(R.id.btnLevel1)
            val level2Card = card.findViewById<MaterialCardView>(R.id.btnLevel2)
            val level3Card = card.findViewById<MaterialCardView>(R.id.btnLevel3)

            tvTitle?.text = displayTitles[paksaId] ?: paksaId.replaceFirstChar { it.uppercaseChar() }

            paksaCards[paksaId] = Triple(level1Card, level2Card, level3Card)
            paksaStars[paksaId] = Triple(
                listOfNotNull(level1Card?.findViewById<ImageView>(R.id.star1_1)),
                listOfNotNull(level2Card?.findViewById(R.id.star2_1), level2Card?.findViewById(R.id.star2_2)),
                listOfNotNull(level3Card?.findViewById(R.id.star3_1), level3Card?.findViewById(R.id.star3_2), level3Card?.findViewById(R.id.star3_3))
            )

            // Set click listeners
            listOf(level1Card to 1, level2Card to 2, level3Card to 3).forEach { (cardView, level) ->
                cardView?.setOnClickListener {
                    if (cardView.isEnabled) startLevelActivity(paksaId, level)
                }
            }
        }
    }

    private fun updateAllCardStates() {
        paksaCards.keys.forEach { paksaId ->
            val (level1Card, level2Card, level3Card) = paksaCards[paksaId] ?: return@forEach
            val (level1Stars, level2Stars, level3Stars) = paksaStars[paksaId] ?: return@forEach

            val unlockedLevel = sharedPref.getInt("unlocked_$paksaId", 1)

            updateCardUI(level1Card, level1Stars, 1, unlockedLevel)
            updateCardUI(level2Card, level2Stars, 2, unlockedLevel)
            updateCardUI(level3Card, level3Stars, 3, unlockedLevel)
        }
    }

    private fun updateCardUI(cardView: MaterialCardView?, stars: List<ImageView>, level: Int, unlockedLevel: Int) {
        cardView?.apply {
            if (level <= unlockedLevel) {
                isClickable = true
                isEnabled = true
                setCardBackgroundColor(unlockedColor)
                alpha = 1f
                stars.forEach { it.setImageResource(R.drawable.starshade_icon) }
            } else {
                isClickable = false
                isEnabled = false
                setCardBackgroundColor(lockedColor)
                alpha = 0.7f
                stars.forEach { it.setImageResource(R.drawable.starblank_icon) }
            }
        }
    }

    private fun setupLevelResultLauncher() {
        levelResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            // ⭐ [RELIABLE FIX] Normalize the ID from the result to match our internal standard.
            val receivedPaksaId = data.getStringExtra("paksa_id") ?: return@registerForActivityResult
            val paksaId = receivedPaksaId.replace("_", "").lowercase()

            val levelCompleted = data.getIntExtra("level_completed", -1)
            if (levelCompleted <= 0) return@registerForActivityResult

            val currentlyUnlocked = sharedPref.getInt("unlocked_$paksaId", 1)
            if (levelCompleted == currentlyUnlocked && levelCompleted < 3) {
                val nextLevel = levelCompleted + 1
                sharedPref.edit { putInt("unlocked_$paksaId", nextLevel) }

                // Animate the unlocking of the next level
                val (level1Card, level2Card, level3Card) = paksaCards[paksaId] ?: return@registerForActivityResult
                val (level1Stars, level2Stars, level3Stars) = paksaStars[paksaId] ?: return@registerForActivityResult

                val cardToUnlock = when (nextLevel) {
                    2 -> level2Card
                    3 -> level3Card
                    else -> null
                }
                val starsToUnlock = when (nextLevel) {
                    2 -> level2Stars
                    3 -> level3Stars
                    else -> emptyList()
                }

                cardToUnlock?.let { animateUnlock(it, starsToUnlock) }
            }
        }
    }

    private fun animateUnlock(cardView: MaterialCardView, stars: List<ImageView>) {
        cardView.apply {
            isClickable = true
            isEnabled = true
            alpha = 1f
            stars.forEach { it.setImageResource(R.drawable.starshade_icon) }

            val colorAnim = ValueAnimator.ofArgb(lockedColor, unlockedColor).apply {
                duration = 500
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    setCardBackgroundColor(animator.animatedValue as Int)
                }
            }
            colorAnim.start()
        }
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
            // ⭐ [RELIABLE FIX] Only need one entry for "kwentongbayan".
            "kwentongbayan" -> when (level) {
                1 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level1::class.java)
                2 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level2::class.java)
                3 -> Intent(this, com.example.tekbuk.GawainContent.KWENTONGBAYAN_level3::class.java)
                else -> null
            }
            else -> null // We handle all known cases, so this shouldn't be hit.
        }
        intent?.let { levelResultLauncher.launch(it) }
    }
}
