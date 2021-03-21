package com.example.uchat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.uchat.InboxFragment
import com.example.uchat.fragments.PeopleFragment

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val NUM_PAGES: Int =2
    override fun getItemCount(): Int=NUM_PAGES

    override fun createFragment(position: Int): Fragment = when (position){
        0-> InboxFragment()
        else -> PeopleFragment()
    }
}
