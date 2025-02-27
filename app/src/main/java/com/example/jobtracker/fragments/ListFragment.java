package com.example.jobtracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jobtracker.JobAdapter;
import com.example.jobtracker.JobDataModel;
import com.example.jobtracker.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ListFragment extends Fragment {
    RecyclerView recyclerView;
    FloatingActionButton fab;
    FirebaseAuth auth;
    JobAdapter adapter;
    DatabaseReference databaseReference;
    ArrayList<JobDataModel> jobList;
    Boolean isClicked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = view.findViewById(R.id.recyler_list);
        auth = FirebaseAuth.getInstance();
        // Get the current user's ID from Firebase Authentication
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize DatabaseReference to the path: userID -> Job
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID).child("jobs");

        jobList = new ArrayList<>();
        isClicked = true;
        adapter = new JobAdapter(getContext(), jobList, isClicked);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchData();

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.viewPagerFragment_to_jobFormFragment);
        });

        return view;
    }

    public void fetchData() {
        if (auth.getCurrentUser() == null) {
            // User is not logged in; avoid fetching data
            Log.d("HomePageFragment", "User is not authenticated. Skipping fetchData.");
            return;
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    JobDataModel job = dataSnapshot.getValue(JobDataModel.class);
                    jobList.add(job);
                }
                Collections.reverse(jobList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
