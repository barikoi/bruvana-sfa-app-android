package com.barikoi.cnlapp.Adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {
    ArrayList<Fragment> fragments= new ArrayList<>();
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<Fragment> fragments) {
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        /*if (position == 0)
        {
            fragment = fragments.get(position);
        }
        else if (position == 1)
        {
            fragment = fragments.get(position);
        }
        else if (position == 2)
        {
            fragment = fragments.get(position);
        }*/
        Log.d("Fragment", "fragment tab: " + fragments.size()+ " position: " + fragments.get(position));
        Log.d("Fragment", "fragment tab pos: " + position);
        fragment = fragments.get(position);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}

