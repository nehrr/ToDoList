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
            showAddItemDialog {
                if (it != null && it.isNotBlank()) {
                    try {
                        launch {
                            addTask(it)
                        }
                    } catch (e: Exception) {
                        Log.e("err", e.toString())
                    }
                }
                view.isEnabled = true
            }
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

    suspend fun addTask(content: String) {
        val res = taskService.addTask(Task("", content, false, "", "")).await()
        if (res.id != "") {
            taskList.add(res)
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun showAddItemDialog(onFinish: (String?) -> Unit) {
        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val dialogView = inflater.inflate(R.layout.dialog, null)

        val editText = dialogView.findViewById<EditText>(R.id.editText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ -> onFinish(editText?.text.toString()) }
            .setNegativeButton("Cancel") { _, _ -> onFinish(null) }
            .create()
        dialog.show()
    }

    fun onResult(date: String) : String {
        return date
    }

    fun showDatePicker(view: View) {
        Log.d("TEST", "push push dah button")
        val newFragment = DatePickerFragment(this::onResult)
        newFragment.show(supportFragmentManager, "datePicker")
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

class DatePickerFragment(val onResult: (String) -> (String)) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onResult("$year $month $day")
    }
}
