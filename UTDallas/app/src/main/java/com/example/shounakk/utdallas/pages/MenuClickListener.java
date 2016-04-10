package com.example.shounakk.utdallas.pages;

import android.support.annotation.IdRes;

import com.example.shounakk.utdallas.MainActivity;
import com.example.shounakk.utdallas.R;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MenuClickListener implements OnMenuTabClickListener {
    FragNavController mFragNavController;

    public MenuClickListener(FragNavController fragNavController) {
        mFragNavController = fragNavController;
    }

    @Override
    public void onMenuTabSelected(@IdRes int menuItemId) {
        switch (menuItemId) {
            case R.id.bb1:
                mFragNavController.switchTab(MainActivity.PLACES_TAB);
                break;
            case R.id.bb2:
                mFragNavController.switchTab(MainActivity.GROUPS_TAB);
                break;
            case R.id.bb3:
                mFragNavController.switchTab(MainActivity.EVENTS_TAB);
                break;
            case R.id.bb4:
                mFragNavController.switchTab(MainActivity.PROFILE_TAB);
                break;
            case R.id.bb5:
                mFragNavController.switchTab(MainActivity.MORE_TAB);
                break;
        }
    }

    @Override
    public void onMenuTabReSelected(@IdRes int menuItemId) {
        // Scroll fragment back to top
    }
}