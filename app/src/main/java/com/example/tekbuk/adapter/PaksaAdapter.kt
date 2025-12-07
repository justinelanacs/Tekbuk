package com.example.tekbuk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.R
import com.example.tekbuk.model.PaksaItem
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.tekbuk.HomepageContent.PaksaPageActivity
import com.example.tekbuk.PaksaContent.*

class PaksaAdapter(
    private val context: Context,
    private val items: MutableList<PaksaItem>
) : RecyclerView.Adapter<PaksaAdapter.PaksaViewHolder>() {

    inner class PaksaViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.titleText)
        val descText: TextView = view.findViewById(R.id.descText)
        val iconImage: ImageView = view.findViewById(R.id.iconImage)
        val progressCircle: CircularProgressIndicator = view.findViewById(R.id.progressCircle)
        val progressPercent: TextView = view.findViewById(R.id.progressPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaksaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paksa, parent, false)
        return PaksaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaksaViewHolder, position: Int) {
        val item = items[position]

        holder.titleText.text = item.title
        holder.descText.text = item.description
        holder.iconImage.setImageResource(item.iconRes)
        holder.progressCircle.progress = item.progress
        holder.progressPercent.text = "${item.progress}%"

        holder.view.setOnClickListener {
            val intent = when (item.title) {
                "TULA" -> Intent(context, TulaContent::class.java)
                "SANAYSAY" -> Intent(context, SanaysayContent::class.java)
                "DAGLI" -> Intent(context, DagliContent::class.java)
                "TALUMPATI" -> Intent(context, TalumpatiContent::class.java)
                "KWENTONG BAYAN" -> Intent(context, KwentongbayanContent::class.java)
                else -> null
            }

            intent?.putExtra("paksa_index", position)

            if (intent != null && context is PaksaPageActivity) {
                BaseContentActivity.scrollListener = context // ✅ correct way
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
