package com.example.tekbuk.HomepageContent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.MainActivity
import com.example.tekbuk.R
import java.util.Locale
import java.util.concurrent.TimeUnit

class PagtatayaPageActivity : AppCompatActivity() {

    // Data classes
    private data class Question(val questionText: String, val options: List<String>, val correctIndex: Int)
    private data class ShuffledQuestion(val questionText: String, val shuffledOptions: List<String>, val newCorrectIndex: Int, val originalOptions: List<String>)

    // UI Elements
    private lateinit var headerCard: View
    private lateinit var questionScrollView: View
    private lateinit var buttonsLayout: View
    private lateinit var instructionOverlay: FrameLayout
    private lateinit var btnStartQuiz: Button
    private lateinit var tvQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var tvCounter: TextView
    private lateinit var tvTimer: TextView

    // Logic Variables
    private val shuffledQuestionsList = ArrayList<ShuffledQuestion>()
    private val userAnswers = HashMap<Int, Int>()
    private var currentQuestionIndex = 0
    private var timer: CountDownTimer? = null

    // Constants for SharedPreferences to ensure keys are consistent
    private val PREFS_NAME = "PagtatayaState"
    private val KEY_FINISHED = "QuizFinishedPermanently"
    private val KEY_SCORE = "FinalScore"
    private val KEY_TOTAL_ITEMS = "FinalTotalItems"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.pagtataya_page)

        initializeViews()

        // 1. CHECK IF THE QUIZ HAS BEEN TAKEN AND FINISHED PERMANENTLY
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_FINISHED, false)) {
            val finalScore = prefs.getInt(KEY_SCORE, 0)
            val finalTotalItems = prefs.getInt(KEY_TOTAL_ITEMS, 0)

            // Hide the entire quiz UI
            headerCard.visibility = View.GONE
            questionScrollView.visibility = View.GONE
            buttonsLayout.visibility = View.GONE
            instructionOverlay.visibility = View.GONE

            // Immediately show the final result and prevent any further action
            showResultDialog(finalScore, finalTotalItems, true)

            // Stop here. Do not set up the quiz again.
            return
        }

        // --- If quiz has NOT been taken, proceed with normal first-time setup ---

        // Show the instruction overlay for the first take
        instructionOverlay.visibility = View.VISIBLE
        loadAndShuffleQuestions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Show exit warning only if the quiz has actually started (instructions are hidden)
                if (instructionOverlay.visibility == View.GONE) {
                    showExitWarningDialog()
                } else {
                    // If instructions are still visible, allow normal back press to exit
                    finish()
                }
            }
        })

        btnStartQuiz.setOnClickListener {
            instructionOverlay.visibility = View.GONE
            startTimer()
            displayQuestion()
        }

        btnNext.setOnClickListener {
            saveUserAnswer()
            if (currentQuestionIndex < shuffledQuestionsList.size - 1) {
                currentQuestionIndex++
                displayQuestion()
            } else {
                // This is the "TAPUSIN" button click on the last question
                confirmFinishQuiz()
            }
        }

        btnBack.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveUserAnswer()
                currentQuestionIndex--
                displayQuestion()
            }
        }
    }

    private fun initializeViews() {
        headerCard = findViewById(R.id.headerCard)
        questionScrollView = findViewById(R.id.questionScrollView)
        buttonsLayout = findViewById(R.id.buttonsLayout)
        instructionOverlay = findViewById(R.id.instructionOverlay)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        tvQuestion = findViewById(R.id.tvQuestion)
        radioGroup = findViewById(R.id.choicesRadioGroup)
        btnNext = findViewById(R.id.nextButton)
        btnBack = findViewById(R.id.backButton)
        tvCounter = findViewById(R.id.questionCounterText)
        tvTimer = findViewById(R.id.timerText)
    }

    private fun startTimer() {
        val totalTime = 5400000L
        timer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            }
            override fun onFinish() {
                Toast.makeText(this@PagtatayaPageActivity, "Tapos na ang oras!", Toast.LENGTH_LONG).show()
                processAndSaveFinalResult(isPrematureExit = true)
            }
        }.start()
    }

    private fun displayQuestion() {
        val currentQ = shuffledQuestionsList[currentQuestionIndex]
        tvCounter.text = "Tanong ${currentQuestionIndex + 1}/${shuffledQuestionsList.size}"
        tvQuestion.text = currentQ.questionText
        // A more robust way to set radio button text
        (radioGroup.getChildAt(0) as RadioButton).text = currentQ.shuffledOptions[0]
        (radioGroup.getChildAt(1) as RadioButton).text = currentQ.shuffledOptions[1]
        (radioGroup.getChildAt(2) as RadioButton).text = currentQ.shuffledOptions[2]
        (radioGroup.getChildAt(3) as RadioButton).text = currentQ.shuffledOptions[3]
        radioGroup.clearCheck()
        if (userAnswers.containsKey(currentQuestionIndex)) {
            val index = userAnswers[currentQuestionIndex]
            if (index != null && index >= 0 && index < radioGroup.childCount) {
                (radioGroup.getChildAt(index) as RadioButton).isChecked = true
            }
        }
        (questionScrollView as ScrollView).scrollY = 0
        btnNext.text = if (currentQuestionIndex == shuffledQuestionsList.size - 1) "TAPUSIN" else "SUNOD"
        btnBack.visibility = if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
    }

    private fun saveUserAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = findViewById<RadioButton>(selectedId)
            val selectedIndex = radioGroup.indexOfChild(radioButton)
            if (selectedIndex != -1) {
                userAnswers[currentQuestionIndex] = selectedIndex
            }
        }
    }

    private fun confirmFinishQuiz() {
        val unansweredCount = shuffledQuestionsList.size - userAnswers.size
        val message = if (unansweredCount > 0) {
            "Mayroon ka pang $unansweredCount na hindi nasasagutan. Sigurado ka bang nais mong tapusin?"
        } else {
            "Sigurado ka bang nais mong ipasa ang iyong sagot?"
        }
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_finish, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        val tvMessage: TextView = dialogView.findViewById(R.id.confirmMessage)
        val btnYes: Button = dialogView.findViewById(R.id.btnConfirmYes)
        val btnNo: Button = dialogView.findViewById(R.id.btnConfirmNo)
        tvMessage.text = message
        btnYes.setOnClickListener {
            dialog.dismiss()
            processAndSaveFinalResult(isPrematureExit = false)
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    // Central function to end the quiz, calculate score, and save it permanently.
    private fun processAndSaveFinalResult(isPrematureExit: Boolean) {
        timer?.cancel()
        var score = 0
        saveUserAnswer()

        for ((questionIndex, userAnswerIndex) in userAnswers) {
            if (questionIndex < shuffledQuestionsList.size) {
                val correctShuffledIndex = shuffledQuestionsList[questionIndex].newCorrectIndex
                if (userAnswerIndex == correctShuffledIndex) {
                    score++
                }
            }
        }

        // ⭐ BUG FIX IS HERE: The total number of items for the result screen should always be the full quiz size.
        val totalItemsForDisplay = shuffledQuestionsList.size

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_FINISHED, true)
            putInt(KEY_SCORE, score)
            // We save the total for consistency, even though it's always 30 now on display
            putInt(KEY_TOTAL_ITEMS, totalItemsForDisplay)
            apply()
        }

        headerCard.visibility = View.GONE
        questionScrollView.visibility = View.GONE
        buttonsLayout.visibility = View.GONE

        if (isPrematureExit) {
            finish()
        } else {
            showResultDialog(score, totalItemsForDisplay, false)
        }
    }

    private fun showResultDialog(score: Int, totalItems: Int, fromSavedState: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_result_card, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false) // User must click button to exit

        val tvScore: TextView = dialogView.findViewById(R.id.resultScoreText)
        val tvTitle: TextView = dialogView.findViewById(R.id.resultTitle)
        val btnBackToMenu: Button = dialogView.findViewById(R.id.btnBackToMenu)

        tvTitle.text = if (fromSavedState) "Nakaraang Resulta" else "Resulta ng Pagtataya"
        tvScore.text = "Ang iyong nakuha ay: $score / $totalItems"

        btnBackToMenu.setOnClickListener {
            dialog.dismiss()
            finish() // Simply finish this activity to return to the previous screen
        }
        dialog.show()
    }

    private fun showExitWarningDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exit_warning, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val btnConfirmExit: Button = dialogView.findViewById(R.id.btnExitConfirm)
        val btnCancel: Button = dialogView.findViewById(R.id.btnExitCancel)

        btnConfirmExit.setOnClickListener {
            dialog.dismiss()
            // This is a premature exit. Process, save, and finish the activity silently.
            processAndSaveFinalResult(isPrematureExit = true)
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    // This function is correct, no changes needed as long as numbers are removed from questions
    private fun loadAndShuffleQuestions() {
        val originalQuestions = mutableListOf<Question>()
        // ... (Your list of 30 questions without numbers goes here) ...
        originalQuestions.add(Question("Bakit tinawag na Mr. Big Shot ang pangunahing tauhan?",
            listOf("Wala siyang absent sa mga laro", "Wala siyang foul sa bawat laban", "Wala siyang mintis sa court", "Wala siyang travelling call"), 2))
        originalQuestions.add(Question("Ilang dipa na lang ang kaniyang layo sa court... Anong damdamin ng tauhan ang mahihinuha sa binasang pahayag?",
            listOf("nag-aalala", "nanghihinayang", "nagdaramdam", "natatakot"), 1))
        originalQuestions.add(Question("Alin sa sumusunod ang maaaring dahilan kung bakit urong-sulong ang pangunahing tauhan sa pagpunta sa basketball court?",
            listOf("Iniisip miya kung dadalo o hindi", "Iniisip niya kung maiiwan o hindi", "Iniisip niya kung pupunta o hindi", "Iniisip niya kung sasama o hindi"), 2))
        originalQuestions.add(Question("Alin sa sumusunod ang posisyon ng pangunahing tauhan sa paglalaro ng basketball?",
            listOf("blocker", "center", "forward", "guard"), 1))
        originalQuestions.add(Question("Bakit bumalik sa lumang basketball court ang pangunahing tauhan?",
            listOf("Gusto niyang balikan ang ugat ng kanyang mga pangarap.", "Hangad niyang makitang muli ang mga kinalakihang kalaro.", "Nais niyang maglaro ulit ng basketball sa kinalakihang lugar.", "Nasa niyang balikan ang alaalang dala ng kinalakihang lugar"), 0))
        originalQuestions.add(Question("Sa iyong palagay, ano ang pagkakaiba ng dagli sa maikling kwento?",
            listOf("Ang dagli ay isinulat nang mabilisan...", "Ang dagli ay mas maiksi kumpara sa maikling kwento.", "Ang Maikling kwento ay nababasa sa isang upuan...", "Ang Maikling kwento ay maraming tagpuan..."), 1))
        originalQuestions.add(Question("Sino ang abala sa pangunguha ng dahon ng saging at pagkakayod ng niyog na gagamitin sa Sumang-Yapos?",
            listOf("kababaihan", "kalalakihan", "kapitbahay", "kasambahay"), 1))
        originalQuestions.add(Question("Ano ang mamamalas sa tuwing gumagawa ng Sumang-Yapos ang mga taga-Mangumit?",
            listOf("Kaligayahan at Kawilihan", "Pagmamahal at Pagkakaisa", "Pangungulita at Kalungkutan", "Galit at Pagkainggit"), 1))
        originalQuestions.add(Question("Kung ikaw ay taga-Mangumit, gagawin mo rin ba ang nakagawiang tradisyon?",
            listOf("Oo sapagkat repleksyon ito ng identidad...", "Oo sapagkat nakikiayon ako sa mga gawain...", "Hindi sapagkat karagdagang gastos lamang...", "Hindi sapagkat wala namang mawawala..."), 0))
        originalQuestions.add(Question("Anong kakanin ang hindi nawawala sa hapag ng mga taga-Mangumit tuwing Undas?",
            listOf("Bananacue", "Karyoka", "Maruya", "Sumang-Yapos"), 3))
        originalQuestions.add(Question("Ano ang mensaheng nais ipahiwatig ng akda?",
            listOf("Ang pag-alala at pagpapakita ng pagmamahal...", "Ang pakikisabay sa nakagawiang tradisyon...", "Ang pag-aalay ng pagkain sa mga namayapa...", "Ang pagpapahalaga sa tradisyon ng Pilipinas."), 0))
        originalQuestions.add(Question("Alin sa mga sumusunod ang mga bahagi ng Sanaysay?",
            listOf("Gitna, konklusyon, wakas", "Simula, gitna, wakas", "Wakas, simula, ideya", "Konklusyon, gitna, nilalaman"), 1))
        originalQuestions.add(Question("Batay sa ikalawang saknong, ano ang ginawa ng mga ilustrado upang maisiwalat ang paghihirap ng mga Pilipino?",
            listOf("Sumulat ng awit...", "Sumulat ng mga akda na gumising sa mamamayan...", "Nagtayo ng mga paaralan...", "Nagtayo ng mga simbahan..."), 1))
        originalQuestions.add(Question("Relihiyong dala ng mga Espanyol",
            listOf("Budhismo", "Hinduismo", "Kristiyanismo", "Tauismo"), 2))
        originalQuestions.add(Question("Ano ang kabuoang mensahe ng tula?",
            listOf("Ang pilipinas ay nagpaalipin...", "Ang pakikipagsapalaran ng mga bayaning pilipino...", "Ang pananakop ng mga espanyol ay may magandang epekto...", "Ang pagiging duwag ng mga pilipino..."), 1))
        originalQuestions.add(Question("Ano ang kahulugan ng krus sa tula?",
            listOf("pagdurusa", "pagkamakabayan", "pananampalataya", "pagmamahal"), 2))
        originalQuestions.add(Question("Ano ang himig ng tula?",
            listOf("Pagpaparaya sa mga dayuhan.", "Paghihiganti sa mga kasamaang naranasan.", "Pakikipagpalaban para sa kalayaan ng bayan.", "Pakikiiisa sa mga dayuhan."), 2))
        originalQuestions.add(Question("Ano sukat ang kinabibilangan ng mga taludtod sa inasang tula?",
            listOf("wawaluhing pantig", "Lalabindalawahing pantig", "Lalabing-apating pantig", "Lalabingwaluhing pantig"), 1))
        originalQuestions.add(Question("Anong aspeto ng kultura ang binigyang-diin sa akda?",
            listOf("Pagtangkilik sa mga banyagang pagkain.", "Pagsuporta sa mga lokal na sining at tradisyon.", "Paglimot sa sariling wika.", "Pagsasagawa ng mga banyagang pagdiriwang."), 1))
        originalQuestions.add(Question("Ano ang mensahe ng akda tungkol sa mga desisyon ng mamimili?",
            listOf("Ang mga desisyon ay walang epekto...", "Ang mga desisyon ay may malalim na epekto...", "Ang mga desisyon ay dapat nakabatay sa presyo...", "Ang mga desisyon ay dapat ipasa sa ibang tao."), 1))
        originalQuestions.add(Question("Ano ang pangunahing layunin ng akdang \"Atin To!\"?",
            listOf("Upang ipakita ang mga depekto...", "Upang hikayatin ang mga tao na tangkilikin ang sariling produkto...", "Upang magpahayag ng suporta sa mga banyagang produkto.", "Upang talakayin ang mga isyu ng globalisasyon."), 1))
        originalQuestions.add(Question("Sino ang itinuturing na pag-asa ng bayan sa akdang \"Atin To!\"?",
            listOf("Ang mga matatanda.", "Ang mga kabataan.", "Ang mga negosyante.", "Ang mga banyagang turista."), 1))
        originalQuestions.add(Question("Ano ang epekto ng pagtangkilik sa lokal na produkto ayon sa akda?",
            listOf("Walang epekto sa ekonomiya.", "Nakakapagpababa ng kalidad ng produkto.", "Nakakatulong sa pag-unlad ng ekonomiya...", "Nagdudulot ng pagtaas ng presyo..."), 2))
        originalQuestions.add(Question("Anong uri ng wika ang ginamit sa talumpati?",
            listOf("Di pormal", "Masining", "Pormal", "Matayutay"), 2))
        originalQuestions.add(Question("Ano ang mensahe ng alamat ng Mangumit?",
            listOf("Huwag magtiwala sa kahit na kanino man.", "Ingatan ang mga ari-arian...", "Maging palaibigan...", "Laging bantayan ang bahay..."), 1))
        originalQuestions.add(Question("Makatwiran ba na iwan ang lugar kung hindi mo nararamdaman ang seguridad ng iyong ari-arian?",
            listOf("Oo, sapagkat hindi biro ang paghihirap...", "Oo, sapagkat mahirap magtiwala sa iba.", "Hindi, dahil marapat lamang na damayan ang iba.", "Hindi dahil hindi dahilan ang pagkawala..."), 0))
        originalQuestions.add(Question("Bakit nagtataka si Mang Kiko sa mga kaganapan sa kanilang sitio?",
            listOf("Dahil may diwata na nagpakita...", "Dahil umiyak si Aling Lisa...", "Sapagkat sunod-sunod ang nakawan...", "Sapagkat nawala ang Kalabaw..."), 2))
        originalQuestions.add(Question("Bakit tinaguriang Mang-umit ang sitio?",
            listOf("Dahil ninakaw ang pangalan...", "Dahil marami ang kaso ng nakawan...", "Sapagkat hindi umuunlad ang sitio...", "Sapagkat walang ibang hanapbuhay..."), 2))
        originalQuestions.add(Question("Anong emosyon ang nararamdaman ni Aling Lisa sa pagkawala ng kanilang ipon?",
            listOf("Masaya at inpirado", "Nalulungkot at nanghihinayang", "Nagagalit at naaawa", "Natatakot at nahihiwagaan"), 1))
        originalQuestions.add(Question("Alin sa mga sumusunod na pangungusap ang wastong pagkakagamit ng salita batay sa Alamat ng Mangumit?",
            listOf("Nalimas ang tubig baha...", "Ang mga inipong tubig para sa handaan...", "Nalimas ng mga magnanakaw ang perang inipon...", "Ang mga punongkahoy ay nalimas..."), 2))

        val randomizedQuestions = originalQuestions.shuffled()
        randomizedQuestions.forEach { question ->
            val correctAnswerText = question.options[question.correctIndex]
            val shuffledOptions = question.options.shuffled()
            val newCorrectIndex = shuffledOptions.indexOf(correctAnswerText)
            shuffledQuestionsList.add(
                ShuffledQuestion(
                    question.questionText,
                    shuffledOptions,
                    newCorrectIndex,
                    question.options
                )
            )
        }
    }
}
