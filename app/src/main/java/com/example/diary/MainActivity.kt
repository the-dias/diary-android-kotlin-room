package com.example.diary

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.gridlayout.widget.GridLayout
import androidx.room.Room
import com.example.diary.database.Database
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity()
{
    private var removeListView: MutableList<CardView> = mutableListOf()

    companion object{
        private var countRows = 0
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        gridLayout.orientation = GridLayout.VERTICAL



        val maxHours = 23
        gridLayout.rowCount = maxHours * 2
        gridLayout.setBackgroundColor(Color.BLACK)

        initGrid(gridLayout)

        val calendar: Calendar = Calendar.getInstance()
        val dateFormat = "dd.MM.yyyy"

        val textViewDate: TextView = findViewById(R.id.textViewDate)
        textViewDate.text = dateFormat

        val buttonSelectDate: Button = findViewById(R.id.buttonSelectDate)
        buttonSelectDate.setOnClickListener{
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)


            val datePickerDialog = DatePickerDialog(
                this,
                { _, mYear, monthOfYear, dayOfMonth ->
                    val date = (dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + mYear)
                    textViewDate.text = date
                    removeView(gridLayout)  // remove prev view
                    loadData(gridLayout, data = date) // update data
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButtonAddTodo).setOnClickListener{
            val intent = Intent(this@MainActivity, TodoActivity::class.java)
            startActivity(intent)
        }
    }

    // set hours row
    @SuppressLint("SetTextI18n")
    private fun initGrid(gridLayout: GridLayout) {
        val minHours = 0
        val maxHours = 23
        for (row in minHours until maxHours) {

            val textView = setTexView(this@MainActivity)
            textView.text =
                "${normalizeTimeString(row)}:00 - ${normalizeTimeString(row + 1)}:00 "
            textView.id = countRows
            textView.setPadding(40, 40, 40, 200)
            textView.setTextColor(Color.WHITE)

            gridLayout.addView(textView)
            gridLayout.minimumWidth = resources.displayMetrics.widthPixels

            countRows++

        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun loadData(gridLayout: GridLayout, data: String) {
        GlobalScope.launch {
            val db = Room.databaseBuilder(
                applicationContext,
                Database::class.java, "todo"
            ).fallbackToDestructiveMigration().build()

            val todoDao = db.todoDao()
            val todoLists = todoDao.getAll()

            runOnUiThread {

                for (todoList in todoLists) {
                   // println("~~~~~ ${todoList.id} ~~~~~")
                    //println(todoList.id.toString() + " " + todoList.name)
                    val timeStart: Double = todoList.dateStart / 3600000.0
                    val timeFinish: Double = todoList.dateFinish / 3600000.0

                    val cardView: CardView = setCardView(
                        context = this@MainActivity,
                        name = todoList.name,
                        startTime = formatMinutesHoursFromMS(todoList.dateStart),
                        finishTime = formatMinutesHoursFromMS(todoList.dateFinish)
                    )

                    val rowStart: Int = checkHourRange(timeStart)
                    val rowFinish: Int = checkHourRange(timeFinish)
                   // println("rowStart: $rowStart")
                    // println("tableRow.id ${textView.id}")
                    //println("rowFinish: $rowFinish")


                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.rowSpec = GridLayout.spec(rowStart, rowFinish, GridLayout.TOP)
                    layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT

                    val ratioHeightSpan = if ((abs(rowFinish - rowStart)) == 0) 80
                    else (abs(rowFinish - rowStart) * 80).toFloat()

                    layoutParams.height = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        ratioHeightSpan.toFloat(),
                        resources.displayMetrics
                    ).toInt()

                    layoutParams.leftMargin = 20
                    layoutParams.rightMargin = 20
                    layoutParams.topMargin = 20
                    layoutParams.bottomMargin = 20

                    println("``````${todoList.date} == ${data}````````````````")



                    if (todoList.date == data) {
                        removeListView.add(cardView)
                        cardView.setOnClickListener{
                            Toast.makeText(this@MainActivity, todoList.name, Toast.LENGTH_SHORT).show()
                        }
                        gridLayout.addView(cardView, layoutParams)
                    }
                }
            }
        }
    }

    private fun removeView(gridLayout: GridLayout) {
        for(view in removeListView){
            gridLayout.removeView(view)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCardView(context: Context, name: String, startTime: String, finishTime: String): CardView{
        val cardView = CardView(context)
        cardView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        cardView.id = View.generateViewId()
        cardView.setBackgroundColor(Color.MAGENTA)

        cardView.minimumWidth = 200

        val textNameView = TextView(context)
        textNameView.id = View.generateViewId()
        textNameView.text = name
        textNameView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        val textTimeStartView = TextView(context)
        textTimeStartView.id = View.generateViewId()
        textTimeStartView.text = "from: $startTime"
        textTimeStartView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        val textTimeFinishView = TextView(context)
        textTimeFinishView.text = "to: $finishTime"
        textTimeFinishView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        val linearLayout = LinearLayout(context)
        linearLayout.id = View.generateViewId()
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(textNameView)
        linearLayout.addView(textTimeStartView)
        linearLayout.addView(textTimeFinishView)

        cardView.addView(linearLayout)
        return cardView
    }

    @SuppressLint("SetTextI18n")
    private fun setTexView(context: Context): TextView{
        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.setTextColor(Color.BLACK)

        return textView
    }
    @TestOnly
    fun normalizeTimeString(time: Int): String{
        if(time >= 10){
            return "$time"
        }
        return "0$time"
    }
    private fun checkHourRange(hour: Double): Int{  // check time range and get row number
        return when(hour){
            in 0.0 .. 1.0 -> 0 // row number
            in 1.1 .. 2.0 -> 1
            in 2.1 .. 3.0 -> 2
            in 3.1 .. 4.0 -> 3
            in 4.1 .. 5.0 -> 4
            in 5.1 .. 6.0 -> 5
            in 6.1 .. 7.0 -> 6
            in 7.1 .. 8.0 -> 7
            in 8.1 .. 9.0 -> 8
            in 9.1 .. 10.0 -> 9
            in 10.1 .. 11.0 -> 10
            in 11.1 .. 12.0 -> 11
            in 12.1 .. 13.0 -> 12
            in 13.1 .. 14.0 -> 13
            in 14.1 .. 15.0 -> 14
            in 15.1 .. 16.0 -> 15
            in 16.1 .. 17.0 -> 16
            in 17.1 .. 18.0 -> 17
            in 18.1 .. 19.0 -> 18
            in 19.1 .. 20.0 -> 19
            in 20.1 .. 21.0 -> 20
            in 21.1 .. 22.0 -> 21
            in 22.1 .. 23.0 -> 22

            else -> {
                23
            }
        }
    }
    private fun formatMinutesHoursFromMS(time: Long): String{
        var minutes = (time / 60000).toDouble().roundToInt()
        val hours = (minutes / 60)
        minutes %= 60

        var strMin: String = minutes.toString()

        if (minutes < 10)
            strMin = ("0$minutes")
        return "$hours:$strMin"
    }
}







