package com.example.uchat

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER ="phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var phoneNumber:String? = null
    private var mverificationId:String? = null
    private var mResendToken:PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown : CountDownTimer? =null
    private var timeLeft: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initView()
        startVerify()
    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
        progressDialog = createProgressDialog("Send a verification Code",true)
        progressDialog.show()
    }



    private fun initView() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifytv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()

        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                val smsCode = credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    sendcodeET.setText(smsCode)
                }

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {

                } else if (e is FirebaseTooManyRequestsException) {

                }
                notifyUserAndRetry("your Phone number might be wrong or connection error. Retry Again")
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                progressDialog.dismiss()
                counterTV.isVisible= false

                mverificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))
        val ClickSpan: ClickableSpan = object :ClickableSpan(){

            override fun updateDrawState(ds: TextPaint) {
                ds.color =ds.linkColor
                ds.isUnderlineText=false
            }

            override fun onClick(widget: View) {
                showLoginActivity()
            }
        }
        span.setSpan(ClickSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod =LinkMovementMethod.getInstance()
        waitingTv.text =span
    }


    private fun notifyUserAndRetry(s: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(s)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber( phoneNumber, 60, TimeUnit.SECONDS, this, callbacks)
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task->
                if(task.isSuccessful){

                    if(::progressDialog.isInitialized){
                        progressDialog.dismiss()
                    }
                    if (task.result?.additionalUserInfo?.isNewUser==true){
                        showSignUpActivity()
                    }else{
                        showHomeActivity()
                    }

                }else{
                    if(::progressDialog.isInitialized){
                        progressDialog.dismiss()
                    }
                    notifyUserAndRetry("your Phone number Verification failed. Try Again")
                }
            }
    }

    private fun showSignUpActivity() {
        val intent =Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showHomeActivity() {
        val intent =Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }



    override fun onBackPressed() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown !=null){
            mCounterDown!!.cancel()
        }
    }

    override fun onClick(v: View) {
        when(v){
            verificationBtn ->{
                val code = sendcodeET.text.toString()
                if(code.isNotEmpty() && !mverificationId.isNullOrBlank()){
                    progressDialog = createProgressDialog("please wait....",false)
                    progressDialog.show()

                    val credential = PhoneAuthProvider.getCredential(mverificationId!!,code.toString())
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn ->{
                if(mResendToken != null) {
//                    resendVerificationCode(phoneNumber.toString(), mResendToken)
                    showTimer(600000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()
                }else{
//                    toast("sorry, you can't request new code now, please wait ...")
//                    PhoneAuthProvider.getInstance().verifyPhoneNumber( phoneNumber!!, 60, TimeUnit.SECONDS, this, callbacks, mResendToken)

                }
            }

        }
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = false

        mCounterDown=  object:CountDownTimer(milliSecInFuture,1000){
            override fun onTick(millisUntilFinished: Long) {
                counterTV.isVisible = true
                counterTV.text=getString(R.string.seconds_remaining, millisUntilFinished/1000)
            }

            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTV.isVisible= false
            }

        }.start()

    }

}

fun Context.createProgressDialog(message: String, isCancelable:Boolean):ProgressDialog{
    return  ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }

}