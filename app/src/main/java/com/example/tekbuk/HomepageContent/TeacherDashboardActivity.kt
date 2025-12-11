package com.example.tekbuk.HomepageContent

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.R
import com.google.firebase.firestore.FirebaseFirestore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.Query

class TeacherDashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private val studentList = ArrayList<StudentResult>()
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Views
        recyclerView = findViewById(R.id.rvStudentScores) // Ensure ID matches your XML
        progressBar = findViewById(R.id.progressBar) // Ensure ID matches your XML

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList) { selectedStudent ->
            showStudentDetailsDialog(selectedStudent)
        }
        recyclerView.adapter = adapter

        // Fetch Data
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        progressBar.visibility = View.VISIBLE
        studentList.clear()

        db.collection("quiz_results")
            .orderBy("last_updated", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val rawData = document.data
                    val name = document.getString("studentName") ?: "Unknown"
                    val section = document.getString("section") ?: "N/A"
                    val totalScore = document.getLong("total_score")?.toInt() ?: 0
                    val pagtataya = document.getLong("pagtataya_score")?.toInt() ?: 0

                    val student = StudentResult(
                        id = document.id,
                        studentName = name,
                        section = section,
                        total_score = totalScore,
                        pagtataya_score = pagtataya,
                        rawData = rawData
                    )
                    studentList.add(student)
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE

                if (studentList.isEmpty()) {
                    Toast.makeText(this, "No student records found yet.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showStudentDetailsDialog(student: StudentResult) {
        val builder = AlertDialog.Builder(this)
        // We will set a custom title view inside the layout, so we don't set title here to keep it cleaner

        // 1. Root ScrollView (To ensure it fits on screen)
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(resources.getColor(R.color.fourpointfive)) // App Background Color

        // 2. Main Container (LinearLayout)
        val mainContainer = android.widget.LinearLayout(this)
        mainContainer.orientation = android.widget.LinearLayout.VERTICAL
        mainContainer.setPadding(40, 40, 40, 40) // Add breathing room

        // --- HEADER SECTION (Name & Section) ---
        val tvName = TextView(this)
        tvName.text = student.studentName.uppercase()
        tvName.textSize = 20f
        tvName.setTypeface(null, android.graphics.Typeface.BOLD)
        tvName.setTextColor(resources.getColor(R.color.black))
        tvName.gravity = android.view.Gravity.CENTER
        mainContainer.addView(tvName)

        val tvSection = TextView(this)
        tvSection.text = student.section
        tvSection.textSize = 16f
        tvSection.setTextColor(resources.getColor(R.color.one)) // Accent Color
        tvSection.gravity = android.view.Gravity.CENTER
        tvSection.setPadding(0, 0, 0, 30) // Bottom margin
        mainContainer.addView(tvSection)

        // --- SCORE SUMMARY CARD ---
        val summaryCard = androidx.cardview.widget.CardView(this)
        summaryCard.radius = 30f
        summaryCard.cardElevation = 10f
        summaryCard.setCardBackgroundColor(resources.getColor(R.color.white))

        val summaryParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        summaryParams.setMargins(10, 10, 10, 40) // Margin bottom
        summaryCard.layoutParams = summaryParams

        val summaryLayout = android.widget.LinearLayout(this)
        summaryLayout.orientation = android.widget.LinearLayout.VERTICAL
        summaryLayout.setPadding(30, 30, 30, 30)

        val tvTotalLabel = TextView(this)
        tvTotalLabel.text = "OVERALL SCORE"
        tvTotalLabel.gravity = android.view.Gravity.CENTER
        tvTotalLabel.textSize = 12f
        summaryLayout.addView(tvTotalLabel)

        val tvTotalScore = TextView(this)
        tvTotalScore.text = "${student.total_score}"
        tvTotalScore.textSize = 32f
        tvTotalScore.setTypeface(null, android.graphics.Typeface.BOLD)
        tvTotalScore.setTextColor(resources.getColor(R.color.one))
        tvTotalScore.gravity = android.view.Gravity.CENTER
        summaryLayout.addView(tvTotalScore)

        val tvPagtataya = TextView(this)
        tvPagtataya.text = "Pagtataya: ${student.pagtataya_score} / 30"
        tvPagtataya.gravity = android.view.Gravity.CENTER
        tvPagtataya.textSize = 14f
        tvPagtataya.setPadding(0, 10, 0, 0)
        summaryLayout.addView(tvPagtataya)

        summaryCard.addView(summaryLayout)
        mainContainer.addView(summaryCard)


        // --- TOPICS LOOP ---
        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")

        for (topic in topics) {
            // Check if data exists for this topic to avoid empty boxes
            if (student.rawData.containsKey("${topic}_l1") || student.rawData.containsKey("${topic}_l3_answer")) {

                val l1 = (student.rawData["${topic}_l1"] as? Number)?.toInt() ?: 0
                val l2 = (student.rawData["${topic}_l2"] as? Number)?.toInt() ?: 0
                val l3Answer = student.rawData["${topic}_l3_answer"] as? String ?: "No Answer"
                val topicTotal = l1 + l2

                // Topic Header
                val tvTopicTitle = TextView(this)
                tvTopicTitle.text = "${topic.uppercase()} (Quiz: $topicTotal pts)"
                tvTopicTitle.textSize = 16f
                tvTopicTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                tvTopicTitle.setTextColor(resources.getColor(R.color.black))
                tvTopicTitle.setPadding(0, 20, 0, 10)
                mainContainer.addView(tvTopicTitle)

                // Scores Row
                val tvScores = TextView(this)
                tvScores.text = "Level 1: $l1  |  Level 2: $l2"
                tvScores.textSize = 14f
                tvScores.setTextColor(resources.getColor(R.color.one))
                tvScores.setPadding(0, 0, 0, 10)
                mainContainer.addView(tvScores)

                // Essay Box (Styled visually)
                val essayContainer = android.widget.LinearLayout(this)
                essayContainer.orientation = android.widget.LinearLayout.VERTICAL
                essayContainer.setBackgroundResource(R.drawable.edittext_bg) // Reusing your existing drawable if available, or use color
                // If edittext_bg doesn't exist, use: essayContainer.setBackgroundColor(android.graphics.Color.WHITE)
                essayContainer.setPadding(25, 25, 25, 25)

                // Add margins to the essay box
                val essayParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                essayParams.setMargins(0, 0, 0, 30) // Spacing after topic
                essayContainer.layoutParams = essayParams

                val tvEssayLabel = TextView(this)
                tvEssayLabel.text = "Repleksyon / Essay:"
                tvEssayLabel.textSize = 12f
                tvEssayLabel.setTypeface(null, android.graphics.Typeface.ITALIC)
                essayContainer.addView(tvEssayLabel)

                // --- FIX FOR WRAPPING TEXT (Inside Loop) ---
                val tvEssayBody = TextView(this)
                tvEssayBody.text = l3Answer
                tvEssayBody.setTextColor(resources.getColor(R.color.black))
                tvEssayBody.textSize = 14f
                tvEssayBody.setPadding(0, 10, 0, 0)

                // 1. Allow multiple lines
                tvEssayBody.isSingleLine = false
                // 2. Set InputType to Text + MultiLine
                tvEssayBody.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                // 3. Set layout params to WRAP_CONTENT
                tvEssayBody.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )

                essayContainer.addView(tvEssayBody)
                mainContainer.addView(essayContainer)
            }
        }

        // --- MAIN REPLEKSYON SECTION ---
        val mainRep = student.rawData["repleksyon_main_answer"] as? String ?: "Not Submitted"

        val tvMainRepTitle = TextView(this)
        tvMainRepTitle.text = "MAIN REPLEKSYON"
        tvMainRepTitle.textSize = 18f
        tvMainRepTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        tvMainRepTitle.setTextColor(resources.getColor(R.color.one))
        tvMainRepTitle.setPadding(0, 20, 0, 10)
        mainContainer.addView(tvMainRepTitle)

        val repContainer = android.widget.LinearLayout(this)
        repContainer.orientation = android.widget.LinearLayout.VERTICAL
        repContainer.setBackgroundColor(resources.getColor(R.color.white)) // Or R.drawable.edittext_bg
        repContainer.setPadding(30, 30, 30, 30)

        val repParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        repParams.setMargins(0, 0, 0, 20)
        repContainer.layoutParams = repParams

        // --- FIX FOR WRAPPING TEXT ---
        val tvRepBody = TextView(this)
        tvRepBody.text = mainRep
        tvRepBody.textSize = 15f
        tvRepBody.setTextColor(resources.getColor(R.color.black))
        // 1. Force SingleLine to FALSE
        tvRepBody.isSingleLine = false
        // 2. Set InputType to MultiLine
        tvRepBody.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        // 3. Ensure layout params assume wrapping
        tvRepBody.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )

        repContainer.addView(tvRepBody)
        mainContainer.addView(repContainer)

        // Add Main Container to ScrollView
        scrollView.addView(mainContainer)

        // Set View and Buttons
        builder.setView(scrollView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        // Optional: Customize Button Color
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.one))
        }
        dialog.show()
    }

}
data class StudentResult(
    val id: String = "",
    val studentName: String = "",
    val section: String = "",
    val total_score: Int = 0,
    val pagtataya_score: Int = 0,
    // We store all raw data map to access specific topic scores/reflections easily later
    val rawData: Map<String, Any> = emptyMap()
)
