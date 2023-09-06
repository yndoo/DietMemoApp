package com.yndoo.diet_memo

import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yndoo.diet_memo.databinding.ActivityMainBinding
import com.yndoo.diet_memo.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth: FirebaseAuth
    private var flag = true //데이터베이스 저장 중에 이동하는 일을 방지하기 위한 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        auth = Firebase.auth

        val database = Firebase.database
        val myRef = database.getReference("myMemo")

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
        //사용자 닉네임 불러오기
        myRef.child(auth.currentUser!!.uid).child("nickname").get().addOnSuccessListener {result ->
            binding.userName.setText(result.value.toString())
        }

        //이메일계정인지?
        myRef.child(auth.currentUser!!.uid).child("email").get().addOnSuccessListener {result ->
            if(result.value!=null){ //email계정있으면
                //Log.d("이메일 데이터가 없어도 null이 안뜨는것같다",result.toString())
                binding.userEmail.text = result.value.toString()
                binding.emailLoginBtn.visibility = View.GONE
            }else if(result.value==null){ //email계정없으면
                binding.userEmail.text = "익명 계정"
            }
        }

        //이미지 불러오기 버튼 클릭 시
        binding.setImgBtn.setOnClickListener {
            //갤러리 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResult.launch(intent)
            flag = false
        }


        //닉네임 변경 버튼 클릭 시
        binding.setNameBtn.setOnClickListener {
            binding.nameArea.visibility = View.VISIBLE
            binding.nameOkBtn.visibility = View.VISIBLE

            binding.nameOkBtn.setOnClickListener {
                flag = false
                val nametext = binding.nameArea.text.toString()
                if(nametext != null){
                    //닉네임 저장, 다시 GONE으로 바꾸기
                    myRef.child("${auth.currentUser!!.uid}").child("nickname").setValue(nametext)
                    binding.userName.text = nametext
                    binding.nameArea.visibility = View.GONE
                    binding.nameOkBtn.visibility = View.GONE
                    Toast.makeText(this, "닉네임을 변경 성공", Toast.LENGTH_SHORT).show()
                    flag = true
                }else{
                    Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show()
                }
            }
        }

        //계정 연동(영구 계정 변환)
        val emailLogin = binding.emailLoginBtn
        emailLogin.setOnClickListener {
            val intent = intent
            val emailLink = intent.data.toString()
            val emailArea = binding.emailArea
            val pwArea = binding.pwArea
            val Ok = binding.linkOkBtn
            emailArea.visibility = View.VISIBLE
            Ok.visibility = View.VISIBLE
            pwArea.visibility = View.VISIBLE
            Ok.setOnClickListener {
                flag = false
                val email = emailArea.text.toString()
                val password = pwArea.text.toString()
                val credential = EmailAuthProvider.getCredential(email, password)
                auth.currentUser!!.linkWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d("ㅇㅇ", "linkWithCredential:success")
                            Toast.makeText(baseContext, "계정 연동 성공", Toast.LENGTH_SHORT,).show()
                            myRef.child("${auth.currentUser!!.uid}").child("email").setValue(email)

                            binding.userEmail.text = email
                            emailArea.visibility = View.GONE
                            Ok.visibility = View.GONE
                            pwArea.visibility = View.GONE
                            emailLogin.visibility = View.GONE

                            val user = task.result?.user
                            flag = true
                        } else {
                            Log.w("ㄴㄴ", "linkWithCredential:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            } //Ok
        } //emailLogin
/*        //로그아웃 버튼 클릭 시
        binding.logout.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(baseContext, "로그아웃 성공", Toast.LENGTH_SHORT,).show()
        }*/
    }//OnCreate

    //이미지 설정 결과 가져오기
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
            //Log.d("uri 값은?", uri.toString())

            val storage = Firebase.storage //인스턴스 생성
            val storageRef = storage.getReference("image") //storage 참조
            val filename = auth.currentUser!!.uid
            val imageRef = storageRef.child("${filename}.png")

            val uploadTask = imageRef.putFile(uri!!)
            binding.progressBar.isVisible = true

            uploadTask.addOnSuccessListener { taskSnapshot ->
                //파일 업로드 성공
                binding.progressBar.isVisible = false
                Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_LONG).show()
                flag = true

            }.addOnFailureListener{
                //파일 업로드 실패
                Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_LONG).show()
            }
        } else{
            flag = true
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
                Toast.makeText(this, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return false
    }
}