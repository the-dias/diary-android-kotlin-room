package com.example.diary

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.diary.database.Database
import com.example.diary.database.Todo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import org.json.JSONObject
import java.util.*


class TodoActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        val editTextDate: EditText = findViewById(R.id.editTextDate)
        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, mYear, monthOfYear, dayOfMonth ->
                    val date = (dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + mYear)
                    editTextDate.setText(date)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }


        val editTimeStart: EditText = findViewById(R.id.editTextTimeStart)
        val editTimeFinish: EditText = findViewById(R.id.editTextTimeFinish)

        val timePickerStart = TimePicker(this)
        val timePickerFinish = TimePicker(this)

        setOnClickListenerTimePicker(this@TodoActivity, timePickerStart, editTimeStart)
        setOnClickListenerTimePicker(this@TodoActivity, timePickerFinish, editTimeFinish)

        val buttonCreate = findViewById<Button>(R.id.buttonCreate)
        buttonCreate.setOnClickListener {
            val editTextName = findViewById<EditText>(R.id.editTextTextNameTodo)
            val editTextDescription = findViewById<EditText>(R.id.editTextTextMultiLineDescription)

            val name = editTextName.text.toString()
            val description = editTextDescription.text.toString()
            val timeStart: Long = getTimeInMillisecond(timePickerStart.minute, timePickerStart.hour)
            val timeFinish: Long =
                getTimeInMillisecond(timePickerFinish.minute, timePickerFinish.hour)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = Room.databaseBuilder(
                    applicationContext,
                    Database::class.java, "todo"
                ).fallbackToDestructiveMigration().build()


                val todoDao = db.todoDao()
                val todo = Todo(
                    name = name,
                    description = description,
                    dateStart = timeStart,
                    dateFinish = timeFinish,
                    date = editTextDate.text.toString(),
                )


                todo.json = todo.id?.let { it1 ->
                    generateJson(
                        id = it1,
                        name = todo.name,
                        description = todo.description,
                        timeStart = todo.dateStart,
                        timeFinish = todo.dateFinish,
                        date = editTextDate.text.toString()
                    ).toString()
                }

                todoDao.insert(todo)
            }
            Toast.makeText(this@TodoActivity, "Todo Created", Toast.LENGTH_SHORT).show()

        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun setTimePicker(context: Context, timePicker: TimePicker, editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minutes = calendar[Calendar.MINUTE]

        TimePickerDialog(
            context,
            { _, mHour, mMinute ->
                editText.setText("$mHour:$mMinute")
                timePicker.minute = mMinute
                timePicker.hour = mHour
            }, hour, minutes, true
        ).show()

    }

    private fun generateJson(
        id: Int,
        name: String,
        description: String,
        timeStart: Long,
        timeFinish: Long,
        date: String
    ): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("description", description)
        json.put("time_finish", timeStart)
        json.put("time_start", timeFinish)
        json.put("date", date)

        return json
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setOnClickListenerTimePicker(
        context: Context,
        timePicker: TimePicker,
        editText: EditText,
        is24HourView: Boolean = true
    ) {
        editText.setOnClickListener {
            timePicker.setIs24HourView(is24HourView)
            setTimePicker(context, timePicker, editText)
        }
    }

     @TestOnly
     fun getTimeInMillisecond(minute: Int, hour: Int): Long {
        return minute * 60000L + hour * 60 * 60000L
    }
}
