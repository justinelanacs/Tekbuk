package com.example.tekbuk.GawainContent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.databinding.ActivityTulaLevel2Binding
import com.example.tekbuk.R
import java.io.BufferedReader
import java.io.InputStreamReader

class TULA_level2 : AppCompatActivity() {

    private val paksaId = "tula"
    private val levelCompleted = 2
    private lateinit var binding: ActivityTulaLevel2Binding

    private val questions = ArrayList<String>()
    private val choices = ArrayList<List<String>>()
    private val answers = ArrayList<Int>()

    private val userAnswers = mutableMapOf<Int, Int>()
    private var index = 0

    private var timer: CountDownTimer? = null
    private val totalTime = 10 * 60 * 1000L
    private var startTime: Long = 0L
    private var quizStarted = false

    private var quizAttempts = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityTulaLevel2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shuffleQuestionsAndChoices()

        // Load previous highest score and attempts
        val prefs = getSharedPreferences("TULA_level2", MODE_PRIVATE)
        val highestScore = prefs.getInt("highest_score", 0)
        quizAttempts = prefs.getInt("quiz_attempts", 0)

        // Show last score and attempts only if user has already finished at least 1 attempt
        binding.lastScoreText.visibility = if (quizAttempts > 0) View.VISIBLE else View.GONE
        if (quizAttempts > 0) {
            binding.lastScoreText.text =
                "Highest Score: $highestScore / ${questions.size}\nAttempts Taken: $quizAttempts / $maxAttempts"
        }

        // Initial UI
        binding.quizCard.visibility = View.GONE
        binding.buttonLayout.visibility = View.GONE
        binding.scoreCard.visibility = View.GONE

        binding.startBtn.visibility = if (quizAttempts >= maxAttempts) View.GONE else View.VISIBLE

        binding.startBtn.setOnClickListener { startQuiz() }
        binding.retakeBtn.setOnClickListener { if (quizAttempts < maxAttempts) startQuiz() }
        binding.nextBtn.setOnClickListener { moveNextQuestion() }
        binding.prevBtn.setOnClickListener { movePreviousQuestion() }
        binding.finishBtn.setOnClickListener { finishQuiz() }
    }

    private fun startQuiz() {
        binding.timerText.text = "00:00"
        quizStarted = true
        binding.startBtn.visibility = View.GONE
        binding.scoreCard.visibility = View.GONE
        binding.quizCard.visibility = View.VISIBLE
        binding.buttonLayout.visibility = View.VISIBLE

        index = 0
        userAnswers.clear()
        startTime = System.currentTimeMillis()
        displayQuestion()
        startTimer()
    }

    private fun shuffleQuestionsAndChoices() {
        val tempQuestions = ArrayList<Question>()
        val input = resources.openRawResource(R.raw.tulalevel2)
        val reader = BufferedReader(InputStreamReader(input))

        reader.forEachLine { line ->
            val parts = line.split("|")
            if (parts.size == 6) {
                val qText = parts[0]
                val choiceList = parts.subList(1, 5).toMutableList()
                val correctIndex = parts[5].toInt() - 1
                tempQuestions.add(Question(qText, choiceList, correctIndex))
            }
        }
        reader.close()

        tempQuestions.shuffle()
        tempQuestions.forEach { q ->
            val originalCorrect = q.choices[q.correctIndex]
            q.choices.shuffle()
            q.correctIndex = q.choices.indexOf(originalCorrect)
        }

        questions.clear()
        choices.clear()
        answers.clear()
        tempQuestions.forEach { q ->
            questions.add(q.text)
            choices.add(q.choices)
            answers.add(q.correctIndex + 1)
        }
    }

    data class Question(val text: String, val choices: MutableList<String>, var correctIndex: Int)

    private fun displayQuestion() {
        binding.questionText.text = questions[index]
        binding.choiceA.text = choices[index][0]
        binding.choiceB.text = choices[index][1]
        binding.choiceC.text = choices[index][2]
        binding.choiceD.text = choices[index][3]

        binding.choicesGroup.clearCheck()
        userAnswers[index]?.let {
            when (it) {
                1 -> binding.choiceA.isChecked = true
                2 -> binding.choiceB.isChecked = true
                3 -> binding.choiceC.isChecked = true
                4 -> binding.choiceD.isChecked = true
            }
        }

        binding.prevBtn.visibility = if (index == 0) View.GONE else View.VISIBLE
        binding.nextBtn.visibility = if (index == questions.size - 1) View.GONE else View.VISIBLE
        binding.finishBtn.visibility = if (index == questions.size - 1) View.VISIBLE else View.GONE

        val params = binding.finishBtn.layoutParams as? LinearLayout.LayoutParams
        params?.let {
            it.marginStart = if (index == questions.size - 1) 40 else 20
            binding.finishBtn.layoutParams = it
        }
    }

    private fun moveNextQuestion() {
        saveUserSelection()
        if (index < questions.size - 1) {
            index++
            animateCardRight()
            displayQuestion()
        }
    }

    private fun movePreviousQuestion() {
        saveUserSelection()
        if (index > 0) {
            index--
            animateCardLeft()
            displayQuestion()
        }
    }

    private fun saveUserSelection() {
        val selectedId = binding.choicesGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedRadio = findViewById<RadioButton>(selectedId)
            val answerNumber = when (selectedRadio.id) {
                binding.choiceA.id -> 1
                binding.choiceB.id -> 2
                binding.choiceC.id -> 3
                binding.choiceD.id -> 4
                else -> 0
            }
            if (answerNumber != 0) userAnswers[index] = answerNumber
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(ms: Long) {
                val minutes = (ms / 1000) / 60
                val seconds = (ms / 1000) % 60
                binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                finishQuiz()
            }
        }.start()
    }

    private fun finishQuiz() {
        if (!quizStarted) return
        timer?.cancel()
        saveUserSelection()

        val elapsedTime = System.currentTimeMillis() - startTime
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60

        var currentScore = 0
        for (i in questions.indices) {
            if (userAnswers[i] == answers[i]) currentScore++
        }

        quizAttempts++

        val prefs = getSharedPreferences("TULA_level2", MODE_PRIVATE)
        val previousHigh = prefs.getInt("highest_score", 0)
        val newHigh = if (currentScore > previousHigh) currentScore else previousHigh

        binding.quizCard.visibility = View.GONE
        binding.buttonLayout.visibility = View.GONE
        binding.scoreCard.visibility = View.VISIBLE

        binding.finalScoreText.text =
            "Score: $currentScore / ${questions.size}\nTime Taken: ${minutes}m ${seconds}s\nAttempt: $quizAttempts / $maxAttempts"

        binding.retakeBtn.visibility = if (quizAttempts < maxAttempts) View.VISIBLE else View.GONE
        binding.startBtn.visibility = if (quizAttempts >= maxAttempts) View.GONE else View.VISIBLE
        binding.finishQuizBtn.visibility = View.VISIBLE

        binding.finishQuizBtn.setOnClickListener {
            // Save highest score and attempts
            prefs.edit().apply {
                putInt("highest_score", newHigh)
                putLong("last_time_ms", elapsedTime)
                putInt("quiz_attempts", quizAttempts)
                apply()
            }

            val intent = Intent().apply {
                putExtra("paksa_id", paksaId)
                putExtra("level_completed", levelCompleted)
            }
            setResult(Activity.RESULT_OK, intent)
            finish() // Finish activity
        }

        if (quizAttempts >= maxAttempts) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Limit Reached")
                .setMessage("You have reached the maximum number of attempts ($maxAttempts).")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .setCancelable(false)
                .show()
        }
    }

    private fun animateCardRight() {
        val anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        binding.quizCard.startAnimation(anim)
    }

    private fun animateCardLeft() {
        val anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        binding.quizCard.startAnimation(anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
