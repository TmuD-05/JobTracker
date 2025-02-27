package com.example.jobtracker.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jobtracker.R;
import com.example.jobtracker.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainFragmentVw extends Fragment {
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_main_vw, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        viewPager.setAdapter(new ViewPageAdapter(requireActivity().getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        // Handle back navigation
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() > 0) {
                    // Move to the previous tab
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                } else {
                    // Default back press behavior
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });

        if (getArguments() != null) {
            int tabIndex = getArguments().getInt("tab_index", 1);
            Log.d("MainFragmentVw", "Received tab index: " + tabIndex);
            viewPager.setCurrentItem(tabIndex, true);
        }
        else {
            Log.d("MainFragmentVw", "No tab index received");
        }

        return view;
    }

}
