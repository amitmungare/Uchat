package com.example.uchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_otp.*

const val PHONE_NUMBER ="phoneNumber"

class OtpActivity : AppCompatActivity() {

    var phoneNumber:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        initView()
    }

    private fun initView() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifytv.text = getString(R.string.verify_number, phoneNumber)
    }

}