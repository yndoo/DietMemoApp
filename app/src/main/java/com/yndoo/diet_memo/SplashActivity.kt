package com.yndoo.diet_memo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth
        val database = Firebase.database
        val myRef = database.getReference("myMemo")

        try{
            Log.d("SPLASH",auth.currentUser!!.uid)
            //Toast.makeText(this,"기존 비회원 로그인되어 있음", Toast.LENGTH_LONG).show()
            //로그인 되어 있으니 화면 넘어감
            myRef.child(auth.currentUser!!.uid).child("nickname").get().addOnSuccessListener {result ->
                findViewById<TextView>(R.id.userNameTitle).setText(result.value.toString())
            }

            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 3000)

        } catch(e : Exception) {
            //Log.d("SPLASH","회원가입 안 되어 있음")
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"비회원 로그인 성공", Toast.LENGTH_LONG).show()
                        //첫회원 닉네임 "나"로 초기화
                        myRef.child(auth.currentUser!!.uid).child("nickname").setValue("나")
                        //로그인 성공이면 화면 넘어감
                        Handler().postDelayed({
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }, 3000)
                    } else {
                        Toast.makeText(this,"비회원 로그인 실패", Toast.LENGTH_LONG).show()
                    }
                }

        }



    }
}