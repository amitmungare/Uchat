package com.example.uchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneNumber:String
    private lateinit var countryCode:String
    private lateinit var alertDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumberET.addTextChangedListener {
            nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }

        nextBtn.setOnClickListener {
            checkNUmber()
        }

    }

    private fun checkNUmber() {

        countryCode  = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + phoneNumberET.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number: $phoneNumber\n"+
            "Is this OK, or would you like ot edit the number?")
            setPositiveButton("OK"){_,_ ->
                showOtpActivity()
            }
            setNegativeButton("Edit"){dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber))
        finish()
    }
}