package com.example.tekbuk.GawainContent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tekbuk.R
import com.example.tekbuk.databinding.ActivitySanaysayLevel2Binding
import java.io.BufferedReader
import java.io.InputStreamReader

class SANAYSAY_level2 : AppCompatActivity() {

    private val paksaId = "sanaysay"
    private val levelCompleted = 2
    private lateinit var binding: ActivitySanaysayLevel2Binding

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
        binding = ActivitySanaysayLevel2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shuffleQuestionsAndChoices()

        val prefs = getSharedPreferences("SANAYSAY_level2_Progress", MODE_PRIVATE)
        val highestScore = prefs.getInt("highest_score", 0)
        quizAttempts = prefs.getInt("quiz_attempts", 0)

        binding.lastScoreText.visibility = if (quizAttempts > 0) View.VISIBLE else View.GONE
        if (quizAttempts > 0) {
            binding.lastScoreText.text =
                "Highest Score: $highestScore / ${questions.size}\nAttempts Taken: $quizAttempts / $maxAttempts"
        }

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
        binding.timerText.text = "10:00"
        quizStarted = true
        binding.startBtn.visibility = View.GONE
        binding.scoreCard.visibility = View.GONE
        binding.quizCard.visibility = View.VISIBLE
        binding.buttonLayout.visibility = View.VISIBLE
        binding.lastScoreText.visibility = View.GONE

        index = 0
        userAnswers.clear()
        startTime = System.currentTimeMillis()

        displayQuestion()
        startTimer()
    }

    private fun finishQuiz() {
        if (!quizStarted) return
        timer?.cancel()
        quizStarted = false
        saveUserSelection()

        val elapsedTime = System.currentTimeMillis() - startTime
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60

        var currentScore = 0
        for (i in questions.indices) {
            if (userAnswers.getOrDefault(i, -1) == answers[i]) {
                currentScore++
            }
        }

        quizAttempts++

        val prefs = getSharedPreferences("SANAYSAY_level2_Progress", MODE_PRIVATE)
        val oldHigh = prefs.getInt("highest_score", 0)
        val newHigh = if (currentScore > oldHigh) currentScore else oldHigh

        val userScoresPrefs = getSharedPreferences("UserScores", Context.MODE_PRIVATE)
        userScoresPrefs.edit().putInt("SANAYSAY_LEVEL_2", newHigh).apply()

        binding.quizCard.visibility = View.GONE
        binding.buttonLayout.visibility = View.GONE
        binding.scoreCard.visibility = View.VISIBLE

        binding.finalScoreText.text =
            "Score: $currentScore / ${questions.size}\n" +
                    "Time Taken: ${minutes}m ${seconds}s\n" +
                    "Attempt: $quizAttempts / $maxAttempts"

        binding.retakeBtn.visibility = if (quizAttempts < maxAttempts) View.VISIBLE else View.GONE
        binding.finishQuizBtn.visibility = View.VISIBLE

        binding.finishQuizBtn.setOnClickListener {
            prefs.edit().apply {
                putInt("highest_score", newHigh)
                putLong("last_time_ms", elapsedTime)
                putInt("quiz_attempts", quizAttempts)
                apply()
            }

            val intent = Intent().apply {
                putExtra("paksa_id", paksaId)
                putExtra("level_completed", levelCompleted)
                putExtra("score", newHigh)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        if (quizAttempts >= maxAttempts) {
            binding.retakeBtn.visibility = View.GONE
        }
    }

    private fun shuffleQuestionsAndChoices() {
        val tempQuestions = ArrayList<Question>()
        try {
            // Make sure you have a raw file named "sanaysaylevel2.raw"
            val input = resources.openRawResource(R.raw.sanaysaylevel2)
            val reader = BufferedReader(InputStreamReader(input))
            reader.forEachLine { line ->
                val parts = line.split("|")
                if (parts.size == 6) {
                    tempQuestions.add(Question(parts[0], parts.subList(1, 5).toMutableList(), parts[5].toInt() - 1))
                }
            }
            reader.close()
        } catch (e: Exception) { e.printStackTrace() }

        tempQuestions.shuffle()
        tempQuestions.forEach { q ->
            val correctAnswer = q.choices[q.correctIndex]
            q.choices.shuffle()
            q.correctIndex = q.choices.indexOf(correctAnswer)
        }

        questions.clear(); choices.clear(); answers.clear()
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
    }

    private fun saveUserSelection() {
        val selectedId = binding.choicesGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val answerNumber = when (findViewById<RadioButton>(selectedId).id) {
                R.id.choiceA -> 1
                R.id.choiceB -> 2
                R.id.choiceC -> 3
                R.id.choiceD -> 4
                else -> 0
            }
            if (answerNumber != 0) userAnswers[index] = answerNumber
        }
    }

    private fun moveNextQuestion() {
        saveUserSelection()
        if (index < questions.size - 1) {
            index++
            animateCardLeftToRight()
            displayQuestion()
        }
    }

    private fun movePreviousQuestion() {
        saveUserSelection()
        if (index > 0) {
            index--
            animateCardRightToLeft()
            displayQuestion()
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
            override fun onFinish() { if (quizStarted) finishQuiz() }
        }.start()
    }

    private fun animateCardLeftToRight() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        binding.quizCard.startAnimation(anim)
    }

    private fun animateCardRightToLeft() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        binding.quizCard.startAnimation(anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
