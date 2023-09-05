package com.yndoo.diet_memo

import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yndoo.diet_memo.databinding.ActivityMainBinding
import com.yndoo.diet_memo.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth: FirebaseAuth
    private var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        auth = Firebase.auth

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
                .into(binding.userImg)
        }.addOnFailureListener {
            //파일 다운로드 실패
        }


        //이미지 불러오기 버튼
        binding.setImgBtn.setOnClickListener {
            //갤러리 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResult.launch(intent)
            flag = false
        }
    }

    //결과 가져오기
    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){

        //결과 코드 OK이고 값이 null이 아니면
        if(it.resultCode == RESULT_OK && it.data != null){
            //값 담기
            val uri = it.data!!.data

            //이미지 뷰에 보여주기
            Glide.with(this)
                .load(uri) //이미지
                .placeholder(R.drawable.icon_profile)
                .into(binding.userImg) //보여줄 위치
            Log.d("uri 값은?", uri.toString())

            val storage = Firebase.storage //인스턴스 생성
            val storageRef = storage.getReference("image") //storage 참조
            val filename = auth.currentUser!!.uid
            val imageRef = storageRef.child("${filename}.png")

            val uploadTask = imageRef.putFile(uri!!)
            binding.progressBar.isVisible = true

            uploadTask.addOnSuccessListener { taskSnapshot ->
                //파일 업로드 성공
                Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_LONG).show()
                flag = true
                binding.progressBar.isVisible = false
            }.addOnFailureListener{
                //파일 업로드 실패
                Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode === KeyEvent.KEYCODE_BACK) {
            //뒤로 가기 버튼 눌렀을 때
            if (flag == true) {
                val myintent = Intent(this, MainActivity::class.java)
                startActivity(myintent)
                return super.onKeyDown(keyCode, event)
            } else {
                Toast.makeText(this, "사진 업로드 성공 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return false
    }
}