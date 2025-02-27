package com.example.jobtracker.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobtracker.JobAdapter;
import com.example.jobtracker.JobDataModel;
import com.example.jobtracker.R;
import com.example.jobtracker.Sign_up;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

public class HomePageFragment extends Fragment {
RecyclerView  recyclerView;
FirebaseAuth auth;
Aead aead;
JobAdapter adapter;
FloatingActionButton fab;
ArrayList<JobDataModel> jobList,progressList;
DatabaseReference databaseReference;
TextView countApproved,countPending,countRejected,countInProgress;
Boolean isClicked;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
       View view = inflater.inflate(R.layout.fragment_home_page,container,false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();

        intialiseAead();

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID).child("jobs");
        progressList = new ArrayList<>();
        jobList = new ArrayList<>();
        isClicked = false;
        adapter = new JobAdapter(getContext(),jobList,false);
        recyclerView.setAdapter(adapter);


        countApproved = view.findViewById(R.id.approved_count);
        countPending = view.findViewById(R.id.pending_count);
        countRejected = view.findViewById(R.id.rejections_count);
        countInProgress = view.findViewById(R.id.in_progress_count);
        fab = view.findViewById(R.id.floatingActionButton);

        fetchData();
        applicationProgress();

        fab.setOnClickListener(v -> {
            logOut();
        });



        return view;
    }
    private void applicationProgress()
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                int approvedCount = 0;
                int pendingCount = 0;
                int rejectedCount = 0;
                progressList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    JobDataModel job = dataSnapshot.getValue(JobDataModel.class);
                    progressList.add(job);
                }

                for (JobDataModel job : progressList)
                {
                    String decryptedStatus = null;
                    try {
                        decryptedStatus = new String(aead.decrypt(Base64.decode(job.getStatus(), Base64.DEFAULT), "".getBytes()));
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    switch (decryptedStatus)
                    {
                        case "Accepted":
                            approvedCount++;
                            break;
                        case "Pending":
                            pendingCount++;
                            break;
                        case "Rejected":
                            rejectedCount++;
                            break;
                    }
                }
                countApproved.setText(String.valueOf(approvedCount));
                countPending.setText(String.valueOf(pendingCount));
                countRejected.setText(String.valueOf(rejectedCount));
                countInProgress.setText(String.valueOf(progressList.size()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void intialiseAead()
    {
        try
        {
            AeadConfig.register();
            KeysetHandle keysetHandle = new AndroidKeysetManager.Builder()
                    .withSharedPref(getContext(),"my_keySet","my_keySetFile")
                    .withMasterKeyUri("android-keystore://my_master_key")
                    .build()
                    .getKeysetHandle();
            aead = keysetHandle.getPrimitive(Aead.class);
        }
        catch (GeneralSecurityException | IOException e) {
            Log.e("DataAdapter", "Error initializing Tink AEAD. Resetting keyset.", e);
        }
    }
    public void fetchData()
    {
        if (auth.getCurrentUser() == null) {
            // User is not logged in; avoid fetching data
            Log.d("HomePageFragment", "User is not authenticated. Skipping fetchData.");
            return;
        }
        databaseReference.orderByKey().limitToLast(3).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    JobDataModel job = dataSnapshot.getValue(JobDataModel.class);
                    jobList.add(job);
                }
                Collections.reverse(jobList);
                Log.d("JobAdapter", "Job list size: " + jobList.size());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    public void logOut()
//    {
//        new AlertDialog.Builder(getContext())
//                .setTitle("Confirm Logout")
//                .setMessage("Are you sure you want to log out?")
//                .setPositiveButton("Logout", (dialog, which) -> {
//
//                    FirebaseAuth.getInstance().signOut();
//                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(getContext(), Sign_up.class);
//                   // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
public void logOut() {
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            // Handle data when it changes
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // Handle any errors
        }
    };
    new AlertDialog.Builder(getContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout", (dialog, which) -> {
                // Remove the database listener
                if (databaseReference != null && valueEventListener != null) {
                    databaseReference.removeEventListener(valueEventListener);
                }
                // Sign out the user
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Redirect to the sign-up activity
                Intent intent = new Intent(getContext(), Sign_up.class);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
}

}