package com.example.tekbuk.HomepageContent

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.databinding.MarkaPageBinding
import com.google.android.material.card.MaterialCardView

class MarkaPageActivity : AppCompatActivity() {

    private lateinit var binding: MarkaPageBinding

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
     * It checks all topics, updates their scores, and decides which UI elements to show or hide.
     */
    private fun loadAndDisplayScores() {
        val topics = mapOf(
            "TULA" to binding.cardTula,
            "SANAYSAY" to binding.cardSanaysay,
            "DAGLI" to binding.cardDagli,
            "TALUMPATI" to binding.cardTalumpati,
            "KWENTONG_BAYAN" to binding.cardKwentongBayan
        )

        var anyScoreExists = false

        // Loop through each topic to update scores and check visibility
        for ((topicId, topicCard) in topics) {
            val hasScoreForThisTopic = updateScoresForTopic(topicId)

            // If this topic has at least one score, make its card visible
            if (hasScoreForThisTopic) {
                topicCard.visibility = View.VISIBLE
                anyScoreExists = true // Mark that we found at least one score overall
            } else {
                topicCard.visibility = View.GONE // Hide the card if no scores for this topic
            }
        }

        // After checking all topics, decide which main view to show
        if (anyScoreExists) {
            // If there's at least one score, show the ScrollView with the score cards...
            binding.scrollView.visibility = View.VISIBLE
            // ...and hide the "Walang Pagsusulit Pa" placeholder.
            binding.trophyholder.visibility = View.GONE
        } else {
            // If no scores were found at all, show the placeholder...
            binding.scrollView.visibility = View.GONE
            // ...and hide the ScrollView.
            binding.trophyholder.visibility = View.VISIBLE
        }
    }

    /**
     * Updates all level scores for a single topic and returns if any score was found.
     * @param topic The topic ID (e.g., "TULA", "KWENTONG_BAYAN").
     * @return `true` if at least one level in this topic has a score, `false` otherwise.
     */
    private fun updateScoresForTopic(topic: String): Boolean {
        // Get the scores for all 3 levels. getScoreFor returns -1 if not found.
        val score1 = getScoreFor(topic, 1)
        val score2 = getScoreFor(topic, 2)
        val score3 = getScoreFor(topic, 3)

        // Find the correct TextViews based on the topic
        val scoreViews = when (topic) {
            "TULA" -> Triple(binding.textViewTulaLevel1Score, binding.textViewTulaLevel2Score, binding.textViewTulaLevel3Score)
            "SANAYSAY" -> Triple(binding.textViewSanaysayLevel1Score, binding.textViewSanaysayLevel2Score, binding.textViewSanaysayLevel3Score)
            "DAGLI" -> Triple(binding.textViewDagliLevel1Score, binding.textViewDagliLevel2Score, binding.textViewDagliLevel3Score)
            "TALUMPATI" -> Triple(binding.textViewTalumpatiLevel1Score, binding.textViewTalumpatiLevel2Score, binding.textViewTalumpatiLevel3Score)
            "KWENTONG_BAYAN" -> Triple(binding.textViewKwentongBayanLevel1Score, binding.textViewKwentongBayanLevel2Score, binding.textViewKwentongBayanLevel3Score)
            else -> null
        }

        // Update the UI for each level
        scoreViews?.let {
            updateScoreView(score1, it.first)
            updateScoreView(score2, it.second)
            updateScoreView(score3, it.third)
        }

        // Return true if any of the scores are valid (>= 0)
        return score1 >= 0 || score2 >= 0 || score3 >= 0
    }

    /**
     * Helper function to update a single TextView with its score.
     * If the score is invalid (-1), it keeps the placeholder text.
     */
    private fun updateScoreView(score: Int, scoreTextView: TextView) {
        if (score >= 0) {
            scoreTextView.text = score.toString()
            scoreTextView.background = null // Remove the gray placeholder background
        } else {
            // If no score, keep the placeholder. Your XML already handles the style.
            scoreTextView.text = " "
        }
    }

    /**
     * Retrieves a user's score from SharedPreferences.
     * @return The integer score, or -1 if no score is found.
     */
    private fun getScoreFor(topic: String, level: Int): Int {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        return prefs.getInt("${topic}_LEVEL_${level}", -1)
    }
}
