package com.example.todolist

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.todolist.network.Task
import com.example.android.todolist.network.taskService

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.DividerItemDecoration

import java.util.*
import android.view.LayoutInflater
import com.example.android.todolist.network.TaskDate
import com.example.todolist.view.AddTaskDialogFragment
import com.example.todolist.view.DatePickerFragment


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val taskList = mutableListOf<Task>()
    private lateinit var recyclerView: RecyclerView
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout?.setOnRefreshListener {
            try {
                launch {
                    getTasks()
                }
            } catch (e: Exception) {
                Log.e("err", e.toString())
            }
            swipeRefreshLayout?.isRefreshing = false
        }

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.adapter = TaskAdapter(taskList, this::onDeleteItem)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemDecor = DividerItemDecoration(this, HORIZONTAL)
        recyclerView.addItemDecoration(itemDecor)

        try {
            launch {
                getTasks()
            }
        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

        fab.setOnClickListener { view ->
            view.isEnabled = false
            showAddItemDialog()
        }
    }

    fun onDeleteItem(position: Int, checked: Boolean) {
        try {
            launch {
                try {
                    if (checked) {
                        taskService.closeTask(taskList[position].id).await()
                    } else {
                        taskService.openTask(taskList[position].id).await()
                    }
                } catch (e: Exception) {
                    Log.e("err", e.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }


    suspend fun getTasks() {
        val res = taskService.getTasks().await()
        taskList.clear()
        taskList.addAll(res)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    suspend fun addTask(content: String, date: TaskDate? = null) {
        val task = Task("", content, false, "", date)
        val res = taskService.addTask(task).await()
        if (res.id != "") {
            taskList.add(res)
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun showAddItemDialog() {
        AddTaskDialogFragment(this::onFinish).show(supportFragmentManager, "ADD_TASK_DIALOG")
    }

    fun onFinish(content: String, date: TaskDate?) {
        if (content.isNotBlank()) {
            try {
                launch {
                    addTask(content, date)
                }
            } catch (e: Exception) {
                Log.e("err", e.toString())
            }
        }
        fab.isEnabled = true
    }
}
