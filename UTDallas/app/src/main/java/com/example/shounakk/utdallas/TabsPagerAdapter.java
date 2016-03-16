package com.example.shounakk.utdallas;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * PagerAdapter for tabs
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;

        switch (index) {
            case 0:
                fragment = new PlacesFragment();
                break;
            case 1:
                fragment = new GroupsFragment();
                break;
            case 2:
                fragment = new EventsFragment();
                break;
            case 3:
                fragment = new ProfileFragment();
                break;
            case 4:
                fragment = new MoreFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() { return 5; }

}
