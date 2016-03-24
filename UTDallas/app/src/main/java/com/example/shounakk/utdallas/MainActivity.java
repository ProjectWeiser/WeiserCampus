package com.example.shounakk.utdallas;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, ViewPager.PageTransformer {

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
        String[] tabNames = {"Place", "Groups", "Events", "Profile", "More"};
        int[] tabIcons = {
                R.drawable.ic_near_me,
                R.drawable.ic_group,
                R.drawable.ic_event,
                R.drawable.ic_account_circle,
                R.drawable.ic_more_horiz
        };

        for(int i = 0; i < tabNames.length; i++) {
            RelativeLayout tabPlace = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tab, null);
            TextView tabPlaceText = (TextView) tabPlace.findViewWithTag("tabText");
            tabPlaceText.setText(tabNames[i]);
            ImageView tabPlaceIcon = (ImageView) tabPlace.findViewWithTag("tabIcon");
            tabPlaceIcon.setImageResource(tabIcons[i]);
            tabLayout.getTabAt(i).setCustomView(tabPlace);
        }

        View view = tabLayout.getTabAt(0).getCustomView();
        if (view.findViewWithTag("tabText") != null && view.findViewWithTag("tabIcon") != null) {
            ((TextView) view.findViewWithTag("tabText")).animate().translationY(-30.0f).alpha(1.0f).setDuration(0).start();
            ((ImageView) view.findViewWithTag("tabIcon")).animate().translationY(-18.0f).alpha(1.0f).scaleX(1.25f).scaleY(1.25f).setDuration(0).start();
        }

        tabLayout.setOnTabSelectedListener(this);

        pager.setPageTransformer(false, this);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
        View view = tab.getCustomView();

        if (view.findViewWithTag("tabText") != null && view.findViewWithTag("tabIcon") != null) {
            ((TextView) view.findViewWithTag("tabText")).animate().translationY(-24.0f).alpha(1.0f).setDuration(250).start();
            ((ImageView) view.findViewWithTag("tabIcon")).animate().translationY(-18.0f).alpha(1.0f).scaleX(1.25f).scaleY(1.25f).setDuration(250).start();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        View view = tab.getCustomView();

        if ((TextView) view.findViewWithTag("tabText") != null) {
            ((TextView) view.findViewWithTag("tabText")).animate().translationY(0).alpha(0.0f).setDuration(250).start();
            ((ImageView) view.findViewWithTag("tabIcon")).animate().translationY(0.0f).alpha(0.65f).scaleX(1.0f).scaleY(1.0f).setDuration(250).start();
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void transformPage(View page, float position) {
        Log.d("UTDALLAS", "Page changed");
    }
}
