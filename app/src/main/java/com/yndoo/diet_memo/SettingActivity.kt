package com.yndoo.diet_memo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.yndoo.diet_memo.databinding.ActivityMainBinding
import com.yndoo.diet_memo.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)

        binding.setImgBtn.setOnClickListener {

        }
    }
}