package com.example.tekbuk.HomepageContent

import android.content.Context // Import Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R // Import R
import com.example.tekbuk.databinding.MarkaPageBinding
import com.google.android.material.card.MaterialCardView

class MarkaPageActivity : AppCompatActivity() {

    private lateinit var binding: MarkaPageBinding

    // ⭐ 1. Constants for Pagtataya SharedPreferences
    private val PAGTATAYA_PREFS_NAME = "PagtatayaState"
    private val KEY_PAGTATAYA_FINISHED = "QuizFinishedPermanently"
    private val KEY_PAGTATAYA_SCORE = "FinalScore"
    private val KEY_PAGTATAYA_TOTAL_ITEMS = "FinalTotalItems"

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = MarkaPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadAndDisplayScores()
    }

    /**
     * Main function to control the UI.
     * It checks all topics and Pagtataya, updates their scores, and decides which UI elements to show.
     */
    private fun loadAndDisplayScores() {
        // A flag to track if any score exists anywhere
        var anyScoreExists = false

        // ⭐ 2. Load and display the Pagtataya score first
        val hasPagtatayaScore = updatePagtatayaScore()
        if (hasPagtatayaScore) {
            anyScoreExists = true // Mark that a score was found
        }

        // --- Now, handle the level-based topics ---
        val topics = mapOf(
            "TULA" to binding.cardTula,
            "SANAYSAY" to binding.cardSanaysay,
            "DAGLI" to binding.cardDagli,
            "TALUMPATI" to binding.cardTalumpati,
            "KWENTONG_BAYAN" to binding.cardKwentongBayan
        )

        // Loop through each topic to update scores and check visibility
        for ((topicId, topicCard) in topics) {
            val hasScoreForThisTopic = updateScoresForTopic(topicId)

            if (hasScoreForThisTopic) {
                topicCard.visibility = View.VISIBLE
                anyScoreExists = true // Mark that a score was found
            } else {
                topicCard.visibility = View.GONE
            }
        }

        // ⭐ 3. Final UI visibility check
        // After checking all topics AND Pagtataya, decide which main view to show
        if (anyScoreExists) {
            binding.scrollView.visibility = View.VISIBLE
            binding.trophyholder.visibility = View.GONE
        } else {
            binding.scrollView.visibility = View.GONE
            binding.trophyholder.visibility = View.VISIBLE
        }
    }

    /**
     * ⭐ 4. NEW FUNCTION: Specifically for loading and displaying the Pagtataya score.
     * @return `true` if a score was found and displayed, `false` otherwise.
     */
    private fun updatePagtatayaScore(): Boolean {
        val prefs = getSharedPreferences(PAGTATAYA_PREFS_NAME, Context.MODE_PRIVATE)
        val isFinished = prefs.getBoolean(KEY_PAGTATAYA_FINISHED, false)

        if (isFinished) {
            val score = prefs.getInt(KEY_PAGTATAYA_SCORE, 0)
            val totalItems = prefs.getInt(KEY_PAGTATAYA_TOTAL_ITEMS, 30) // Default to 30 if not found

            // Update the TextView with the score
            binding.textViewPagtatayaScore.text = "$score / $totalItems"
            binding.textViewPagtatayaScore.background = null // Remove placeholder background

            // Make the card visible
            binding.cardPagtataya.visibility = View.VISIBLE
            return true // A score was found
        } else {
            // No score found, hide the card
            binding.cardPagtataya.visibility = View.GONE
            return false // No score was found
        }
    }

    /**
     * Updates all level scores for a single topic and returns if any score was found.
     * (This function remains unchanged)
     */
    private fun updateScoresForTopic(topic: String): Boolean {
        val score1 = getScoreFor(topic, 1)
        val score2 = getScoreFor(topic, 2)
        val score3 = getScoreFor(topic, 3)

        val scoreViews = when (topic) {
            "TULA" -> Triple(binding.textViewTulaLevel1Score, binding.textViewTulaLevel2Score, binding.textViewTulaLevel3Score)
            "SANAYSAY" -> Triple(binding.textViewSanaysayLevel1Score, binding.textViewSanaysayLevel2Score, binding.textViewSanaysayLevel3Score)
            "DAGLI" -> Triple(binding.textViewDagliLevel1Score, binding.textViewDagliLevel2Score, binding.textViewDagliLevel3Score)
            "TALUMPATI" -> Triple(binding.textViewTalumpatiLevel1Score, binding.textViewTalumpatiLevel2Score, binding.textViewTalumpatiLevel3Score)
            "KWENTONG_BAYAN" -> Triple(binding.textViewKwentongBayanLevel1Score, binding.textViewKwentongBayanLevel2Score, binding.textViewKwentongBayanLevel3Score)
            else -> null
        }

        scoreViews?.let {
            updateScoreView(score1, it.first)
            updateScoreView(score2, it.second)
            updateScoreView(score3, it.third)
        }

        return score1 >= 0 || score2 >= 0 || score3 >= 0
    }

    /**
     * Helper function to update a single TextView with its score.
     * (This function remains unchanged, but the 'score.toString()' logic is fine for single scores)
     */
    private fun updateScoreView(score: Int, scoreTextView: TextView) {
        if (score >= 0) {
            // For levels, we assume the score is out of 10, but just displaying the number is fine.
            scoreTextView.text = score.toString()
            scoreTextView.background = null
        } else {
            scoreTextView.text = " "
        }
    }

    /**
     * Retrieves a user's score for a specific level from SharedPreferences.
     * (This function remains unchanged)
     */
    private fun getScoreFor(topic: String, level: Int): Int {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        return prefs.getInt("${topic}_LEVEL_${level}", -1)
    }
}
