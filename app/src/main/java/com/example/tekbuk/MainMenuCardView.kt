package com.example.tekbuk

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class MainMenuCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private val titleView: TextView
    private val descriptionView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_main_menu_card, this, true)

        imageView = findViewById(R.id.cardImage)
        titleView = findViewById(R.id.cardTitle)
        descriptionView = findViewById(R.id.cardDescription)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MainMenuCard)
            imageView.setImageResource(
                typedArray.getResourceId(R.styleable.MainMenuCard_cardImage, R.drawable.pagtataya)
            )
            titleView.text = typedArray.getString(R.styleable.MainMenuCard_cardTitle) ?: "Title"
            descriptionView.text = typedArray.getString(R.styleable.MainMenuCard_cardDescription) ?: ""
            typedArray.recycle()
        }
    }

    fun setCardData(imageRes: Int, title: String, description: String) {
        imageView.setImageResource(imageRes)
        titleView.text = title
        descriptionView.text = description
    }
}
