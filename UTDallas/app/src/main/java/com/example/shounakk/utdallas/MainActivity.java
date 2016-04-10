package com.example.shounakk.utdallas;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.shounakk.utdallas.pages.EventsFragment;
import com.example.shounakk.utdallas.pages.GroupsFragment;
import com.example.shounakk.utdallas.pages.MenuClickListener;
import com.example.shounakk.utdallas.pages.MoreFragment;
import com.example.shounakk.utdallas.pages.PlacesFragment;
import com.example.shounakk.utdallas.pages.ProfileFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int PLACES_TAB = FragNavController.TAB1;
    public static final int GROUPS_TAB = FragNavController.TAB2;
    public static final int EVENTS_TAB = FragNavController.TAB3;
    public static final int PROFILE_TAB = FragNavController.TAB4;
    public static final int MORE_TAB = FragNavController.TAB5;

    private BottomBar mBotttomBar;
    private FragNavController mFragNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Fragment> fragments = new ArrayList<>(5);

        fragments.add(new PlacesFragment());
        fragments.add(new GroupsFragment());
        fragments.add(new EventsFragment());
        fragments.add(new ProfileFragment());
        fragments.add(new MoreFragment());

        mFragNavController = new FragNavController(getSupportFragmentManager(), R.id.fragContainer, fragments);

        mFragNavController.switchTab(PLACES_TAB);

        mBotttomBar = BottomBar.attach(this, savedInstanceState);
        mBotttomBar.noResizeGoodness();
        mBotttomBar.setItemsFromMenu(R.menu.bottombar_menu, new MenuClickListener(mFragNavController));
        int barColor = ContextCompat.getColor(this, R.color.mat_grey_800);
        mBotttomBar.mapColorForTab(0, barColor);
        mBotttomBar.mapColorForTab(1, barColor);
        mBotttomBar.mapColorForTab(2, barColor);
        mBotttomBar.mapColorForTab(3, barColor);
        mBotttomBar.mapColorForTab(4, barColor);
    }
}