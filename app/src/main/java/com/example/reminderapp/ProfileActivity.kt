package com.example.reminderapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reminderapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textUsername.text = LoginActivity.usernameGlobal

        //Camera image onClickListener
        binding.imgCamera.setOnClickListener{
            if(!isCameraPermissionGranted()) {
                val permissions = mutableListOf(
                        Manifest.permission.CAMERA,
                )
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 444)
            }
            else{
                capturePhoto()
            }
        }

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                R.id.ic_add -> {
                    startActivity(Intent(applicationContext, AddActivity::class.java))
                    finish()
                }
            }
            true
        }

        if(!isCameraPermissionGranted()) {
            val permissions = mutableListOf(
                    Manifest.permission.CAMERA,
            )
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 444)
        }

    }

    private fun isCameraPermissionGranted():Boolean{
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun capturePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 200 && data != null){
            val photo = data.extras?.get("data") as Bitmap
            binding.imageView2.setImageBitmap(photo)
        }
    }
}