package com.example.tekbuk.GawainContent

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R

class SANAYSAY_level1 : AppCompatActivity() {

    private lateinit var crosswordGrid: GridLayout
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView

    private var timerStarted = false
    private var countDownTimer: CountDownTimer? = null
    private var score = 0
    private val gridSize = 15
    private val cells = Array(gridSize) { arrayOfNulls<EditText>(gridSize) }
    private var levelFinished = false

    data class Word(
        val text: String,
        val startRow: Int,
        val startCol: Int,
        val direction: String,
        val clue: String
    )

    private val words = listOf(
        Word("PUNTOD", 2, 2, "H", "Lugar kung saan inilalagak ang mga yumao."),
        Word("SUMANGYAPOS", 7, 2, "H", "Kakanin na gawa sa malagkit ang gata ng niyog."),
        Word("PAGHILOM", 10, 3, "H", "Pagkawala ng sakit o paggaling ng sugat."),
        Word("PAGLISAN", 2, 2, "V", "Pag-alis o pagkawala nang tuluyan."),
        Word("NAMAYAPA", 5, 4, "V", "Mahal sa buhay na binawian ng buhay.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sanaysay_level1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timerText = findViewById(R.id.timer)
        scoreText = findViewById(R.id.score)
        crosswordGrid = findViewById(R.id.crosswordGrid)

        buildGrid()
        loadSavedState()
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "Time's up!"
                disableAllCells()
                if (!levelFinished) {
                    finishLevel()
                }
            }
        }.start()
    }

    private fun disableAllCells() {
        cells.forEach { row -> row.forEach { it?.isEnabled = false } }
    }

    private fun buildGrid() {
        val sizeDp = 23
        val scale = resources.displayMetrics.density
        val px = (sizeDp * scale + 0.5f).toInt()

        crosswordGrid.rowCount = gridSize
        crosswordGrid.columnCount = gridSize

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = EditText(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = px
                        height = px
                        setMargins(1, 1, 1, 1)
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(col)
                    }
                    setBackgroundColor(Color.DKGRAY)
                    isEnabled = false
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    textSize = 12f
                    setTextColor(Color.BLACK)
                    gravity = android.view.Gravity.CENTER
                    isSingleLine = true
                    isFocusable = false
                    isFocusableInTouchMode = false
                    isCursorVisible = false
                    setPadding(0, 0, 0, 0)
                }
                crosswordGrid.addView(cell)
                cells[row][col] = cell
            }
        }

        for (word in words) {
            for (i in word.text.indices) {
                val r = if (word.direction == "H") word.startRow else word.startRow + i
                val c = if (word.direction == "H") word.startCol + i else word.startCol
                if (r !in 0 until gridSize || c !in 0 until gridSize) continue
                cells[r][c]?.apply {
                    setBackgroundColor(Color.WHITE)
                    isEnabled = true
                    setOnClickListener {
                        if (!timerStarted) {
                            startTimer(10 * 60 * 1000)
                            timerStarted = true
                        }
                        showWordDialog(word)
                    }
                }
            }
        }
    }

    private fun showWordDialog(word: SANAYSAY_level1.Word) {
        // 1. Inflate the custom layout we just created
        val dialogView = layoutInflater.inflate(R.layout.dialog_crossword_input, null)

        // 2. Create the AlertDialog builder
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // 3. Create the dialog and make its background transparent to show the CardView's rounded corners
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. Get the views from inside our custom layout
        val clueView = dialogView.findViewById<TextView>(R.id.dialogClue)
        val input = dialogView.findViewById<EditText>(R.id.dialogInput)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnDialogSubmit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)

        // Set the clue text dynamically
        clueView.text = word.clue

        // 5. Set the click listener for the Submit button
        btnSubmit.setOnClickListener {
            val answer = input.text.toString().trim().uppercase()
            if (answer == word.text) {
                fillWord(word, isNewAnswer = true)
                Toast.makeText(this, "✅ Tama!", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Close the dialog on correct answer
            } else {
                Toast.makeText(this, "❌ Mali! Subukan muli.", Toast.LENGTH_SHORT).show()
                // We don't dismiss the dialog, so the user can try again
            }
        }

        // 6. Set the click listener for the Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss() // Just close the dialog
        }

        // 7. Show the beautiful new dialog
        dialog.show()
    }

    private fun fillWord(word: Word, isNewAnswer: Boolean) {
        for (i in word.text.indices) {
            val r = if (word.direction == "H") word.startRow else word.startRow + i
            val c = if (word.direction == "H") word.startCol + i else word.startCol
            cells[r][c]?.apply {
                setText(word.text[i].toString())
                isEnabled = false
                setBackgroundColor(Color.parseColor("#C8E6C9"))
            }
        }

        if (isNewAnswer) {
            saveWordState(word)
            recalculateScore()
            if (words.all { wordCompleted(it) } && !levelFinished) {
                countDownTimer?.cancel()
                finishLevel()
            }
        }
    }

    private fun wordCompleted(word: Word): Boolean {
        val sharedPref = getSharedPreferences("SANAYSAY_Level1_Progress", MODE_PRIVATE)
        return sharedPref.getBoolean("word_${word.text}_completed", false)
    }

    private fun finishLevel() {
        if (levelFinished) return
        levelFinished = true
        recalculateScore()
        saveFinalScore("SANAYSAY", 1, score)

        val resultIntent = Intent().apply {
            putExtra("paksa_id", "sanaysay")
            putExtra("level_completed", 1)
            putExtra("score", score)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        AlertDialog.Builder(this)
            .setTitle("Level Completed!")
            .setMessage("Your final score is $score / 10")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun saveFinalScore(topic: String, level: Int, scoreToSave: Int) {
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val key = "${topic}_LEVEL_${level}"
        prefs.edit().putInt(key, scoreToSave).apply()
    }

    private fun saveWordState(word: Word) {
        val sharedPref = getSharedPreferences("SANAYSAY_Level1_Progress", MODE_PRIVATE)
        sharedPref.edit().putBoolean("word_${word.text}_completed", true).apply()
    }

    private fun loadSavedState() {
        val sharedPref = getSharedPreferences("SANAYSAY_Level1_Progress", MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                fillWord(word, isNewAnswer = false)
            }
        }
        recalculateScore()
    }

    private fun recalculateScore() {
        var currentScore = 0
        val sharedPref = getSharedPreferences("SANAYSAY_Level1_Progress", MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                currentScore += 2
            }
        }
        score = currentScore
        scoreText.text = if (score > 0) "Score: $score / 10" else ""
    }
}
