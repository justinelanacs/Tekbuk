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
        builder.setTitle("${student.studentName} (${student.section})")

        // Construct the detailed message manually
        val sb = StringBuilder()
        sb.append("Total Score: ${student.total_score}\n")
        sb.append("Pagtataya: ${student.pagtataya_score}/30\n\n")

        sb.append("--- MGA PAKSA ---\n")
        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")

        for (topic in topics) {
            // 1. Get scores safely (Cast to Number first to handle Long/Int from Firestore)
            val l1 = (student.rawData["${topic}_l1"] as? Number)?.toInt() ?: 0
            val l2 = (student.rawData["${topic}_l2"] as? Number)?.toInt() ?: 0
            val l3Answer = student.rawData["${topic}_l3_answer"] as? String ?: "No Answer"

            // 2. Calculate the Total for this specific Paksa
            val topicTotal = l1 + l2

            // 3. Display with the calculated total
            sb.append("${topic.uppercase()} (Total: $topicTotal pts):\n")
            sb.append("  Level 1: $l1\n")
            sb.append("  Level 2: $l2\n")
            sb.append("  Repleksyon: \"$l3Answer\"\n\n")
        }

        sb.append("--- MAIN REPLEKSYON ---\n")
        val mainRep = student.rawData["repleksyon_main_answer"] as? String ?: "Not Submitted"
        sb.append(mainRep)

        // Show inside a scrollable view
        val scrollView = ScrollView(this)
        val textView = TextView(this)
        textView.text = sb.toString()
        textView.setPadding(50, 40, 50, 40)
        textView.textSize = 16f
        textView.setTextColor(resources.getColor(android.R.color.black))
        scrollView.addView(textView)

        builder.setView(scrollView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        builder.show()
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
