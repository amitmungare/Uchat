package com.example.uchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

class ChatActivity : AppCompatActivity() {

    private val friendId: String by lazy {
        intent.getStringExtra(UID)!!
    }

    private val name : String by lazy {
        intent.getStringExtra(NAME)!!
    }
    private val image : String by lazy {
        intent.getStringExtra(IMAGE)!!

    }


    private val mCurrentUid:String by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser: User
//    lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }

        nameTv.text=name
        Picasso.get().load(image).into(userImgView)

        sendBtn.setOnClickListener {
             msgEdtv.text?.let { 
                 if(it.isNotEmpty()){
                     sendMessage(it.toString())
                     it.clear()
                 }
             }
        }

    }

    private fun sendMessage(msg: String) {

        val id = getMessages(friendId).push().key
        checkNotNull(id){"Cannot be null"}
        val msgMap = Message(msg, mCurrentUid,id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener{

        }.addOnFailureListener {

        }

        updateLastMassage(msgMap)

    }

    private fun updateLastMassage(message: Message) {

        val inboxMap = Inbox(message.msg, friendId, name, image, count = 0)
        getInbox(mCurrentUid, friendId).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)

                    inboxMap.apply {
                        from= message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count = 1
                    }
                    value?. let {
                        if(it.from == message.senderId){
                            inboxMap.count = value.count+1
                        }
                    }

                    getInbox(friendId, mCurrentUid).setValue(inboxMap)

                }

            })
        }
    }

    private fun makeAsRead(){
        getInbox(friendId, mCurrentUid).child("count").setValue(0)
    }

    private fun getMessages(friendId: String)=
        db.reference.child("messages/${getId(friendId)}")


    private fun getInbox(toUser:String, fromUser:String)=
        db.reference.child("chats/$toUser/$fromUser")

    private  fun getId(friendId:String):String{
        return if(friendId > mCurrentUid){
            mCurrentUid+friendId
        }else{
            friendId+mCurrentUid
        }
    }

}


