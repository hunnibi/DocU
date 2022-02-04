package com.mainpckg.docu

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import java.lang.Exception

class MainActivity : AppCompatActivity(){
    private val requestCodeSpeechInput = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val speechButton = findViewById<ImageButton>(R.id.speakImageButton)
        speechButton.setOnClickListener { speak() }
    }

    // Actual implementation of the recording of sound @Todo move to own class
    private fun speak(){
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de")
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording:")
        
        try {
            //if no error -> Display Text
            startActivityForResult(mIntent, requestCodeSpeechInput)
        }catch (e: Exception){
            Log.e("VOICE RECORDING", e.message.toString())
        }
    }
    //TODO Should not use startActivity / onActivityResults
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            requestCodeSpeechInput -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val displayText = findViewById<TextView>(R.id.recordedTextView)
                    displayText.text = result!![0]
                }
            }
        }
    }
    //TODO Actual Threading et al for the Bluetooth-Connector. HOPE this works for usb connection too
}