package com.yndoo.diet_memo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth

        try{
            Log.d("SPLASH",auth.currentUser!!.uid)
            Toast.makeText(this,"기존 비회원 로그인되어 있음", Toast.LENGTH_LONG).show()
            //로그인 되어 있으니 화면 넘어감
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 3000)
        } catch(e : Exception) {
            Log.d("SPLASH","회원가입 안 되어 있음")
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"비회원 로그인 성공", Toast.LENGTH_LONG).show()
                        //로그인 성공이면 화면 넘어감
                        Handler().postDelayed({
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }, 3000)
                    } else {
                        Toast.makeText(this,"비회원 로그인 살패", Toast.LENGTH_LONG).show()
                    }
                }
        }



    }
}