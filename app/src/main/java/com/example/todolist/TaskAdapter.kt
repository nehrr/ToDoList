package com.example.todolist

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.todolist.network.Task
import kotlinx.android.synthetic.main.taskitem.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class TaskAdapter(var taskList: List<Task>, val onClickClose: (Int, Boolean) -> Unit): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.taskitem, parent, false))
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = taskList[position]
        holder.bind(current)
    }

    inner class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(task: Task) {
            Log.d("TASK", task.toString())
            itemView.task?.text = task.content
            itemView.date?.text = parseDate(task.created)
            itemView.due_date?.text = task.due?.date ?: "No due date"
            itemView.task?.isChecked = task.completed
            itemView.task?.strikeThrough = task.completed
            itemView.date?.strikeThrough = task.completed
            itemView.due_date?.strikeThrough = task.completed
        }

        var taskItemView: TextView? = null

        fun parseDate(date: String) : String {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            var parsedDate = LocalDate.parse(date, inputFormatter)
            var formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            return parsedDate.format(formatter)
        }

        init {
            taskItemView = itemView.findViewById(R.id.task)
            itemView.task.setOnCheckedChangeListener { buttonView, isChecked ->
                onClickClose(adapterPosition, isChecked)
                buttonView.strikeThrough = isChecked
            }
        }

        private var TextView.strikeThrough
            get() = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG > 0
            set(value) {
                paintFlags = if (value)
                    paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else
                    paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

    }
}