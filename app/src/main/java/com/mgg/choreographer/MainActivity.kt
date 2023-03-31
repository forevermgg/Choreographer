package com.mgg.choreographer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mgg.choreographer.databinding.ActivityMainBinding

open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    /**
     * A native method that is implemented by the 'choreographer' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'choreographer' library on application startup.
        init {
            System.loadLibrary("choreographer")
        }
    }
}