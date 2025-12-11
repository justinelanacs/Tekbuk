package com.example.tekbuk.HomepageContent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.R

class StudentAdapter(
    private val studentList: List<StudentResult>,
    private val onItemClick: (StudentResult) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvSection: TextView = itemView.findViewById(R.id.tvSection)
        val tvScore: TextView = itemView.findViewById(R.id.tvTotalScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val currentItem = studentList[position]
        holder.tvName.text = currentItem.studentName
        holder.tvSection.text = currentItem.section
        holder.tvScore.text = "${currentItem.total_score} pts"

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount() = studentList.size
}
