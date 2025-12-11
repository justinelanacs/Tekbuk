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
            // Default to empty strings to force the validation check
            val studentName = userPrefs.getString("StudentName", "")?.trim() ?: ""
            val studentSection = userPrefs.getString("StudentSection", "")?.trim() ?: ""

            // Strict check: User must have set a real name, not "Guest User" or empty
            if (studentName.isEmpty() || studentName == "Guest User" || studentName == "STUDENT NAME") {
                Toast.makeText(this, "Please go to Settings and enter your Name and Section properly.", Toast.LENGTH_LONG).show()
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
        // Logic for Main Repleksyon Card
        val repleksyonAnswer = getReflectionText("REPLEKSYON_MAIN")
        if (repleksyonAnswer.isNotEmpty()) {
            binding.cardRepleksyon.visibility = View.VISIBLE
            binding.textViewRepleksyonStatus.text = "Submitted"
            anyScoreExists = true
        } else {
            binding.cardRepleksyon.visibility = View.GONE
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

        // Check if Level 3 (Essay) has text
        val answer3 = getReflectionText(topic)


        updateScoreView(score1, scoreViews.first)
        updateScoreView(score2, scoreViews.second)

        // For Level 3, we don't show a score number, we show status
        if (answer3.isNotEmpty()) {
            scoreViews.third.text = "Submitted"
            scoreViews.third.setTextColor(resources.getColor(com.example.tekbuk.R.color.one))
        } else {
            scoreViews.third.text = "Pending"
        }
        return score1 >= 0 || score2 >= 0 || answer3.isNotEmpty()
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
    // NEW Helper to get String answers (Essays)
    private fun getReflectionText(topic: String): String {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        // Expected Keys: "TULA_LEVEL_3_ANSWER" or "REPLEKSYON_MAIN_ANSWER"
        val suffix = if(topic == "REPLEKSYON_MAIN") "_ANSWER" else "_LEVEL_3_ANSWER"
        return prefs.getString("${topic}${suffix}", "") ?: ""
    }

    private fun uploadAllScoresToFirebase(studentName: String, studentSection: String) {
        Toast.makeText(this, "Sending scores...", Toast.LENGTH_SHORT).show()

        // 1. Get Pagtataya Score
        val pagtatayaPrefs = getSharedPreferences("PagtatayaState", Context.MODE_PRIVATE)
        val pagtatayaScore = if (pagtatayaPrefs.getBoolean("QuizFinishedPermanently", false)) {
            pagtatayaPrefs.getInt("FinalScore", 0)
        } else {
            0
        }

        // 2. Prepare the Data Map
        // We ensure 'studentName' and 'section' are at the top level
        val scoresMap = mutableMapOf<String, Any>(
            "studentName" to studentName,
            "section" to if (studentSection.isNotEmpty()) studentSection else "No Section",
            "pagtataya_score" to pagtatayaScore,
            "last_updated" to Date()
        )

        var totalScore = pagtatayaScore

        // 3. Loop through topics to get numeric scores (Level 1 & 2)
        val topics = listOf("TULA", "SANAYSAY", "DAGLI", "TALUMPATI", "KWENTONG_BAYAN")
        for (topic in topics) {
            // Level 1 (Quiz)
            val score1 = getScoreFor(topic, 1)
            if (score1 >= 0) {
                scoresMap["${topic.lowercase()}_l1"] = score1
                totalScore += score1
            }

            // Level 2 (Quiz)
            val score2 = getScoreFor(topic, 2)
            if (score2 >= 0) {
                scoresMap["${topic.lowercase()}_l2"] = score2
                totalScore += score2
            }

            // Level 3 (TEXT ANSWER for Teacher Evaluation)
            val answer3 = getReflectionText(topic)
            if (answer3.isNotEmpty()) {
                scoresMap["${topic.lowercase()}_l3_answer"] = answer3
                scoresMap["${topic.lowercase()}_l3_status"] = "Submitted"
            } else {
                scoresMap["${topic.lowercase()}_l3_status"] = "Pending"
            }
        }

        // 4. Main Repleksyon (Text Answer)
        val mainRepleksyonAnswer = getReflectionText("REPLEKSYON_MAIN")
        if (mainRepleksyonAnswer.isNotEmpty()) {
            scoresMap["repleksyon_main_answer"] = mainRepleksyonAnswer
            scoresMap["repleksyon_status"] = "Submitted"
        } else {
            scoresMap["repleksyon_status"] = "Pending"
        }

        // Add the total score to the map
        scoresMap["total_score"] = totalScore

        // 5. Upload to Firebase
        // Create a unique ID combining Name and Section to prevent duplicates
        val documentId = "${studentName}_${studentSection}".replace(" ", "_").uppercase()

        db.collection("quiz_results")
            .document(documentId)
            .set(scoresMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Scores sent to Teacher successfully!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()

        scoresMap["total_score"] = totalScore

        // 5. Upload to Firebase
        // Create a unique ID combining Name and Section to prevent duplicates
        val documentId = "${studentName}_${studentSection}".replace(" ", "_").uppercase()

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
}
