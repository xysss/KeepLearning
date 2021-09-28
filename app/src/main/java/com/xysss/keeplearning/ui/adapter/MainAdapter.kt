package com.xysss.keeplearning.ui.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.xysss.keeplearning.ui.fragment.FourFragment
import com.xysss.keeplearning.ui.fragment.OneFragment
import com.xysss.keeplearning.ui.fragment.ThreeFragment
import com.xysss.keeplearning.ui.fragment.TwoFragment

/**
 * Author:bysd-2
 * Time:2021/9/2811:10
 */

class MainAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    companion object {
        const val PAGE_ONE = 0
        const val PAGE_TWO = 1
        const val PAGE_THREE = 2
        const val PAGE_FOUR = 3
    }

    private val fragments: SparseArray<Fragment> = SparseArray()

    init {
        fragments.put(PAGE_ONE, OneFragment())
        fragments.put(PAGE_TWO, TwoFragment())
        fragments.put(PAGE_THREE, ThreeFragment())
        fragments.put(PAGE_FOUR, FourFragment())
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size()
    }
}