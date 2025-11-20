package com.example.tekbuk.GawainContent

import android.app.Activity
import android.app.AlertDialog
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

class KWENTONGBAYAN_level1 : AppCompatActivity() {

    private lateinit var crosswordGrid: GridLayout
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView

    private var timerStarted = false
    private var countDownTimer: CountDownTimer? = null
    private var score = 0
    private val gridSize = 15
    private val cells = Array(gridSize) { arrayOfNulls<EditText>(gridSize) }

    data class Word(
        val text: String,
        val startRow: Int,
        val startCol: Int,
        val direction: String,
        val clue: String
    )

    private val words = listOf(
        Word("SIMPLE", 5, 6, "H", "Payak."),
        Word("NAKAHANDA", 9, 4, "H", "Nakalaan."),
        Word("GINULAT", 4, 7, "V", "Ginulantang."),
        Word("MAGNAKAW", 6, 4, "V", "Mang-umit."),
        Word("NAUBOS", 8, 9, "V", "Nalimas.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kwentongbayan_level1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timerText = findViewById(R.id.timer)
        timerText.text = "00:00"

        scoreText = findViewById(R.id.score)
        scoreText.text = ""

        crosswordGrid = findViewById(R.id.crosswordGrid)

        buildGrid()
        loadSavedState() // Restore previous answers and score
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
                finishLevel()
            }
        }.start()
    }

    private fun disableAllCells() {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                cells[row][col]?.isEnabled = false
            }
        }
    }

    private fun buildGrid() {
        val sizeDp = 23
        val scale = resources.displayMetrics.density
        val px = (sizeDp * scale + 0.5f).toInt()

        crosswordGrid.rowCount = gridSize
        crosswordGrid.columnCount = gridSize

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = EditText(this)
                val params = GridLayout.LayoutParams().apply {
                    width = px
                    height = px
                    setMargins(1, 1, 1, 1)
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                }
                cell.layoutParams = params
                cell.setBackgroundColor(Color.DKGRAY)
                cell.isEnabled = false
                cell.filters = arrayOf(InputFilter.LengthFilter(1))
                cell.textSize = sizeDp * 0.7f
                cell.setTextColor(Color.BLACK)
                cell.gravity = android.view.Gravity.CENTER
                cell.isSingleLine = true
                cell.isFocusable = false
                cell.isFocusableInTouchMode = false
                cell.isCursorVisible = false
                cell.setPadding(0, 0, 0, 0)
                crosswordGrid.addView(cell)
                cells[row][col] = cell
            }
        }

        for (word in words) {
            for (i in word.text.indices) {
                val r = if (word.direction == "H") word.startRow else word.startRow + i
                val c = if (word.direction == "H") word.startCol + i else word.startCol
                if (r !in 0 until gridSize || c !in 0 until gridSize) continue
                val cell = cells[r][c]
                cell?.apply {
                    setBackgroundColor(Color.WHITE)
                    isEnabled = true
                    val currentWord = word
                    setOnClickListener {
                        if (!timerStarted) {
                            startTimer(10 * 60 * 1000)
                            timerStarted = true
                        }
                        showWordDialog(currentWord)
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
                    fillWord(word, saveScore = true)
                    Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Wrong! Try again.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fillWord(word: Word, saveScore: Boolean) {
        for (i in word.text.indices) {
            val r = if (word.direction == "H") word.startRow else word.startRow + i
            val c = if (word.direction == "H") word.startCol + i else word.startCol
            val cell = cells[r][c]
            cell?.apply {
                setText(word.text[i].toString())
                isEnabled = false
                setBackgroundColor(Color.parseColor("#C8E6C9"))
            }
        }

        if (saveScore) {
            score += 2
            saveWordState(word)
            saveScoreState()
        }

        if (words.all { wordCompleted(it) }) {
            countDownTimer?.cancel()
            finishLevel()
        }
    }

    private fun wordCompleted(word: Word): Boolean {
        for (i in word.text.indices) {
            val r = if (word.direction == "H") word.startRow else word.startRow + i
            val c = if (word.direction == "H") word.startCol + i else word.startCol
            val cell = cells[r][c] ?: return false
            if (cell.text.toString() != word.text[i].toString()) return false
        }
        return true
    }

    private fun finishLevel() {
        val sharedPref = getSharedPreferences("KWENTONGBAYAN_level1", MODE_PRIVATE)
        val alreadyCompleted = sharedPref.getBoolean("level_completed", false)

        // Set result for GawainPageActivity
        val resultIntent = Intent().apply {
            putExtra("paksa_id", "kwentongbayan")
            putExtra("level_completed", 1)
            putExtra("score", score)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        // Show dialog only if not already completed
        if (!alreadyCompleted) {
            sharedPref.edit().putBoolean("level_completed", true).apply()
            AlertDialog.Builder(this)
                .setTitle("Level Completed!")
                .setMessage("Your final score is $score / 10")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
    }

    // --- Persistence ---
    private fun saveWordState(word: Word) {
        val sharedPref = getSharedPreferences("KWENTONGBAYAN_level1", MODE_PRIVATE)
        sharedPref.edit().putBoolean("word_${word.text}_completed", true).apply()
    }

    private fun saveScoreState() {
        val sharedPref = getSharedPreferences("KWENTONGBAYAN_level1", MODE_PRIVATE)
        sharedPref.edit().putInt("score", score).apply()
    }

    private fun loadSavedState() {
        val sharedPref = getSharedPreferences("KWENTONGBAYAN_level1", MODE_PRIVATE)
        score = sharedPref.getInt("score", 0)
        scoreText.text = if (score > 0) "Score: $score / 10" else ""

        for (word in words) {
            val completed = sharedPref.getBoolean("word_${word.text}_completed", false)
            if (completed) fillWord(word, saveScore = false)
        }
    }
}
