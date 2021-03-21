package com.example.uchat.fragments

import android.content.Intent
import android.os.Bundle
import android.system.Os.bind
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uchat.ChatsAvt
import com.example.uchat.EmptyViewHolder
import com.example.uchat.R
import com.example.uchat.databinding.ActivityChatBinding.bind
import com.example.uchat.databinding.ActivityLoginBinding.bind
import com.example.uchat.databinding.EmptyViewBinding.bind
import com.example.uchat.models.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chats.*
import java.lang.Exception

private const val  DELETED_VIEW_TYPE =1
private const val  NORMAL_VIEW_TYPE =2

class PeopleFragment : Fragment() {

    private lateinit var  mAdapter: FirestorePagingAdapter<User,RecyclerView.ViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = LinearLayoutManager(requireContext())
        setUpAdapter()
        return inflater.inflate(R.layout.fragment_chats,container,false)
    }

    private fun setUpAdapter() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(10)
            .build()

        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(database,config,User::class.java)
            .build()

        mAdapter = object :FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                 return when(viewType){
                     NORMAL_VIEW_TYPE ->{
                         UserViewHolder(inflater.inflate(R.layout.list_item,parent,false))
                     }
                     else-> EmptyViewHolder(inflater.inflate(R.layout.empty_view,parent,false))
                 }

            }

            override fun onBindViewHolder(
                viewHolder: RecyclerView.ViewHolder,
                position: Int,
                user: User
            ) {
                // Bind to ViewHolder
                if (viewHolder is UserViewHolder)
                    if (auth.uid == user.uid) {
                        currentList?.snapshot()?.removeAt(position)
                        notifyItemRemoved(position)
                    } else
                        viewHolder.bind(user) { name: String, photo: String, id: String ->
                            startActivity(
                                ChatsAvt.createChatActivity(
                                    requireContext(),
                                    id,
                                    name,
                                    photo
                                )
                            )
                        }
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid){
                    DELETED_VIEW_TYPE
                }else{
                    NORMAL_VIEW_TYPE
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when(state){
                    LoadingState.LOADING_INITIAL -> { }
                    LoadingState.LOADING_MORE -> { }
                    LoadingState.LOADED -> { }
                    LoadingState.ERROR -> { }
                    LoadingState.FINISHED -> { }
                }
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter =mAdapter
        }
    }

}
