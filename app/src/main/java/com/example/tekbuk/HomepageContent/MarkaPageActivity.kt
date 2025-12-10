package com.example.tekbuk.HomepageContent

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.databinding.MarkaPageBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class MarkaPageActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var binding: MarkaPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = MarkaPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        loadAndDisplayScores()

        binding.sendButton.setOnClickListener {
            val userPrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            val studentName = userPrefs.getString("StudentName", "Guest User") ?: "Guest User"
            if (studentName == "Guest User" || studentName.isEmpty()) {
                Toast.makeText(this, "Please go to Settings and set your Name first.", Toast.LENGTH_LONG).show()
            } else {
                uploadAllScoresToFirebase(studentName, userPrefs.getString("StudentSection", "") ?: "")
            }
        }
    }

    private fun loadAndDisplayScores() {
        var anyScoreExists = false

        val hasPagtatayaScore = updatePagtatayaScore()
        if (hasPagtatayaScore) {
            anyScoreExists = true
        }

        val topics = mapOf(
            "TULA" to Triple(binding.textViewTulaLevel1Score, binding.textViewTulaLevel2Score, binding.textViewTulaLevel3Score),
            "SANAYSAY" to Triple(binding.textViewSanaysayLevel1Score, binding.textViewSanaysayLevel2Score, binding.textViewSanaysayLevel3Score),
            "DAGLI" to Triple(binding.textViewDagliLevel1Score, binding.textViewDagliLevel2Score, binding.textViewDagliLevel3Score),
            "TALUMPATI" to Triple(binding.textViewTalumpatiLevel1Score, binding.textViewTalumpatiLevel2Score, binding.textViewTalumpatiLevel3Score),
            "KWENTONG_BAYAN" to Triple(binding.textViewKwentongBayanLevel1Score, binding.textViewKwentongBayanLevel2Score, binding.textViewKwentongBayanLevel3Score)
        )

        val topicCards = mapOf(
            "TULA" to binding.cardTula,
            "SANAYSAY" to binding.cardSanaysay,
            "DAGLI" to binding.cardDagli,
            "TALUMPATI" to binding.cardTalumpati,
            "KWENTONG_BAYAN" to binding.cardKwentongBayan
        )

        for ((topicId, topicCard) in topicCards) {
            val hasScoreForThisTopic = updateScoresForTopic(topicId, topics[topicId]!!)
            if (hasScoreForThisTopic) {
                topicCard.visibility = View.VISIBLE
                anyScoreExists = true
            } else {
                topicCard.visibility = View.GONE
            }
        }

        if (anyScoreExists) {
            binding.scrollView.visibility = View.VISIBLE
            binding.trophyholder.visibility = View.GONE
        } else {
            binding.scrollView.visibility = View.GONE
            binding.trophyholder.visibility = View.VISIBLE
        }
    }

    private fun updatePagtatayaScore(): Boolean {
        val prefs = getSharedPreferences("PagtatayaState", Context.MODE_PRIVATE)
        val isFinished = prefs.getBoolean("QuizFinishedPermanently", false)

        return if (isFinished) {
            val score = prefs.getInt("FinalScore", 0)
            val totalItems = prefs.getInt("FinalTotalItems", 30)
            binding.textViewPagtatayaScore.text = "$score / $totalItems"
            binding.textViewPagtatayaScore.background = null
            binding.cardPagtataya.visibility = View.VISIBLE
            true
        } else {
            binding.cardPagtataya.visibility = View.GONE
            false
        }
    }

    private fun updateScoresForTopic(topic: String, scoreViews: Triple<TextView, TextView, TextView>): Boolean {
        val score1 = getScoreFor(topic, 1)
        val score2 = getScoreFor(topic, 2)
        val score3 = getScoreFor(topic, 3)

        updateScoreView(score1, scoreViews.first)
        updateScoreView(score2, scoreViews.second)
        updateScoreView(score3, scoreViews.third)

        return score1 >= 0 || score2 >= 0 || score3 >= 0
    }

    private fun updateScoreView(score: Int, scoreTextView: TextView) {
        if (score >= 0) {
            scoreTextView.text = score.toString()
            scoreTextView.background = null
        } else {
            scoreTextView.text = " "
        }
    }

    private fun getScoreFor(topic: String, level: Int): Int {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        return prefs.getInt("${topic}_LEVEL_${level}", -1)
    }

    private fun uploadAllScoresToFirebase(studentName: String, studentSection: String) {
        Toast.makeText(this, "Sending scores...", Toast.LENGTH_SHORT).show()

        val pagtatayaPrefs = getSharedPreferences("PagtatayaState", Context.MODE_PRIVATE)
        val pagtatayaScore = if (pagtatayaPrefs.getBoolean("QuizFinishedPermanently", false)) pagtatayaPrefs.getInt("FinalScore", 0) else 0

        val scoresMap = mutableMapOf<String, Any>(
            "studentName" to studentName,
            "section" to studentSection,
            "pagtataya_score" to pagtatayaScore,
            "last_updated" to Date()
        )

        var totalScore = pagtatayaScore

        val topics = listOf("TULA", "SANAYSAY", "DAGLI", "TALUMPATI", "KWENTONG_BAYAN")
        for (topic in topics) {
            for (level in 1..3) {
                val score = getScoreFor(topic, level)
                if (score >= 0) {
                    scoresMap["${topic.lowercase()}_l$level"] = score
                    totalScore += score
                }
            }
        }
        scoresMap["total_score"] = totalScore

        val documentId = "${studentName}_${studentSection}".replace(" ", "_")

        db.collection("quiz_results")
            .document(documentId)
            .set(scoresMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Scores sent to Teacher successfully!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
