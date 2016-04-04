package com.example.shounakk.utdallas;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * PagerAdapter for tabs
 */
public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    Fragment changeTo;

    public void setChangeTo(ATEC fragment) {
        changeTo = fragment;
    }

    public TabsPagerAdapter(FragmentManager fm, ATEC atec, UXFragment ux) {
        super(fm);
        changeTo = atec;

        if(ux != null)
            changeTo = ux;

        if(changeTo == null)
            Log.d("sdf", "Null cont");
        else
            Log.d("asd", "not null const");
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;

        if(changeTo == null)
            Log.d("sdf", "Null get");
        else
            Log.d("asd", "not null get");

        switch (index) {
            case 0:
                if(changeTo == null)
                    fragment = new PlacesFragment();
                else
                    fragment = changeTo;
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
