package com.yndoo.diet_memo

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.play.integrity.internal.m
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yndoo.diet_memo.databinding.ActivityMainBinding
import java.net.URI
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    val dataModelList = mutableListOf<DataModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = Firebase.auth

        val database = Firebase.database
        val myRef = database.getReference("myMemo")

        val listView = binding.mainLV
        val myadapter = ListViewAdapter(dataModelList)
        listView.adapter = myadapter


        val storage = Firebase.storage //storage 인스턴스 생성
        val storageRef = storage.getReference("image") //storage 참조
        val filename = auth.currentUser!!.uid
        val imageRef = storageRef.child("${filename}.png")

        //storage에서 이미지 가져와서 보여주기
        val downloadTask = imageRef.downloadUrl
        downloadTask.addOnSuccessListener { uri ->
            //파일 다운로드 성공
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.icon_profile)
                .into(binding.userProfile)
            binding.userProfile.setBackgroundResource(R.drawable.radius)
        }.addOnFailureListener {
            //파일 다운로드 실패
        }


        // listView에 넣을 메모 목록 Firebase에서 가져오기
        // .orderByChild("date") <<를 추가하여 날짜순 정렬
        myRef.child(Firebase.auth.currentUser!!.uid).child("memo").orderByChild("date").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                dataModelList.clear()

                //snapshot에는 전체가 들어있음
                for(dataModel in snapshot.children){
                    Log.d("Data", dataModel.toString())

                    dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
                myadapter.notifyDataSetChanged()

                Log.d("DataList", dataModelList.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        // 메모 작성 다이얼로그 띄우기
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
            
            // 다이얼로그의 저장 버튼 클릭시
            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener {
                val workoutMemo = mAlertDialog.findViewById<EditText>(R.id.workoutMemo)?.text.toString()
                val dietMemo = mAlertDialog.findViewById<EditText>(R.id.dietMemo)?.text.toString()

                val model = DataModel(dateText, workoutMemo, dietMemo)

                val database = Firebase.database
                val myRef = database.getReference("myMemo").child(Firebase.auth.currentUser!!.uid).child("memo")

                myRef.push().setValue(model)

                // 저장했으면 다이얼로그 닫기
                mAlertDialog.dismiss()
            }
        }

        binding.settingBtn.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}