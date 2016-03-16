package com.example.shounakk.utdallas;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TabsPagerAdapter adapter;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create TabsPagerAdapter
        adapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Create Pager and set Adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Create TabLayout and set Pager
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        // Set up the tabs with images
        setUpTabs();

    }

    /**
     * Set up tabs with images
     */
    private void setUpTabs() {

        // Place Tab
        TextView tabPlace = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tabPlace.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_near_me, 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabPlace);

        // Groups Tab
        TextView tabGroups = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tabGroups.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_group, 0, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabGroups);

        // Events Tab
        TextView tabEvents = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tabEvents.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event, 0, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabEvents);

        // Profile Tab
        TextView tabProfile = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tabProfile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle, 0, 0, 0);
        tabLayout.getTabAt(3).setCustomView(tabProfile);

        // More Tab
        TextView tabMore = (TextView) LayoutInflater.from(this).inflate(R.layout.tab, null);
        tabMore.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_more_horiz, 0, 0, 0);
        tabLayout.getTabAt(4).setCustomView(tabMore);

    }


}
