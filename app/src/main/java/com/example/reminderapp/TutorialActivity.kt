package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.reminderapp.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    private lateinit var registerBtn: Button
    private lateinit var binding: ActivityTutorialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerBtn = binding.btnRegister
        registerBtn.setOnClickListener {
            startActivity(
                    Intent(applicationContext, Tutorial2Activity::class.java))
        }
    }
}