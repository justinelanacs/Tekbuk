package com.example.tekbuk.GawainContent

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
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

class TULA_level1 : AppCompatActivity() {

    private lateinit var crosswordGrid: GridLayout
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView

    private var timerStarted = false
    private var countDownTimer: CountDownTimer? = null
    private var score = 0
    private val gridSize = 15
    private val cells = Array(gridSize) { arrayOfNulls<EditText>(gridSize) }

    // This flag prevents finishLevel from being called multiple times
    private var levelFinished = false

    data class Word(
        val text: String,
        val startRow: Int,
        val startCol: Int,
        val direction: String,
        val clue: String
    )

    private val words = listOf(
        Word("PAGDURUSA", 3, 4, "H", "Paghihirap."),
        Word("MARAHAS", 5, 0, "H", "Malupit."),
        Word("ISINATITIK", 7, 5, "H", "Isinulat."),
        Word("KASARINLAN", 2, 5, "V", "Kalayaan."),
        Word("KANDILI", 5, 8, "V", "Dala-dala.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tula_level1)
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
                            startTimer(10 * 60 * 1000) // 10 minutes
                            timerStarted = true
                        }
                        showWordDialog(word)
                    }
                }
            }
        }
    }

    private fun showWordDialog(word: Word) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val clueView = TextView(this).apply {
            text = word.clue
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }
        val input = EditText(this).apply {
            hint = "Enter your answer"
            isSingleLine = true
        }
        layout.addView(clueView)
        layout.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Answer the clue!!")
            .setView(layout)
            .setPositiveButton("Submit") { dialog, _ ->
                val answer = input.text.toString().uppercase()
                if (answer == word.text) {
                    fillWord(word, isNewAnswer = true)
                    Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Wrong! Try again.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            // ⭐ FIX: Save the word state BEFORE checking for completion
            saveWordState(word)

            // Recalculate score from scratch to ensure accuracy
            recalculateScore()

            // Now check if all words are completed
            if (words.all { wordCompleted(it) } && !levelFinished) {
                countDownTimer?.cancel()
                finishLevel()
            }
        }
    }

    private fun wordCompleted(word: Word): Boolean {
        val sharedPref = getSharedPreferences("TULA_Level1_Progress", MODE_PRIVATE)
        return sharedPref.getBoolean("word_${word.text}_completed", false)
    }

    private fun finishLevel() {
        // Prevent this function from running more than once
        if (levelFinished) return
        levelFinished = true

        // Ensure score is correct before finishing
        recalculateScore()

        // Save the final score to the correct "UserScores" file
        saveFinalScore("TULA", 1, score)

        val resultIntent = Intent().apply {
            putExtra("paksa_id", "tula")
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
        val editor = prefs.edit()
        val key = "${topic}_LEVEL_${level}"
        editor.putInt(key, scoreToSave)
        editor.apply()
    }

    private fun saveWordState(word: Word) {
        val sharedPref = getSharedPreferences("TULA_Level1_Progress", MODE_PRIVATE)
        sharedPref.edit().putBoolean("word_${word.text}_completed", true).apply()
    }

    // ⭐ FIX: Renamed this function for clarity and purpose
    private fun loadSavedState() {
        val sharedPref = getSharedPreferences("TULA_Level1_Progress", MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                fillWord(word, isNewAnswer = false)
            }
        }
        // Recalculate score based on loaded state
        recalculateScore()
    }

    // ⭐ FIX: New function to reliably calculate the score
    private fun recalculateScore() {
        var currentScore = 0
        val sharedPref = getSharedPreferences("TULA_Level1_Progress", MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                currentScore += 2
            }
        }
        score = currentScore
        scoreText.text = if (score > 0) "Score: $score / 10" else ""
    }
}
