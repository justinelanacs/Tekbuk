package com.example.tekbuk.HomepageContent

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R
import com.example.tekbuk.databinding.MarkaPageBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

        // ⭐ 1. Check for new grades from the teacher first.
        checkForTeacherGrades()

        binding.sendButton.setOnClickListener {
            val userPrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            val studentName = userPrefs.getString("StudentName", "")?.trim() ?: ""
            val studentSection = userPrefs.getString("StudentSection", "")?.trim() ?: ""

            if (studentName.isEmpty() || studentName == "Guest User" || studentName == "STUDENT NAME") {
                Toast.makeText(this, "Please go to Settings and enter your Name and Section properly.", Toast.LENGTH_LONG).show()
            } else {
                uploadAllScoresToFirebase(studentName, userPrefs.getString("StudentSection", "") ?: "")
            }
        }
    }

    private fun loadAndDisplayScores() {
        val userPrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val studentName = userPrefs.getString("StudentName", "STUDENT NAME")
        val studentSection = userPrefs.getString("StudentSection", "SECTION")

        var anyScoreExists = false

        if (updatePagtatayaScore()) anyScoreExists = true

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
            if (updateScoresForTopic(topicId, topics[topicId]!!)) {
                topicCard.visibility = View.VISIBLE
                anyScoreExists = true
            } else {
                topicCard.visibility = View.GONE
            }
        }

        val repleksyonAnswer = getReflectionText("REPLEKSYON_MAIN")
        val mainRepScore = getSharedPreferences("UserScores", MODE_PRIVATE).getInt("REPLEKSYON_MAIN_SCORE", 0)

        if (repleksyonAnswer.isNotEmpty()) {
            binding.cardRepleksyon.visibility = View.VISIBLE
            anyScoreExists = true

            if (mainRepScore > 0) {
                binding.textViewRepleksyonStatus.text = "Total: $mainRepScore/30"
                binding.textViewRepleksyonStatus.setTextColor(resources.getColor(R.color.one))
                binding.textViewRepleksyonStatus.setTypeface(null, Typeface.BOLD)
            } else {
                binding.textViewRepleksyonStatus.text = "Ipinasa (Naghihintay ng marka)"
                binding.textViewRepleksyonStatus.setTypeface(null, Typeface.ITALIC)
            }
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
            binding.textViewPagtatayaScore.text = "Total: $score/$totalItems"
            binding.textViewPagtatayaScore.setTextColor(resources.getColor(R.color.one))
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
        val answer3 = getReflectionText(topic)

        updateScoreView(score1, scoreViews.first)
        updateScoreView(score2, scoreViews.second)

        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        val score3 = prefs.getInt("${topic}_LEVEL_3_SCORE", 0)

        if (score3 > 0) {
            scoreViews.third.text = "$score3 pts"
            scoreViews.third.setTextColor(resources.getColor(R.color.one))
            scoreViews.third.setTypeface(null, Typeface.BOLD)
        } else if (answer3.isNotEmpty()) {
            scoreViews.third.text = "Ipinasa (Naghihintay ng marka)"
            scoreViews.third.setTextColor(resources.getColor(android.R.color.darker_gray))
            scoreViews.third.setTypeface(null, Typeface.ITALIC)
        } else {
            scoreViews.third.text = "Hindi pa tapos"
        }

        return score1 >= 0 || score2 >= 0 || answer3.isNotEmpty()
    }

    private fun updateScoreView(score: Int, scoreTextView: TextView) {
        if (score >= 0) {
            scoreTextView.text = "$score pts"
            scoreTextView.setTextColor(resources.getColor(R.color.one))
        } else {
            scoreTextView.text = "N/A"
            scoreTextView.setTextColor(resources.getColor(R.color.one))
        }
    }

    private fun getScoreFor(topic: String, level: Int): Int {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        return prefs.getInt("${topic}_LEVEL_${level}", -1)
    }

    private fun getReflectionText(topic: String): String {
        val prefs = getSharedPreferences("UserScores", MODE_PRIVATE)
        val suffix = if (topic == "REPLEKSYON_MAIN") "_ANSWER" else "_LEVEL_3_ANSWER"
        return prefs.getString("${topic}${suffix}", "") ?: ""
    }

    private fun uploadAllScoresToFirebase(studentName: String, studentSection: String) {
        Toast.makeText(this, "Sending scores...", Toast.LENGTH_SHORT).show()

        val pagtatayaPrefs = getSharedPreferences("PagtatayaState", Context.MODE_PRIVATE)
        val pagtatayaScore = if (pagtatayaPrefs.getBoolean("QuizFinishedPermanently", false)) pagtatayaPrefs.getInt("FinalScore", 0) else 0

        val scoresMap = mutableMapOf<String, Any>(
            "studentName" to studentName,
            "section" to if (studentSection.isNotEmpty()) studentSection else "No Section",
            "pagtataya_score" to pagtatayaScore,
            "last_updated" to Date()
        )

        var totalScore = pagtatayaScore

        val topics = listOf("TULA", "SANAYSAY", "DAGLI", "TALUMPATI", "KWENTONG_BAYAN")
        for (topic in topics) {
            val score1 = getScoreFor(topic, 1)
            if (score1 >= 0) {
                scoresMap["${topic.lowercase()}_l1"] = score1
                totalScore += score1
            }

            val score2 = getScoreFor(topic, 2)
            if (score2 >= 0) {
                scoresMap["${topic.lowercase()}_l2"] = score2
                totalScore += score2
            }

            val answer3 = getReflectionText(topic)
            if (answer3.isNotEmpty()) {
                scoresMap["${topic.lowercase()}_l3_answer"] = answer3
            }
        }

        val mainRepleksyonAnswer = getReflectionText("REPLEKSYON_MAIN")
        if (mainRepleksyonAnswer.isNotEmpty()) {
            scoresMap["repleksyon_main_answer"] = mainRepleksyonAnswer
        }

        scoresMap["total_score"] = totalScore

        val documentId = "${studentName}_${studentSection}".replace(" ", "_").uppercase()

        db.collection("quiz_results")
            .document(documentId)
            .set(scoresMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Scores sent to Teacher successfully!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkForTeacherGrades() {
        val userPrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = userPrefs.getString("StudentName", "") ?: ""
        val section = userPrefs.getString("StudentSection", "") ?: ""

        if (name.isNotEmpty() && name != "STUDENT NAME") {
            val docId = "${name}_${section}".replace(" ", "_").uppercase()

            db.collection("quiz_results").document(docId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val scoresPrefsEditor = getSharedPreferences("UserScores", Context.MODE_PRIVATE).edit()
                        var wereGradesUpdated = false

                        val topics = listOf("TULA", "SANAYSAY", "DAGLI", "TALUMPATI", "KWENTONG_BAYAN")
                        for (topic in topics) {
                            val scoreKey = "${topic.lowercase()}_l3_score"
                            val score = document.getLong(scoreKey)?.toInt()
                            if (score != null && score > 0) {
                                scoresPrefsEditor.putInt("${topic}_LEVEL_3_SCORE", score)
                                wereGradesUpdated = true
                            }
                        }

                        val mainScore = document.getLong("repleksyon_main_score")?.toInt()
                        if (mainScore != null && mainScore > 0) {
                            scoresPrefsEditor.putInt("REPLEKSYON_MAIN_SCORE", mainScore)
                            wereGradesUpdated = true
                        }

                        scoresPrefsEditor.apply()

                        if (wereGradesUpdated) {
                            loadAndDisplayScores()
                            Toast.makeText(this, "Your new grades from the teacher are here!", Toast.LENGTH_LONG).show()
                        } else {
                            loadAndDisplayScores() // Load scores even if no new grades
                        }
                    } else {
                         loadAndDisplayScores() // Still load local scores if no Firestore doc exists
                    }
                }
                .addOnFailureListener {
                     loadAndDisplayScores() // Load local scores on failure too
                }
        } else {
             loadAndDisplayScores() // Load local scores if no user profile is set
        }
    }

    private fun updateRepleksyonStatus() {
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val isSubmitted = prefs.getBoolean("REPLEKSYON_SUBMITTED", false)
        val cardRepleksyon = findViewById<MaterialCardView>(R.id.cardRepleksyon)
        val statusTextView = findViewById<TextView>(R.id.textViewRepleksyonStatus)

        // Make the entire card visible
        cardRepleksyon.visibility = View.VISIBLE

        if (isSubmitted) {
            statusTextView.text = "Naipasa na"
            // ⭐ FIX: Set the text color to white when submitted ⭐
            statusTextView.setTextColor(resources.getColor(R.color.white, theme))
        } else {
            statusTextView.text = "Hindi pa naipapasa"
            // ⭐ FIX: Set the text color back to your default (e.g., black) when not submitted ⭐
            statusTextView.setTextColor(resources.getColor(R.color.black, theme))
        }
    }
}
