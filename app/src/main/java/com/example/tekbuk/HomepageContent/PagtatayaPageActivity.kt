package com.example.tekbuk.HomepageContent

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.MainActivity
import com.example.tekbuk.R
import java.util.Locale
import java.util.concurrent.TimeUnit

class PagtatayaPageActivity : AppCompatActivity() {

    // Data Class for Question
    data class Question(
        val questionText: String,
        val options: List<String>,
        val correctIndex: Int // 0=A, 1=B, 2=C, 3=D
    )

    // UI Elements
    private lateinit var tvQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button // New Back Button
    private lateinit var tvCounter: TextView
    private lateinit var tvTimer: TextView
    private lateinit var instructionOverlay: FrameLayout
    private lateinit var btnStartQuiz: Button
    private lateinit var questionScrollView: ScrollView

    // Radio Buttons
    private lateinit var rbA: RadioButton
    private lateinit var rbB: RadioButton
    private lateinit var rbC: RadioButton
    private lateinit var rbD: RadioButton

    // Logic Variables
    private val questionsList = ArrayList<Question>()
    private val userAnswers = HashMap<Int, Int>() // Stores user answers (QuestionIndex -> SelectedOptionIndex)
    private var currentQuestionIndex = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagtataya_page) // Make sure XML name matches

        // Initialize Views
        initializeViews()

        // Load Data
        loadQuestionsData()

        // 1. Start Button Logic (Instruction Overlay)
        btnStartQuiz.setOnClickListener {
            instructionOverlay.visibility = View.GONE
            startTimer()
            displayQuestion()
        }

        // 2. Next / Finish Button Logic
        btnNext.setOnClickListener {
            saveUserAnswer() // Save selection before moving

            if (currentQuestionIndex < questionsList.size - 1) {
                // Go to Next Question
                currentQuestionIndex++
                displayQuestion()
            } else {
                // Finish Quiz
                confirmFinishQuiz()
            }
        }

        // 3. Back Button Logic
        btnBack.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveUserAnswer() // Save current selection before going back
                currentQuestionIndex--
                displayQuestion()
            }
        }
    }

    private fun initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion)
        radioGroup = findViewById(R.id.choicesRadioGroup)
        btnNext = findViewById(R.id.nextButton)
        // Note: You need to add a "Back" button in your XML or use a layout that supports two buttons at the bottom
        // For now, I will assume you might add a button with ID btnBack, or we can handle logic without it if strict.
        // *IMPORTANT*: If your XML only has one button, you might need to add a 'Previous' button in XML.
        // Assuming you added a button with id "backButton" in XML:
        btnBack = findViewById(R.id.backButton) // Make sure to add this ID in XML if not present

        tvCounter = findViewById(R.id.questionCounterText)
        tvTimer = findViewById(R.id.timerText)
        instructionOverlay = findViewById(R.id.instructionOverlay)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        questionScrollView = findViewById(R.id.questionScrollView)

        rbA = findViewById(R.id.rbOptionA)
        rbB = findViewById(R.id.rbOptionB)
        rbC = findViewById(R.id.rbOptionC)
        rbD = findViewById(R.id.rbOptionD)
    }

    private fun startTimer() {
        // 1 Hour 30 Minutes = 90 minutes = 5,400,000 milliseconds
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
                showResultDialog()
            }
        }.start()
    }

    private fun displayQuestion() {
        val currentQ = questionsList[currentQuestionIndex]

        // Update Counter
        tvCounter.text = "Tanong ${currentQuestionIndex + 1}/${questionsList.size}"

        // Update Text
        tvQuestion.text = currentQ.questionText
        rbA.text = currentQ.options[0]
        rbB.text = currentQ.options[1]
        rbC.text = currentQ.options[2]
        rbD.text = currentQ.options[3]

        // Clear previous selection visually
        radioGroup.clearCheck()

        // Restore saved answer if it exists
        if (userAnswers.containsKey(currentQuestionIndex)) {
            val savedAnswerIndex = userAnswers[currentQuestionIndex]
            when (savedAnswerIndex) {
                0 -> rbA.isChecked = true
                1 -> rbB.isChecked = true
                2 -> rbC.isChecked = true
                3 -> rbD.isChecked = true
            }
        }

        // Scroll to top
        questionScrollView.scrollY = 0

        // Button Text Logic
        if (currentQuestionIndex == questionsList.size - 1) {
            btnNext.text = "TAPUSIN"
        } else {
            btnNext.text = "SUNOD"
        }

        // Hide Back button on first question
        btnBack.visibility = if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
    }

    private fun saveUserAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            var selectedIndex = -1
            when (selectedId) {
                R.id.rbOptionA -> selectedIndex = 0
                R.id.rbOptionB -> selectedIndex = 1
                R.id.rbOptionC -> selectedIndex = 2
                R.id.rbOptionD -> selectedIndex = 3
            }
            userAnswers[currentQuestionIndex] = selectedIndex
        }
    }

    private fun confirmFinishQuiz() {
        // Check if all questions are answered
        val unansweredCount = questionsList.size - userAnswers.size

        val message = if (unansweredCount > 0) {
            "Mayroon ka pang $unansweredCount na hindi nasasagutan. Sigurado ka bang nais mong tapusin?"
        } else {
            "Sigurado ka bang nais mong ipasa ang iyong sagot?"
        }

        AlertDialog.Builder(this)
            .setTitle("Tapusin ang Pagtataya")
            .setMessage(message)
            .setPositiveButton("Oo") { _, _ -> showResultDialog() }
            .setNegativeButton("Hindi", null)
            .show()
    }

    private fun showResultDialog() {
        timer?.cancel()

        // Calculate Score
        var score = 0
        for (i in questionsList.indices) {
            val userAnswer = userAnswers[i]
            val correctAnswer = questionsList[i].correctIndex
            if (userAnswer == correctAnswer) {
                score++
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Resulta ng Pagtataya")
        builder.setMessage("Ang iyong nakuha ay: $score / ${questionsList.size}")
        builder.setCancelable(false)

        builder.setPositiveButton("Bumalik sa Menu") { dialog, _ ->
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Optional: Review Answers button logic could go here if desired

        val dialog = builder.create()
        dialog.show()
    }

    private fun loadQuestionsData() {
        // 1.C, 2.B, 3.C, 4.B, 5.A... etc (Matches your provided key)

        questionsList.add(Question("1. Bakit tinawag na Mr. Big Shot ang pangunahing tauhan?",
            listOf("Wala siyang absent sa mga laro", "Wala siyang foul sa bawat laban", "Wala siyang mintis sa court", "Wala siyang travelling call"), 2))

        questionsList.add(Question("2. Ilang dipa na lang ang kaniyang layo sa court... Anong damdamin ng tauhan ang mahihinuha sa binasang pahayag?",
            listOf("nag-aalala", "nanghihinayang", "nagdaramdam", "natatakot"), 1))

        questionsList.add(Question("3. Alin sa sumusunod ang maaaring dahilan kung bakit urong-sulong ang pangunahing tauhan sa pagpunta sa basketball court?",
            listOf("Iniisip miya kung dadalo o hindi", "Iniisip niya kung maiiwan o hindi", "Iniisip niya kung pupunta o hindi", "Iniisip niya kung sasama o hindi"), 2))

        questionsList.add(Question("4. Alin sa sumusunod ang posisyon ng pangunahing tauhan sa paglalaro ng basketball?",
            listOf("blocker", "center", "forward", "guard"), 1))

        questionsList.add(Question("5. Bakit bumalik sa lumang basketball court ang pangunahing tauhan?",
            listOf("Gusto niyang balikan ang ugat ng kanyang mga pangarap.", "Hangad niyang makitang muli ang mga kinalakihang kalaro.", "Nais niyang maglaro ulit ng basketball sa kinalakihang lugar.", "Nasa niyang balikan ang alaalang dala ng kinalakihang lugar"), 0))

        questionsList.add(Question("6. Sa iyong palagay, ano ang pagkakaiba ng dagli sa maikling kwento?",
            listOf("Ang dagli ay isinulat nang mabilisan...", "Ang dagli ay mas maiksi kumpara sa maikling kwento.", "Ang Maikling kwento ay nababasa sa isang upuan...", "Ang Maikling kwento ay maraming tagpuan..."), 1))

        questionsList.add(Question("7. Sino ang abala sa pangunguha ng dahon ng saging at pagkakayod ng niyog na gagamitin sa Sumang-Yapos?",
            listOf("kababaihan", "kalalakihan", "kapitbahay", "kasambahay"), 1))

        questionsList.add(Question("8. Ano ang mamamalas sa tuwing gumagawa ng Sumang-Yapos ang mga taga-Mangumit?",
            listOf("Kaligayahan at Kawilihan", "Pagmamahal at Pagkakaisa", "Pangungulita at Kalungkutan", "Galit at Pagkainggit"), 1))

        questionsList.add(Question("9. Kung ikaw ay taga-Mangumit, gagawin mo rin ba ang nakagawiang tradisyon?",
            listOf("Oo sapagkat repleksyon ito ng identidad...", "Oo sapagkat nakikiayon ako sa mga gawain...", "Hindi sapagkat karagdagang gastos lamang...", "Hindi sapagkat wala namang mawawala..."), 0))

        questionsList.add(Question("10. Anong kakanin ang hindi nawawala sa hapag ng mga taga-Mangumit tuwing Undas?",
            listOf("Bananacue", "Karyoka", "Maruya", "Sumang-Yapos"), 3))

        questionsList.add(Question("11. Ano ang mensaheng nais ipahiwatig ng akda?",
            listOf("Ang pag-alala at pagpapakita ng pagmamahal...", "Ang pakikisabay sa nakagawiang tradisyon...", "Ang pag-aalay ng pagkain sa mga namayapa...", "Ang pagpapahalaga sa tradisyon ng Pilipinas."), 0))

        questionsList.add(Question("12. Alin sa mga sumusunod ang mga bahagi ng Sanaysay?",
            listOf("Gitna, konklusyon, wakas", "Simula, gitna, wakas", "Wakas, simula, ideya", "Konklusyon, gitna, nilalaman"), 1))

        questionsList.add(Question("13. Batay sa ikalawang saknong, ano ang ginawa ng mga ilustrado upang maisiwalat ang paghihirap ng mga Pilipino?",
            listOf("Sumulat ng awit...", "Sumulat ng mga akda na gumising sa mamamayan...", "Nagtayo ng mga paaralan...", "Nagtayo ng mga simbahan..."), 1))

        questionsList.add(Question("14. Relihiyong dala ng mga Espanyol",
            listOf("Budhismo", "Hinduismo", "Kristiyanismo", "Tauismo"), 2))

        questionsList.add(Question("15. Ano ang kabuoang mensahe ng tula?",
            listOf("Ang pilipinas ay nagpaalipin...", "Ang pakikipagsapalaran ng mga bayaning pilipino...", "Ang pananakop ng mga espanyol ay may magandang epekto...", "Ang pagiging duwag ng mga pilipino..."), 1))

        questionsList.add(Question("16. Ano ang kahulugan ng krus sa tula?",
            listOf("pagdurusa", "pagkamakabayan", "pananampalataya", "pagmamahal"), 2))

        questionsList.add(Question("17. Ano ang himig ng tula?",
            listOf("Pagpaparaya sa mga dayuhan.", "Paghihiganti sa mga kasamaang naranasan.", "Pakikipagpalaban para sa kalayaan ng bayan.", "Pakikiiisa sa mga dayuhan."), 2))

        questionsList.add(Question("18. Ano sukat ang kinabibilangan ng mga taludtod sa inasang tula?",
            listOf("wawaluhing pantig", "Lalabindalawahing pantig", "Lalabing-apating pantig", "Lalabingwaluhing pantig"), 1))

        questionsList.add(Question("19. Anong aspeto ng kultura ang binigyang-diin sa akda?",
            listOf("Pagtangkilik sa mga banyagang pagkain.", "Pagsuporta sa mga lokal na sining at tradisyon.", "Paglimot sa sariling wika.", "Pagsasagawa ng mga banyagang pagdiriwang."), 1))

        questionsList.add(Question("20. Ano ang mensahe ng akda tungkol sa mga desisyon ng mamimili?",
            listOf("Ang mga desisyon ay walang epekto...", "Ang mga desisyon ay may malalim na epekto...", "Ang mga desisyon ay dapat nakabatay sa presyo...", "Ang mga desisyon ay dapat ipasa sa ibang tao."), 1))

        questionsList.add(Question("21. Ano ang pangunahing layunin ng akdang \"Atin To!\"?",
            listOf("Upang ipakita ang mga depekto...", "Upang hikayatin ang mga tao na tangkilikin ang sariling produkto...", "Upang magpahayag ng suporta sa mga banyagang produkto.", "Upang talakayin ang mga isyu ng globalisasyon."), 1))

        questionsList.add(Question("22. Sino ang itinuturing na pag-asa ng bayan sa akdang \"Atin To!\"?",
            listOf("Ang mga matatanda.", "Ang mga kabataan.", "Ang mga negosyante.", "Ang mga banyagang turista."), 1))

        questionsList.add(Question("23. Ano ang epekto ng pagtangkilik sa lokal na produkto ayon sa akda?",
            listOf("Walang epekto sa ekonomiya.", "Nakakapagpababa ng kalidad ng produkto.", "Nakakatulong sa pag-unlad ng ekonomiya...", "Nagdudulot ng pagtaas ng presyo..."), 2))

        questionsList.add(Question("24. Anong uri ng wika ang ginamit sa talumpati?",
            listOf("Di pormal", "Masining", "Pormal", "Matayutay"), 2)) // Assuming 24 is 25 in key? or separate. Kept logical.

        questionsList.add(Question("25. Ano ang mensahe ng alamat ng Mangumit?",
            listOf("Huwag magtiwala sa kahit na kanino man.", "Ingatan ang mga ari-arian...", "Maging palaibigan...", "Laging bantayan ang bahay..."), 1))

        questionsList.add(Question("26. Makatwiran ba na iwan ang lugar kung hindi mo nararamdaman ang seguridad ng iyong ari-arian?",
            listOf("Oo, sapagkat hindi biro ang paghihirap...", "Oo, sapagkat mahirap magtiwala sa iba.", "Hindi, dahil marapat lamang na damayan ang iba.", "Hindi dahil hindi dahilan ang pagkawala..."), 0))

        questionsList.add(Question("27. Bakit nagtataka si Mang Kiko sa mga kaganapan sa kanilang sitio?",
            listOf("Dahil may diwata na nagpakita...", "Dahil umiyak si Aling Lisa...", "Sapagkat sunod-sunod ang nakawan...", "Sapagkat nawala ang Kalabaw..."), 2))

        questionsList.add(Question("28. Bakit tinaguriang Mang-umit ang sitio?",
            listOf("Dahil ninakaw ang pangalan...", "Dahil marami ang kaso ng nakawan...", "Sapagkat hindi umuunlad ang sitio...", "Sapagkat walang ibang hanapbuhay..."), 2))

        questionsList.add(Question("29. Anong emosyon ang nararamdaman ni Aling Lisa sa pagkawala ng kanilang ipon?",
            listOf("Masaya at inpirado", "Nalulungkot at nanghihinayang", "Nagagalit at naaawa", "Natatakot at nahihiwagaan"), 1))

        questionsList.add(Question("30. Alin sa mga sumusunod na pangungusap ang wastong pagkakagamit ng salita batay sa Alamat ng Mangumit?",
            listOf("Nalimas ang tubig baha...", "Ang mga inipong tubig para sa handaan...", "Nalimas ng mga magnanakaw ang perang inipon...", "Ang mga punongkahoy ay nalimas..."), 2))
    }
}
