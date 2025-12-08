package com.example.tekbuk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.HomapageContent.GawainPageActivity
import com.example.tekbuk.HomapageContent.MarkaPageActivity
import com.example.tekbuk.HomapageContent.PagtatayaPageActivity
import com.example.tekbuk.HomapageContent.PaksaPageActivity
import com.example.tekbuk.HomapageContent.RepleksyonPageActivity
import com.example.tekbuk.R
import com.example.tekbuk.model.MainMenuItem

class MainMenuAdapter(
    private val context: Context,
    private val menuList: List<MainMenuItem>
) : RecyclerView.Adapter<MainMenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.cardImage)
        val tvTitle: TextView = itemView.findViewById(R.id.cardTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.cardDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_menu_card, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        holder.imgIcon.setImageResource(item.imageResId)
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description

        // ✅ Handle click (case-insensitive)
        holder.itemView.setOnClickListener {
            when (item.title.uppercase()) {
                "PAKSA" -> context.startActivity(Intent(context, PaksaPageActivity::class.java))
                "GAWAIN" -> context.startActivity(Intent(context, GawainPageActivity::class.java))
                "PAGTATAYA" -> context.startActivity(Intent(context, PagtatayaPageActivity::class.java))
                "REPLEKSYON" -> context.startActivity(Intent(context, RepleksyonPageActivity::class.java))
                "MARKA / ISKOR" -> context.startActivity(Intent(context, MarkaPageActivity::class.java))
            }
        }
    }

    override fun getItemCount(): Int = menuList.size
}
