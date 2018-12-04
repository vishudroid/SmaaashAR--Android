package com.smaaash.ar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CreateFragment extends Fragment {

    LinearLayout ll_card1, ll_card2, ll_card3;
    private List<Integer> urlsListCategory1,urlsListCategory2,urlsListCategory3;
    private List<String> titlesListCategory1,titlesListCategory2,titlesListCategory3;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    TextView tv_category_name;

    public CreateFragment() {
        // Required empty public constructor
    }

    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseTitlesUrlsArrays();
        setUpView(view);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    public void setUpView(View view) {

        ll_card1 = (LinearLayout) view.findViewById(R.id.ll_card1);
        ll_card2 = (LinearLayout) view.findViewById(R.id.ll_card2);
        ll_card3 = (LinearLayout) view.findViewById(R.id.ll_card3);

        View categoryView1 = LayoutInflater.from(getActivity()).inflate(R.layout.home_card_item, null);
        setUpViewForCards(categoryView1);
        tv_category_name.setText("Bollywood");
        recyclerAdapter = new RecyclerAdapter(urlsListCategory1, titlesListCategory1, getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        ll_card1.addView(categoryView1);
        View categoryView2 = LayoutInflater.from(getActivity()).inflate(R.layout.home_card_item, null);
        setUpViewForCards(categoryView2);
        tv_category_name.setText("Funky");
        recyclerAdapter = new RecyclerAdapter(urlsListCategory2, titlesListCategory2, getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        ll_card2.addView(categoryView2);

        View categoryView3 = LayoutInflater.from(getActivity()).inflate(R.layout.home_card_item, null);
        setUpViewForCards(categoryView3);
        tv_category_name.setText("Bollywood");
        recyclerAdapter = new RecyclerAdapter(urlsListCategory3, titlesListCategory3, getActivity());
        recyclerView.setAdapter(recyclerAdapter);
        ll_card3.addView(categoryView3);


    }

    public void initialiseTitlesUrlsArrays() {

        urlsListCategory1 = new ArrayList<>();
        urlsListCategory2 = new ArrayList<>();
        urlsListCategory3 = new ArrayList<>();
        titlesListCategory1 = new ArrayList<>();
        titlesListCategory2 = new ArrayList<>();
        titlesListCategory3 = new ArrayList<>();

        urlsListCategory1.add(R.drawable.obama);
        urlsListCategory1.add(R.drawable.pdma);
        urlsListCategory1.add(R.drawable.singham);
        urlsListCategory1.add(R.drawable.tiptap);
        urlsListCategory1.add(R.drawable.obama);

        urlsListCategory2.add(R.drawable.pdma);
        urlsListCategory2.add(R.drawable.obama);
        urlsListCategory2.add(R.drawable.singham);
        urlsListCategory2.add(R.drawable.tiptap);
        urlsListCategory2.add(R.drawable.obama);

        urlsListCategory3.add(R.drawable.pdma);
        urlsListCategory3.add(R.drawable.obama);
        urlsListCategory3.add(R.drawable.singham);
        urlsListCategory3.add(R.drawable.tiptap);
        urlsListCategory3.add(R.drawable.obama);


        titlesListCategory1.add(getString(R.string.obama));
        titlesListCategory1.add(getString(R.string.pdma));
        titlesListCategory1.add(getString(R.string.singham));
        titlesListCategory1.add(getString(R.string.tiptip));
        titlesListCategory1.add(getString(R.string.obama));

        titlesListCategory2.add(getString(R.string.pdma));
        titlesListCategory2.add(getString(R.string.obama));
        titlesListCategory2.add(getString(R.string.singham));
        titlesListCategory2.add(getString(R.string.tiptip));
        titlesListCategory2.add(getString(R.string.obama));

        titlesListCategory3.add(getString(R.string.pdma));
        titlesListCategory3.add(getString(R.string.obama));
        titlesListCategory3.add(getString(R.string.singham));
        titlesListCategory3.add(getString(R.string.tiptip));
        titlesListCategory3.add(getString(R.string.obama));



    }

    public void setUpViewForCards(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_category);
        tv_category_name = (TextView) view.findViewById(R.id.tv_category_name);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
    }


}
