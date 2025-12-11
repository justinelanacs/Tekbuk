package com.example.tekbuk.HomepageContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class TeacherDashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var sectionSpinner: Spinner
    private lateinit var db: FirebaseFirestore
    private lateinit var toolbar: MaterialToolbar
    private lateinit var auth: FirebaseAuth

    private val allStudentsList = ArrayList<StudentResult>()
    private val filteredStudentList = ArrayList<StudentResult>()
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // --- ⭐ FIX: The order of these calls is critical ---

        // 1. Call super.onCreate() first. This is a requirement.
        super.onCreate(savedInstanceState)

        // This is a modern way to handle edge-to-edge display. It's good to have it early.
        enableEdgeToEdge()

        // 2. SET THE CONTENT VIEW. The activity now knows about R.id.main and all other views.
        setContentView(R.layout.activity_teacher_dashboard)

        // 3. NOW you can safely find views and set listeners on them.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // --- End of Fix ---

        // The rest of your initialization code will now work without crashing.
        recyclerView = findViewById(R.id.rvStudentScores)
        progressBar = findViewById(R.id.progressBar)
        sectionSpinner = findViewById(R.id.sectionSpinner)
        toolbar = findViewById(R.id.toolbar)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // --- Set up Toolbar menu click listener for Logout ---
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    signOutAndReturn()
                    true // Indicate that the event was handled
                }
                else -> false
            }
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(filteredStudentList) { selectedStudent ->
            showStudentDetailsDialog(selectedStudent)
        }
        recyclerView.adapter = adapter

        // Fetch Data
        fetchDataFromFirebase()
    }

    private fun signOutAndReturn() {
        auth.signOut()
        val prefs = getSharedPreferences("TeacherSession", Context.MODE_PRIVATE)
        prefs.edit().remove("LastLoginTime").apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    // ... (The rest of your code is correct and does not need to be changed) ...

    private fun fetchDataFromFirebase() {
        progressBar.visibility = View.VISIBLE
        allStudentsList.clear()

        db.collection("quiz_results")
            .orderBy("studentName", Query.Direction.ASCENDING)
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
                    allStudentsList.add(student)
                }
                setupSectionFilter()
                progressBar.visibility = View.GONE

                if (allStudentsList.isEmpty()) {
                    Toast.makeText(this, "No student records found yet.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupSectionFilter() {
        val sections = allStudentsList.map { it.section }.distinct().sorted()
        val spinnerItems = mutableListOf("All Sections").apply { addAll(sections) }
        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_black,
            spinnerItems
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sectionSpinner.adapter = spinnerAdapter

        sectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSection = spinnerItems[position]
                filterStudentList(selectedSection)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        filterStudentList("All Sections")
    }

    private fun filterStudentList(section: String) {
        filteredStudentList.clear()

        if (section == "All Sections") {
            filteredStudentList.addAll(allStudentsList)
        } else {
            val studentsFromSection = allStudentsList.filter { it.section == section }
            filteredStudentList.addAll(studentsFromSection)
        }
        adapter.notifyDataSetChanged()
    }

    private fun showStudentDetailsDialog(student: StudentResult) {
        val builder = AlertDialog.Builder(this)
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(resources.getColor(R.color.fourpointfive, theme))

        val mainContainer = android.widget.LinearLayout(this)
        mainContainer.orientation = android.widget.LinearLayout.VERTICAL
        mainContainer.setPadding(40, 40, 40, 40)

        val tvName = TextView(this)
        tvName.text = student.studentName.uppercase()
        tvName.textSize = 20f
        tvName.setTypeface(null, android.graphics.Typeface.BOLD)
        tvName.setTextColor(resources.getColor(R.color.black, theme))
        tvName.gravity = android.view.Gravity.CENTER
        mainContainer.addView(tvName)

        val tvSection = TextView(this)
        tvSection.text = student.section
        tvSection.textSize = 16f
        tvSection.setTextColor(resources.getColor(R.color.one, theme))
        tvSection.gravity = android.view.Gravity.CENTER
        tvSection.setPadding(0, 0, 0, 30)
        mainContainer.addView(tvSection)

        val summaryCard = androidx.cardview.widget.CardView(this)
        summaryCard.radius = 30f
        summaryCard.cardElevation = 10f
        summaryCard.setCardBackgroundColor(resources.getColor(R.color.white, theme))

        val summaryParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        summaryParams.setMargins(10, 10, 10, 40)
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
        tvTotalScore.setTextColor(resources.getColor(R.color.one, theme))
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


        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")

        for (topic in topics) {
            if (student.rawData.containsKey("${topic}_l1") || student.rawData.containsKey("${topic}_l3_answer")) {

                val l1 = (student.rawData["${topic}_l1"] as? Number)?.toInt() ?: 0
                val l2 = (student.rawData["${topic}_l2"] as? Number)?.toInt() ?: 0
                val l3Answer = student.rawData["${topic}_l3_answer"] as? String ?: "No Answer"
                val topicTotal = l1 + l2

                val tvTopicTitle = TextView(this)
                tvTopicTitle.text = "${topic.uppercase()} (Quiz: $topicTotal pts)"
                tvTopicTitle.textSize = 16f
                tvTopicTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                tvTopicTitle.setTextColor(resources.getColor(R.color.black, theme))
                tvTopicTitle.setPadding(0, 20, 0, 10)
                mainContainer.addView(tvTopicTitle)

                val tvScores = TextView(this)
                tvScores.text = "Level 1: $l1  |  Level 2: $l2"
                tvScores.textSize = 14f
                tvScores.setTextColor(resources.getColor(R.color.one, theme))
                tvScores.setPadding(0, 0, 0, 10)
                mainContainer.addView(tvScores)

                val essayContainer = android.widget.LinearLayout(this)
                essayContainer.orientation = android.widget.LinearLayout.VERTICAL
                essayContainer.setBackgroundResource(R.drawable.edittext_bg)
                essayContainer.setPadding(25, 25, 25, 25)

                val essayParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                essayParams.setMargins(0, 0, 0, 30)
                essayContainer.layoutParams = essayParams

                val tvEssayLabel = TextView(this)
                tvEssayLabel.text = "Repleksyon / Essay:"
                tvEssayLabel.textSize = 12f
                tvEssayLabel.setTypeface(null, android.graphics.Typeface.ITALIC)
                essayContainer.addView(tvEssayLabel)

                val tvEssayBody = TextView(this)
                tvEssayBody.text = l3Answer
                tvEssayBody.setTextColor(resources.getColor(R.color.black, theme))
                tvEssayBody.textSize = 14f
                tvEssayBody.setPadding(0, 10, 0, 0)
                tvEssayBody.isSingleLine = false
                tvEssayBody.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                tvEssayBody.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                essayContainer.addView(tvEssayBody)
                mainContainer.addView(essayContainer)
            }
        }

        val mainRep = student.rawData["repleksyon_main_answer"] as? String ?: "Not Submitted"

        val tvMainRepTitle = TextView(this)
        tvMainRepTitle.text = "MAIN REPLEKSYON"
        tvMainRepTitle.textSize = 18f
        tvMainRepTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        tvMainRepTitle.setTextColor(resources.getColor(R.color.one, theme))
        tvMainRepTitle.setPadding(0, 20, 0, 10)
        mainContainer.addView(tvMainRepTitle)

        val repContainer = android.widget.LinearLayout(this)
        repContainer.orientation = android.widget.LinearLayout.VERTICAL
        repContainer.setBackgroundColor(resources.getColor(R.color.white, theme))
        repContainer.setPadding(30, 30, 30, 30)
        val repParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        repParams.setMargins(0, 0, 0, 20)
        repContainer.layoutParams = repParams

        val tvRepBody = TextView(this)
        tvRepBody.text = mainRep
        tvRepBody.textSize = 15f
        tvRepBody.setTextColor(resources.getColor(R.color.black, theme))
        tvRepBody.isSingleLine = false
        tvRepBody.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        tvRepBody.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        repContainer.addView(tvRepBody)
        mainContainer.addView(repContainer)
        scrollView.addView(mainContainer)

        builder.setView(scrollView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.one, theme))
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
    val rawData: Map<String, Any> = emptyMap()
)
