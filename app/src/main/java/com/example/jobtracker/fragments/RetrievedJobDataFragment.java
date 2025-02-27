package com.example.jobtracker.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobtracker.R;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RetrievedJobDataFragment extends Fragment
{
    private Aead aead;
    DatePickerDialog subDatePickerDialog;
    DatePickerDialog repDatePickerDialog;
    TextView cancel,update,delete;
    Button replyDate, submissionDate;
    Spinner status,jobType;
    EditText companyName,jobTitle,salary;
    DatabaseReference databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         View view =inflater.inflate(R.layout.job_update_form, container, false);


        status = view.findViewById(R.id.spinner_ApplicationStatus);
        jobType = view.findViewById(R.id.spinner_JobType);
        cancel = view.findViewById(R.id.btn_cancel);
        update = view.findViewById(R.id.btn_update);
        delete = view.findViewById(R.id.btn_delete);
        companyName = view.findViewById(R.id.editText_CompanyName);
        jobTitle = view.findViewById(R.id.editText_JobTitle);
        salary = view.findViewById(R.id.editText_Salary);
        replyDate = view.findViewById(R.id.btn_reply);
        submissionDate = view.findViewById(R.id.btn_submission);

        intialiseAead();

        //Getting the data from the bundle
        String companyValue = getArguments().getString("company");
        String positionValue = getArguments().getString("position");
        String statusValue = getArguments().getString("status");
        String salaryValue = getArguments().getString("salary");
        String jobTypeValue = getArguments().getString("jobType");
        String reptDate = getArguments().getString("rDate");
        String subDate = getArguments().getString("sDate");

        initSubmissionDatePicker();
        initReplyDatePicker();

        // setting the data to edittext
        companyName.setText(companyValue);
        jobTitle.setText(positionValue);
        salary.setText(salaryValue);
        replyDate.setText(reptDate);
        submissionDate.setText(subDate);


        // setting the data to the spinner
        if (statusValue != null) {
            ArrayAdapter<CharSequence> statusAdapter = (ArrayAdapter<CharSequence>) status.getAdapter();
            int statusPosition = statusAdapter.getPosition(statusValue);
            status.setSelection(statusPosition != -1 ? statusPosition : 0);
        }
        if (jobTypeValue != null){
            ArrayAdapter<CharSequence> jobTypeAdapter = (ArrayAdapter<CharSequence>) jobType.getAdapter();
            int jobTypePosition = jobTypeAdapter.getPosition(jobTypeValue);
            jobType.setSelection(jobTypePosition != -1 ? jobTypePosition : 0);
        }

        submissionDate.setOnClickListener(v -> subDatePickerDialog.show());
        replyDate.setOnClickListener(v -> repDatePickerDialog.show());
        // to navigate back to the applications fragment
        Bundle bundle = new Bundle();
        bundle.putInt("tab_index", 1);

        //setting the onclick Listener for the cancel button
        cancel.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_retrievedJobDataFragment_to_viewPagerFragment, bundle);
        });
        //setting the onclick Listener for the delete button
        delete.setOnClickListener(v -> {
            deleteApplication();
            Navigation.findNavController(view).navigate(R.id.action_retrievedJobDataFragment_to_viewPagerFragment, bundle);
        });
        //setting the onclick Listener for the update button
        update.setOnClickListener(v -> {
            updateJobData();
            Navigation.findNavController(view).navigate(R.id.action_retrievedJobDataFragment_to_viewPagerFragment, bundle);

        });
        // if the user presses the back button, navigate back to the main fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).navigate(R.id.action_retrievedJobDataFragment_to_viewPagerFragment);
            }
        });

        return view;
    }

    public void updateJobData() {
        // Get the userID from arguments or authentication
        String userID = getArguments().getString("userID");
        // Example: retrieve userID from arguments
        String key = getArguments().getString("key");
        Log.d("RetrievedJobDataFragment", "Key: " + key);
        Log.d("RetrievedJobDataFragment", "userId: " + userID);
        // Retrieve the job key

        // Initialize the DatabaseReference with the new structure: userID -> "Job" -> key
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userID).child("jobs").child(key);

        // Collect the updated values
        String sttus = status.getSelectedItem().toString();
        String jbType = jobType.getSelectedItem().toString();
        String cmpNme = companyName.getText().toString();
        String jbTitle = jobTitle.getText().toString();
        String salry = salary.getText().toString();
        String repDate = replyDate.getText().toString();
        String subDate = submissionDate.getText().toString();
        try{

            byte[] encryptedStatus = aead.encrypt(sttus.getBytes(),"".getBytes());
            byte[] encryptedJbType = aead.encrypt(jbType.getBytes(),"".getBytes());
            byte[] encryptedCmpNme = aead.encrypt(cmpNme.getBytes(),"".getBytes());
            byte[] encryptedJbTitle = aead.encrypt(jbTitle.getBytes(),"".getBytes());
            byte[] encryptedSalry = aead.encrypt(salry.getBytes(),"".getBytes());
            byte[] encryptedRepDate = aead.encrypt(repDate.getBytes(),"".getBytes());
            byte[] encryptedSubDate = aead.encrypt(subDate.getBytes(),"".getBytes());
            byte[] encryptedKey = aead.encrypt(key.getBytes(),"".getBytes());


            String base64Status = Base64.encodeToString(encryptedStatus, Base64.DEFAULT);
            String base64JbType = Base64.encodeToString(encryptedJbType, Base64.DEFAULT);
            String base64CmpNme = Base64.encodeToString(encryptedCmpNme, Base64.DEFAULT);
            String base64JbTitle = Base64.encodeToString(encryptedJbTitle, Base64.DEFAULT);
            //String base64Key = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            String base64Salry = Base64.encodeToString(encryptedSalry, Base64.DEFAULT);
            String base64RepDate = Base64.encodeToString(encryptedRepDate, Base64.DEFAULT);
            String base64SubDate = Base64.encodeToString(encryptedSubDate, Base64.DEFAULT);

            // Create a map for the updates
            Map<String, Object> updates = new HashMap<>();
            updates.put("company",base64CmpNme);
            updates.put("position", base64JbTitle);
            updates.put("jobType", base64JbType);
            updates.put("status", base64Status);
            updates.put("salary", base64Salry);
            updates.put("dateOfReply", base64RepDate);
            updates.put("dateOfSubmission", base64SubDate);

            // Update the data at the specified path
            databaseReference.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Handle success, e.g., show a confirmation message
                       // Toast.makeText(getContext(), "Job updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure, e.g., show an error message
                       // Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        catch(GeneralSecurityException e)
        {
            e.printStackTrace();
        }

    }

    public void deleteApplication()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference("Job");
        String key = getArguments().getString("key");
        databaseReference.child(key).removeValue();
    }

    private void initSubmissionDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month += 1; // Adjust for 0-based index
            String date = makeDateString(day, month, year);
            submissionDate.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);

        int style = AlertDialog.THEME_HOLO_DARK;
        subDatePickerDialog = new DatePickerDialog(requireContext(), style, dateSetListener, year, month, day);
        subDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private void initReplyDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month += 1; // Adjust for 0-based index
            String date = makeDateString(day, month, year);
            replyDate.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);

        int style = AlertDialog.THEME_HOLO_DARK;
        repDatePickerDialog = new DatePickerDialog(requireContext(), style, dateSetListener, year, month, day);

        // Dynamically set the minimum date for the reply date picker based on submission date
        submissionDate.setOnClickListener(v -> {
            subDatePickerDialog.show();
            subDatePickerDialog.setOnDismissListener(dialog -> {
                String submissionDateText = submissionDate.getText().toString();
                Calendar submissionCalendar = Calendar.getInstance();
                String[] parts = submissionDateText.split(" ");
                submissionCalendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
                submissionCalendar.set(Calendar.MONTH, getMonthNumber(parts[0]) - 1);
                submissionCalendar.set(Calendar.DATE, Integer.parseInt(parts[1]));
                repDatePickerDialog.getDatePicker().setMinDate(submissionCalendar.getTimeInMillis());
            });
        });
    }

    private int getMonthNumber(String month) {
        switch (month.toUpperCase()) {
            case "Jan": return 1;
            case "Feb": return 2;
            case "Mar": return 3;
            case "Apr": return 4;
            case "May": return 5;
            case "Jun": return 6;
            case "Jul": return 7;
            case "Aug": return 8;
            case "Sep": return 9;
            case "Oct": return 10;
            case "Nov": return 11;
            case "Dec": return 12;
            default: return 1;
        }
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1; // Adjust month (0-based index)
        int date = cal.get(Calendar.DATE);
        return makeDateString(date, month, year);
    }

    private String getMonthFormat(int month) {
        switch (month) {
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "Mar";
            case 4: return "Apr";
            case 5: return "May";
            case 6: return "Jun";
            case 7: return "Jul";
            case 8: return "Aug";
            case 9: return "Sep";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
            default: return "Jan";
        }
    }
    public void intialiseAead()
    {
        try{
            AeadConfig.register();
            KeysetHandle keysetHandle = new AndroidKeysetManager.Builder()
                    .withSharedPref(getContext(), "my_keySet","my_keySetFile")
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .build()
                    .getKeysetHandle();
            aead = keysetHandle.getPrimitive(Aead.class);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}