package com.example.jobtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

import kotlinx.coroutines.Job;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder>
{
    private List<JobDataModel> jobList;
    private DatabaseReference databaseReference;
    private Aead aead;
    private Context context;
    private Boolean isClickable;
    private String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public JobAdapter(Context context,ArrayList<JobDataModel> jList,Boolean isClickable)
    {
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID).child("jobs");
        this.jobList = jList;
        this.isClickable = isClickable;

        intialiseAead();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                jobList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    JobDataModel job = dataSnapshot.getValue(JobDataModel.class);
                    jobList.add(job);
                }
                Log.d("JobAdapter", "Job list size: " + jobList.size());
                notifyItemRangeChanged(0,jobList.size());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void setClickable( boolean isClickable){
        this.isClickable = isClickable;
    }
    @NonNull
    @Override
    public JobAdapter.JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobAdapter.JobViewHolder holder, int position)
    {
        JobDataModel job = jobList.get(position);

        if(aead != null && job != null)
        {
            try
            {
                //String decryptedName = new String(aead.decrypt(Base64.decode(dataModel.getName(), Base64.DEFAULT), "".getBytes()));
                String decryptedStatus = new String(aead.decrypt(Base64.decode(job.getStatus(), Base64.DEFAULT), "".getBytes()));
                String decryptedPosition = new String(aead.decrypt(Base64.decode(job.getPosition(), Base64.DEFAULT), "".getBytes()));
                String decryptedSalary = new String(aead.decrypt(Base64.decode(job.getSalary(), Base64.DEFAULT), "".getBytes()));
                String decryptedJobType = new String(aead.decrypt(Base64.decode(job.getJobType(), Base64.DEFAULT), "".getBytes()));
                String decryptedKey = new String(aead.decrypt(Base64.decode(job.getKey(), Base64.DEFAULT), "".getBytes()));
                String decryptedDateOfSubmission = new String(aead.decrypt(Base64.decode(job.getDateOfSubmission(), Base64.DEFAULT), "".getBytes()));
                String decryptedDateOfReply = new String(aead.decrypt(Base64.decode(job.getDateOfReply(), Base64.DEFAULT), "".getBytes()));
                String decryptedCompany = new String(aead.decrypt(Base64.decode(job.getCompany(), Base64.DEFAULT), "".getBytes()));

                holder.companyName.setText(decryptedCompany);
                holder.position.setText(decryptedPosition);
                holder.date.setText(decryptedDateOfSubmission);
                holder.status.setText(decryptedStatus);
                statusColorChanger(holder.status, decryptedStatus, context);

                if(isClickable)
                {
                    holder.itemView.setOnClickListener(v -> {

                        //RetrievedJobDataFragment fragment = new RetrievedJobDataFragment();
                        // Pass data to the fragment using Bundle
                        Bundle args = new Bundle();
                        args.putString("status", decryptedStatus);
                        args.putString("company", decryptedCompany);
                        args.putString("position", decryptedPosition);
                        args.putString("salary", decryptedSalary);
                        args.putString("JobType", decryptedJobType);
                        args.putString("sDate", decryptedDateOfSubmission);
                        args.putString("rDate",decryptedDateOfReply);
                        args.putString("key", decryptedKey);
                        args.putString("userID", userID);


                        Navigation.findNavController(v).navigate(R.id.viewPagerFragment_to_retrievedJobDataFragment,args);
                    });
                }
                else {
                    holder.itemView.setOnClickListener(null);
                }
            }
            catch(Exception e){
                Log.e("DataAdapter", "Decryption error: " + e.getMessage());
            }
        }else{
            Log.e("DataAdapter", "Aead or dataModel is null");
            holder.itemView.setOnClickListener(null);
            holder.companyName.setText("N/A");
            holder.position.setText("N/A");
            holder.date.setText("N/A");
            holder.status.setText("N/A");
        }


    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public class JobViewHolder extends RecyclerView.ViewHolder
    {
        TextView companyName, position, date, status;
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.company_name);
            position = itemView.findViewById(R.id.job_position);
            date = itemView.findViewById(R.id.application_date);
            status = itemView.findViewById(R.id.application_status);

        }
    }
    public void statusColorChanger(TextView textView, String status, Context context) {
        switch (status) {
            case "Pending":
                textView.setTextColor(context.getResources().getColor(R.color.orange_pending));
                break;
            case "Rejected":
                textView.setTextColor(context.getResources().getColor(R.color.red_rejectde));
                break;
            case "Accepted":
                textView.setTextColor(context.getResources().getColor(R.color.green_aproved));
                break;
            default:
                textView.setTextColor(context.getResources().getColor(android.R.color.black));
                break;
        }
    }

    public void intialiseAead()
    {
        try
        {
            AeadConfig.register();
            KeysetHandle keysetHandle = new AndroidKeysetManager.Builder()
                    .withSharedPref(context,"my_keySet","my_keySetFile")
                    .withMasterKeyUri("android-keystore://my_master_key")
                    .build()
                    .getKeysetHandle();
            aead = keysetHandle.getPrimitive(Aead.class);
        }
        catch (GeneralSecurityException | IOException e) {
            Log.e("DataAdapter", "Error initializing Tink AEAD. Resetting keyset.", e);
        }
    }
}

