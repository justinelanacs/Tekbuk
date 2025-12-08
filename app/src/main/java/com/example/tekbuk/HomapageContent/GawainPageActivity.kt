package com.example.tekbuk.HomapageContent

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.animation.ValueAnimator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.gawain_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        if (sharedPref.getBoolean("isFirstRun", true)) {
            listOf("dagli", "sanaysay", "tula", "talumpati", "kwentongbayan").forEach { paksa ->
                sharedPref.edit { putInt("unlocked_$paksa", 1) }
            }
            sharedPref.edit { putBoolean("isFirstRun", false) }
        }

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

        for (cardId in cardIds) {
            val card = findViewById<CardView>(cardId) ?: continue
            val paksaIdRaw = card.tag?.toString() ?: resources.getResourceEntryName(cardId)
            val paksaId = paksaIdRaw.removePrefix("card").replace("_", "").lowercase()

            val tvTitle = card.findViewById<TextView>(R.id.paksaTitle)
            val level1Card = card.findViewById<MaterialCardView>(R.id.btnLevel1)
            val level2Card = card.findViewById<MaterialCardView>(R.id.btnLevel2)
            val level3Card = card.findViewById<MaterialCardView>(R.id.btnLevel3)

            val level1Stars = listOfNotNull(level1Card?.findViewById<ImageView>(R.id.star1_1))
            val level2Stars = listOfNotNull(
                level2Card?.findViewById<ImageView>(R.id.star2_1),
                level2Card?.findViewById<ImageView>(R.id.star2_2)
            )
            val level3Stars = listOfNotNull(
                level3Card?.findViewById<ImageView>(R.id.star3_1),
                level3Card?.findViewById<ImageView>(R.id.star3_2),
                level3Card?.findViewById<ImageView>(R.id.star3_3)
            )

            tvTitle?.text = displayTitles[paksaId] ?: paksaId.replaceFirstChar { it.uppercaseChar() }

            paksaCards[paksaId] = Triple(level1Card, level2Card, level3Card)
            paksaStars[paksaId] = Triple(level1Stars, level2Stars, level3Stars)

            val unlocked = sharedPref.getInt("unlocked_$paksaId", 1)

            fun updateCard(cardView: MaterialCardView?, stars: List<ImageView>, level: Int) {
                cardView?.apply {
                    if (level <= unlocked) {
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

            updateCard(level1Card, level1Stars, 1)
            updateCard(level2Card, level2Stars, 2)
            updateCard(level3Card, level3Stars, 3)

            listOf(level1Card to 1, level2Card to 2, level3Card to 3).forEach { (cardView, level) ->
                cardView?.setOnClickListener {
                    if (cardView.isEnabled) startLevelActivity(paksaId, level)
                }
            }
        }

        levelResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult
            val paksaId = data.getStringExtra("paksa_id") ?: return@registerForActivityResult
            val levelCompleted = data.getIntExtra("level_completed", -1)
            if (levelCompleted <= 0) return@registerForActivityResult

            val currentlyUnlocked = sharedPref.getInt("unlocked_$paksaId", 1)
            if (levelCompleted == currentlyUnlocked) {
                val nextLevel = levelCompleted + 1
                sharedPref.edit { putInt("unlocked_$paksaId", nextLevel) }

                paksaCards[paksaId]?.apply {
                    paksaStars[paksaId]?.let { starsTriple ->
                        fun unlockCard(cardView: MaterialCardView?, stars: List<ImageView>, level: Int) {
                            cardView?.apply {
                                if (level == nextLevel) {
                                    isClickable = true
                                    isEnabled = true
                                    alpha = 1f
                                    stars.forEach { it.setImageResource(R.drawable.starshade_icon) }
                                    val colorAnim = ValueAnimator.ofArgb(lockedColor, unlockedColor)
                                    colorAnim.duration = 500
                                    colorAnim.interpolator = DecelerateInterpolator()
                                    colorAnim.addUpdateListener { animator ->
                                        setCardBackgroundColor(animator.animatedValue as Int)
                                    }
                                    colorAnim.start()
                                }
                            }
                        }
                        unlockCard(second, starsTriple.second, 2)
                        unlockCard(third, starsTriple.third, 3)
                    }
                }
            }
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
