package com.yndoo.diet_memo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.play.integrity.internal.m
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yndoo.diet_memo.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.writeBtn.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("운동 메모 다이얼로그")
            val mAlertDialog = mBuilder.show()

            var dateText = ""

            val DateSetBtn = mAlertDialog.findViewById<Button>(R.id.dateSelectBtn)

            DateSetBtn?.setOnClickListener {

                val today = GregorianCalendar()
                val year: Int = today.get(Calendar.YEAR)
                val month: Int = today.get(Calendar.MONTH)
                val date: Int = today.get(Calendar.DATE)

                val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int
                    ) {
                        //month는 +1 해야 함
                        Log.d("MAIN", "${year}, ${month+1}, ${dayOfMonth}")

                        DateSetBtn.setText("${year}/${month+1}/${dayOfMonth}")
                        dateText = "${year}/${month+1}/${dayOfMonth}"
                    }

                }, year, month, date)
                dlg.show()
            }
            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener {
                val dietMemo = mAlertDialog.findViewById<EditText>(R.id.dietMemo)?.text.toString()

                val model = DataModel(dateText, dietMemo)

                val database = Firebase.database
                val myRef = database.getReference("myMemo")

                myRef.push().setValue(model)

                mAlertDialog.dismiss()
            }
        }
    }
}