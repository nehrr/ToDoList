package com.example.todolist.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.android.todolist.network.TaskDate

import com.example.todolist.R
import kotlinx.android.synthetic.main.fragment_add_task_dialog.*
import kotlinx.android.synthetic.main.fragment_add_task_dialog.view.*
import java.util.*


class AddTaskDialogFragment(val onFinish: (String, TaskDate?) -> Unit) : DialogFragment() {
    private var date : TaskDate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_task_dialog, container, false)
        view.button.setOnClickListener {
            val newFragment = DatePickerFragment { dateString ->
                date = TaskDate(dateString)
            }
            newFragment.show(activity!!.supportFragmentManager, "datePicker")
        }

        view.buttonOk.setOnClickListener {
            onFinish(editText.text.toString(), date)
            dismiss()
        }

        view.cancel.setOnClickListener {
            dismiss()
        }

        return view
    }

}


class DatePickerFragment(val onResult: (String) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        var monthFormatted = String.format("%02d", month)
        var dayFormatted = String.format("%02d", day)
        onResult("$year-$monthFormatted-$dayFormatted")
    }
}
