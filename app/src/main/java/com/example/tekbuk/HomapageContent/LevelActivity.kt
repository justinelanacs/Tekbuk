package com.example.tekbuk.HomapageContent

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

    }
}