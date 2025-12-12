package com.example.tekbuk.HomepageContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
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
        enableEdgeToEdge()
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

        // UPDATE ADAPTER INITIALIZATION
        // It now takes two lambda functions: one for item clicks, one for delete clicks.
        adapter = StudentAdapter(
            filteredStudentList,
            onItemClicked = { selectedStudent ->
                showStudentDetailsDialog(selectedStudent)
            },
            onDeleteClicked = { studentToDelete ->
                showDeleteConfirmationDialog(studentToDelete)
            }
        )
        recyclerView.adapter = adapter

        fetchDataFromFirebase()
    }

    // --- START OF NEW DELETE FUNCTIONS ---

    private fun showDeleteConfirmationDialog(student: StudentResult) {
        // 1. Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)

        // 2. Create the AlertDialog with the custom view
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        // 3. Find views inside the custom layout
        val messageTextView = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)
        val deleteButton = dialogView.findViewById<Button>(R.id.btnDelete)

        // 4. Set the dynamic message for the specific student
        messageTextView.text = "Are you sure you want to permanently delete all records for ${student.studentName}?\n\nThis action cannot be undone."

        // 5. Create the dialog and make its background transparent
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 6. Set click listeners for the custom buttons
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteStudentData(student)
            dialog.dismiss()
        }

        // 7. Show the dialog
        dialog.show()
    }


    private fun deleteStudentData(student: StudentResult) {
        if (student.id.isBlank()) {
            Toast.makeText(this, "Cannot delete: Student ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        db.collection("quiz_results").document(student.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${student.studentName}'s record deleted successfully.", Toast.LENGTH_SHORT).show()
                // Refresh the list from local memory for a faster UI update
                val studentToRemove = allStudentsList.find { it.id == student.id }
                if (studentToRemove != null) {
                    allStudentsList.remove(studentToRemove)
                }
                // Re-apply the current filter to update the RecyclerView
                filterStudentList(sectionSpinner.selectedItem.toString())
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to delete record: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --- END OF NEW DELETE FUNCTIONS ---

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
        // 1. Inflate your custom XML layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_student_details, null)

        // 2. Create the AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        // 3. Find the views inside your inflated layout
        val studentNameTextView = dialogView.findViewById<TextView>(R.id.tvDialogStudentName)
        val studentSectionTextView = dialogView.findViewById<TextView>(R.id.tvDialogStudentSection)
        val scoresButton = dialogView.findViewById<Button>(R.id.btnViewScores)
        val reflectionsButton = dialogView.findViewById<Button>(R.id.btnGradeReflections)

        // 4. Set the data from the 'student' object
        studentNameTextView.text = student.studentName
        studentSectionTextView.text = student.section

        // 5. Create the dialog and make its window background transparent
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 6. Set the click listeners for the buttons
        scoresButton.setOnClickListener {
            dialog.dismiss() // Good practice to dismiss the current dialog
            showScoresDialog(student)
        }

        reflectionsButton.setOnClickListener {
            dialog.dismiss() // Dismiss this dialog first
            showGradingDialog(student)
        }

        // 7. Show the dialog
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_student_details, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)

        val title = dialogView.findViewById<TextView>(R.id.tvDialogStudentName)
        val section = dialogView.findViewById<TextView>(R.id.tvDialogStudentSection)
        val btn1 = dialogView.findViewById<Button>(R.id.btnViewScores)
        val btn2 = dialogView.findViewById<Button>(R.id.btnGradeReflections)

        title.text = "Grade Reflections"
        section.text = student.studentName

        btn1.visibility = View.GONE
        btn2.visibility = View.GONE

        val buttonContainer = btn1.parent as LinearLayout
        val topicsWithAnswers = mutableListOf<Pair<String, String>>()

        val mainRepAnswer = student.rawData["repleksyon_main_answer"] as? String
        if (!mainRepAnswer.isNullOrEmpty()) {
            topicsWithAnswers.add("PANGKALAHATANG REPLEKSYON" to "repleksyon_main")
        }

        val topics = listOf("tula", "sanaysay", "dagli", "talumpati", "kwentong_bayan")
        for (topic in topics) {
            val l3Answer = student.rawData["${topic}_l3_answer"] as? String
            if (!l3Answer.isNullOrEmpty()) {
                topicsWithAnswers.add("Repleksyon: ${topic.uppercase()}" to "${topic}_l3")
            }
        }

        if (topicsWithAnswers.isEmpty()) {
            Toast.makeText(this, "No reflection answers submitted by this student.", Toast.LENGTH_LONG).show()
            return
        }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        for ((buttonTitle, dataKey) in topicsWithAnswers) {
            val button = Button(this).apply {
                text = buttonTitle
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.one, theme))
                setTextColor(resources.getColor(R.color.white, theme))
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 8, 0, 8)
                layoutParams = params
            }
            button.setOnClickListener {
                dialog.dismiss()
                showEssayGradingPopup(student, buttonTitle, dataKey)
            }
            buttonContainer.addView(button)
        }

        dialog.show()
    }

    private fun showEssayGradingPopup(student: StudentResult, title: String, dataKey: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_grade_essay, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)

        val tvEssayTitle = dialogView.findViewById<TextView>(R.id.tvEssayTitle)
        val tvEssayAnswer = dialogView.findViewById<TextView>(R.id.tvEssayAnswer)
        val etEssayScore = dialogView.findViewById<EditText>(R.id.etEssayScore)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEssayGrade)

        val maxScore = if (dataKey == "repleksyon_main") 30 else 10
        val scoreKey = "${dataKey}_score"

        val answer = student.rawData["${dataKey}_answer"] as? String ?: "Answer not found."
        val currentScore = (student.rawData["${dataKey}_score"] as? Number)?.toInt() ?: 0

        tvEssayTitle.text = title
        tvEssayAnswer.text = answer
        etEssayScore.hint = "Score / $maxScore"

        etEssayScore.setHintTextColor(resources.getColor(R.color.one, theme))

        if (currentScore > 0) {
            etEssayScore.setText(currentScore.toString())
        }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val scoreStr = etEssayScore.text.toString()
            if (scoreStr.isNotEmpty()) {
                val newScore = scoreStr.toInt()

                if (newScore > maxScore) {
                    Toast.makeText(this, "Score cannot exceed $maxScore points.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                // --- START OF FIX: RECALCULATE TOTAL SCORE ---

                // 1. Get the student's total score and the old score for this specific essay.
                val currentTotalScore = student.total_score
                val oldEssayScore = (student.rawData[scoreKey] as? Number)?.toInt() ?: 0

                // 2. Calculate the new total score.
                // Formula: newTotal = currentTotal - oldScore + newScore
                val newTotalScore = currentTotalScore - oldEssayScore + newScore

                // 3. Prepare a map with BOTH the essay score AND the updated total score.
                val updates = mapOf(
                    scoreKey to newScore,          // e.g., "tula_l3_score" to 10
                    "total_score" to newTotalScore // Update the main total
                )

                // --- END OF FIX ---
                val scoreKey = "${dataKey}_score"

                db.collection("quiz_results").document(student.id)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Grade saved!", Toast.LENGTH_SHORT).show()
                        fetchDataFromFirebase()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a score.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
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
