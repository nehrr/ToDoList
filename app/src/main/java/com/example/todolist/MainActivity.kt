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
        Log.d("RES", res.toString())
        taskList.clear()
        taskList.addAll(res)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    suspend fun addTask(content: String, date: TaskDate? = null) {
        val task = Task("", content, false, "", date)
        Log.d("TASK", task.toString())
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
        Log.d("date", date.toString())
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


    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_checked -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    */
}
