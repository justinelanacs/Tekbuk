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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // ⭐ 1. IMPORT OnBackPressedCallback
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

    private var levelFinished = false

    // SharedPreferences keys
    private val prefsName = "TULA_Level1_Progress"
    private val keyLevelFinished = "LevelFinishedPermanently"
    private val keyTimeTaken = "TimeTaken"

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

        // ⭐ 2. SET UP THE BACK BUTTON HANDLER
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Only show the warning if the level is not yet finished.
                if (!levelFinished) {
                    showExitWarningDialog()
                } else {
                    // If level is finished, allow normal back press.
                    finish()
                }
            }
        })

        timerText = findViewById(R.id.timer)
        scoreText = findViewById(R.id.score)
        crosswordGrid = findViewById(R.id.crosswordGrid)

        // Check if the level has been permanently finished
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        if (prefs.getBoolean(keyLevelFinished, false)) {
            levelFinished = true
            showAlreadyCompletedDialog()
        } else {
            buildGrid()
            loadSavedState()
        }
    }

    // ⭐ 3. UPDATED FUNCTION: Shows a CardView dialog with score and time.
    private fun showAlreadyCompletedDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_level_completed, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Get views from the dialog layout
        val tvScore = dialogView.findViewById<TextView>(R.id.tvDialogScore)
        val tvTime = dialogView.findViewById<TextView>(R.id.tvDialogTime)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)

        // Retrieve the saved score and time
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val savedScore = getSharedPreferences("UserScores", MODE_PRIVATE).getInt("TULA_LEVEL_1", 0)
        val timeTakenSeconds = prefs.getInt(keyTimeTaken, 0)

        // Format the retrieved data and set it to the TextViews
        tvScore.text = "$savedScore / 10"
        val minutes = timeTakenSeconds / 60
        val seconds = timeTakenSeconds % 60
        tvTime.text = String.format("%02d:%02d", minutes, seconds)

        // Set the button click listener to finish the activity
        btnOk.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.setCancelable(false)
        dialog.show()
    }


    // ⭐ 4. NEW FUNCTION: Shows a warning dialog when the user tries to exit.
    private fun showExitWarningDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exit_warning, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmExit: Button = dialogView.findViewById(R.id.btnExitConfirm)
        val btnCancel: Button = dialogView.findViewById(R.id.btnExitCancel)

        btnConfirmExit.setOnClickListener {
            dialog.dismiss()
            // Process, save, and exit prematurely.
            processAndSaveFinalResult(isPrematureExit = true)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // ⭐ 5. NEW FUNCTION: Central place to handle saving the score and exiting.
    // In processAndSaveFinalResult()

    private fun processAndSaveFinalResult(isPrematureExit: Boolean) {
        if (levelFinished) return

        levelFinished = true
        countDownTimer?.cancel()
        recalculateScore()
        saveFinalScore("TULA", 1, score)

        // ⭐ SAVE THE TIME TAKEN
        val timeRemaining = timerText.text.toString()
        val totalTime = 10 * 60 // 10 minutes in seconds
        val parts = timeRemaining.split(":")
        val remainingSeconds = if (parts.size == 2) {
            parts[0].toInt() * 60 + parts[1].toInt()
        } else {
            0
        }
        val timeTakenSeconds = totalTime - remainingSeconds

        // Mark the level as permanently finished and save time
        getSharedPreferences(prefsName, MODE_PRIVATE).edit().apply {
            putBoolean(keyLevelFinished, true)
            putInt(keyTimeTaken, timeTakenSeconds) // Save time in seconds
            apply()
        }

        // ... rest of the function is the same
        val resultIntent = Intent().apply {
            putExtra("paksa_id", "tula")
            putExtra("level_completed", 1)
            putExtra("score", score)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        if (isPrematureExit) {
            Toast.makeText(this, "Your progress has been saved.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Level Completed!")
                .setMessage("Your final score is $score / 10")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer to prevent memory leaks when the activity is destroyed.
        countDownTimer?.cancel()
    }

    private fun startTimer(timeInMillis: Long) {
        if (timerStarted) return
        timerStarted = true
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "Time's up!"
                disableAllCells()
                processAndSaveFinalResult(isPrematureExit = false)
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
                        startTimer(10 * 60 * 1000) // 10 minutes
                        showWordDialog(word)
                    }
                }
            }
        }
    }

    private fun showWordDialog(word: Word) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_crossword_input, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val clueView = dialogView.findViewById<TextView>(R.id.dialogClue)
        val input = dialogView.findViewById<EditText>(R.id.dialogInput)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnDialogSubmit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)

        clueView.text = word.clue

        btnSubmit.setOnClickListener {
            val answer = input.text.toString().trim().uppercase()
            if (answer == word.text) {
                fillWord(word, isNewAnswer = true)
                Toast.makeText(this, "✅ Tama!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "❌ Mali! Subukan muli.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
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

            // Check if all words are completed
            if (words.all { wordCompleted(it) }) {
                processAndSaveFinalResult(isPrematureExit = false)
            }
        }
    }

    private fun wordCompleted(word: Word): Boolean {
        val sharedPref = getSharedPreferences(prefsName, MODE_PRIVATE)
        return sharedPref.getBoolean("word_${word.text}_completed", false)
    }

    private fun saveFinalScore(topic: String, level: Int, scoreToSave: Int) {
        val prefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val key = "${topic}_LEVEL_${level}"
        editor.putInt(key, scoreToSave)
        editor.apply()
    }

    private fun saveWordState(word: Word) {
        val sharedPref = getSharedPreferences(prefsName, MODE_PRIVATE)
        sharedPref.edit().putBoolean("word_${word.text}_completed", true).apply()
    }

    private fun loadSavedState() {
        val sharedPref = getSharedPreferences(prefsName, MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                fillWord(word, isNewAnswer = false)
            }
        }
        recalculateScore()
    }

    private fun recalculateScore() {
        var currentScore = 0
        val sharedPref = getSharedPreferences(prefsName, MODE_PRIVATE)
        for (word in words) {
            if (sharedPref.getBoolean("word_${word.text}_completed", false)) {
                currentScore += 2
            }
        }
        score = currentScore
        scoreText.text = if (score > 0) "Score: $score / 10" else "Score: 0 / 10"
    }
}
