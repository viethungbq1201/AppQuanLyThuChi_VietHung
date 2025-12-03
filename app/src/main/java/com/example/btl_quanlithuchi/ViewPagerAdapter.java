package com.example.btl_quanlithuchi;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new Trangthu_Fragment();
            case 2: return new Trangchi_Fragment();
            case 3: return new Trangbieudo_Fragment();
            default: return new Trangchu_Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}

