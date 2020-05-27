package com.thedancercodes.androidcv345

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_display_image.*
import kotlinx.android.synthetic.main.camera_layout.*

class DisplayImage : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image)
        val bitmap = BitmapFactory.decodeFile(intent.getStringExtra("image_path"))
        displayImageView.setImageBitmap(bitmap)
    }
}