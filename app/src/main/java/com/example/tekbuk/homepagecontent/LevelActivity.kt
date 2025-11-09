package com.example.tekbuk.homepagecontent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tekbuk.R
class LevelActivity : AppCompatActivity() {

    private var paksaId: String? = null
    private var level: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gawain_page) // create a simple layout activity_level.xml

        // read extras passed from GawainPageActivity
        paksaId = intent.getStringExtra("paksa_id")
        level = intent.getIntExtra("level", 1)

        // show paksa/level in the UI if you want
        findViewById<TextView>(R.id.paksaTitle)?.text = "Paksa: ${paksaId ?: "?"} — Level: $level"

        // Example Finish button: when user completes the level, return result to caller
        findViewById<Button>(R.id.btnFinishLevel)?.setOnClickListener {
            // inside LevelActivity when user finishes level N successfully:
            val resultIntent = Intent().apply {
                putExtra("paksa_id", paksaId)
                putExtra("level_completed", level) // e.g., 1
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}