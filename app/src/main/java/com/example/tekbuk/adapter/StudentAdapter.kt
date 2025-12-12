package com.example.tekbuk.HomepageContent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tekbuk.R

class StudentAdapter(
    private val students: List<StudentResult>,
    private val onItemClicked: (StudentResult) -> Unit,
    // Add a new listener specifically for the delete action
    private val onDeleteClicked: (StudentResult) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
    }

    override fun getItemCount(): Int = students.size

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvStudentName)
        private val sectionTextView: TextView = itemView.findViewById(R.id.tvSection)
        private val scoreTextView: TextView = itemView.findViewById(R.id.tvTotalScore)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.ivDeleteStudent) // Get the delete icon

        fun bind(student: StudentResult) {
            nameTextView.text = student.studentName
            sectionTextView.text = student.section
            scoreTextView.text = "${student.total_score} pts"

            // Set the click listener for the whole item
            itemView.setOnClickListener {
                onItemClicked(student)
            }

            // Set the click listener specifically for the delete icon
            deleteIcon.setOnClickListener {
                onDeleteClicked(student)
            }
        }
    }
}
