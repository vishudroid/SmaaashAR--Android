package com.smaaash.ar;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);



    }
    private void setupViewPager(CustomViewPager viewPager) {

        CreateAdapter adapter = new CreateAdapter(getSupportFragmentManager());
        adapter.addFrag(new CreateFragment(), "Home");
        adapter.addFrag(new CreateFragment(), "Create");
        viewPager.setAdapter(adapter);
        viewPager.disableScroll(true);

    }





}
