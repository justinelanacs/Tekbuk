package com.example.tekbuk.HomepageContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvStudentScores)
        progressBar = findViewById(R.id.progressBar)
        sectionSpinner = findViewById(R.id.sectionSpinner)
        toolbar = findViewById(R.id.toolbar)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    signOutAndReturn()
                    true
                }
                else -> false
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(filteredStudentList) { selectedStudent ->
            showStudentDetailsDialog(selectedStudent)
        }
        recyclerView.adapter = adapter

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

    private fun fetchDataFromFirebase() {
        progressBar.visibility = View.VISIBLE
        allStudentsList.clear()

        db.collection("quiz_results")
            .orderBy("studentName", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val student = document.toObject(StudentResult::class.java).copy(id = document.id, rawData = document.data)
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
                filterStudentList(spinnerItems[position])
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
            filteredStudentList.addAll(allStudentsList.filter { it.section == section })
        }
        adapter.notifyDataSetChanged()
    }

    private fun showStudentDetailsDialog(student: StudentResult) {
        val builder = AlertDialog.Builder(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER
        }

        mainContainer.addView(TextView(this).apply {
            text = student.studentName.uppercase()
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        mainContainer.addView(TextView(this).apply {
            text = student.section
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        })

        val scoresButton = Button(this).apply{
            text = "View Scores"
        }
        scoresButton.setOnClickListener{
            showScoresDialog(student)
        }

        val reflectionsButton = Button(this).apply{
            text = "Grade Reflections"
        }
        reflectionsButton.setOnClickListener{
            showGradingDialog(student)
        }

        mainContainer.addView(scoresButton)
        mainContainer.addView(reflectionsButton)

        builder.setView(mainContainer)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showScoresDialog(student: StudentResult) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Scores for ${student.studentName}")

        val scrollView = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        mainContainer.addView(createScoreView("Pagtataya", "${student.pagtataya_score} / 30"))

        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")
        for (topic in topics) {
            val l1 = (student.rawData["${topic}_l1"] as? Number)?.toInt() ?: -1
            val l2 = (student.rawData["${topic}_l2"] as? Number)?.toInt() ?: -1
            if (l1 >= 0) {
                 mainContainer.addView(createScoreView("${topic.uppercase()} Level 1", "${l1.takeIf { it >= 0 } ?: "N/A"}"))
            }
            if (l2 >= 0) {
                 mainContainer.addView(createScoreView("${topic.uppercase()} Level 2", "${l2.takeIf { it >= 0 } ?: "N/A"}"))
            }
        }

        scrollView.addView(mainContainer)
        builder.setView(scrollView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showGradingDialog(student: StudentResult) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Grade Reflections for ${student.studentName}")

        val scrollView = ScrollView(this)
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val gradeInputs = mutableMapOf<String, EditText>()

        val mainRepAnswer = student.rawData["repleksyon_main_answer"] as? String
        if (!mainRepAnswer.isNullOrEmpty()) {
            val currentScore = (student.rawData["repleksyon_main_score"] as? Number)?.toInt() ?: 0
            val essayView = createEssayGradingView("PANGKALAHATANG REPLEKSYON", mainRepAnswer, currentScore)
            mainContainer.addView(essayView)
            gradeInputs["repleksyon_main_score"] = essayView.findViewWithTag("input")
        }

        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")
        for (topic in topics) {
            val l3Answer = student.rawData["${topic.lowercase()}_l3_answer"] as? String
            if (!l3Answer.isNullOrEmpty()) {
                val currentScore = (student.rawData["${topic.lowercase()}_l3_score"] as? Number)?.toInt() ?: 0
                val essayView = createEssayGradingView("Repleksyon: ${topic.uppercase()}", l3Answer, currentScore)
                mainContainer.addView(essayView)
                gradeInputs["${topic.lowercase()}_l3_score"] = essayView.findViewWithTag("input")
            }
        }
        
        if (gradeInputs.isEmpty()) {
            Toast.makeText(this, "No reflection answers submitted yet.", Toast.LENGTH_SHORT).show()
            return
        }

        scrollView.addView(mainContainer)
        builder.setView(scrollView)

        builder.setPositiveButton("Save Grades") { dialog, _ ->
            val updates = mutableMapOf<String, Any>()
            for ((key, editText) in gradeInputs) {
                val scoreStr = editText.text.toString()
                if (scoreStr.isNotEmpty()) {
                    updates[key] = scoreStr.toInt()
                }
            }

            if (updates.isNotEmpty()) {
                db.collection("quiz_results").document(student.id)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Grades Saved!", Toast.LENGTH_SHORT).show()
                        fetchDataFromFirebase()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save grades.", Toast.LENGTH_SHORT).show()
                    }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }


    private fun createScoreView(title: String, score: String): View {
        val card = CardView(this).apply {
            radius = 30f
            cardElevation = 8f
            setCardBackgroundColor(resources.getColor(R.color.four, theme))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(10, 10, 10, 20)
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 25, 40, 25)
            gravity = Gravity.CENTER_VERTICAL
        }

        layout.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.one, theme))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        layout.addView(TextView(this).apply {
            text = score
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.black, theme))
        })
        card.addView(layout)
        return card
    }

    private fun createEssayGradingView(title: String, answer: String, currentScore: Int): View {
        val essayContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.edittext_bg)
            setPadding(25, 25, 25, 25)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 30)
            }
        }

        essayContainer.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.one, theme))
        })

        essayContainer.addView(TextView(this).apply {
            text = answer
            setTextColor(resources.getColor(R.color.black, theme))
            textSize = 14f
            setPadding(0, 10, 0, 10)
        })

        val scoreInput = EditText(this).apply {
            hint = "Enter score"
            setText(if (currentScore > 0) currentScore.toString() else "")
            tag = "input"
        }

        essayContainer.addView(scoreInput)

        return essayContainer
    }
}

@com.google.firebase.firestore.IgnoreExtraProperties
data class StudentResult(
    val id: String = "",
    val studentName: String = "",
    val section: String = "",
    val total_score: Int = 0,
    val pagtataya_score: Int = 0,
    @get:com.google.firebase.firestore.Exclude val rawData: Map<String, Any> = emptyMap()
)
