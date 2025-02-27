package com.example.jobtracker.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.jobtracker.JobDataModel;
import com.example.jobtracker.R;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;


public class JobFormFragment extends Fragment
{
    private Aead aead;
    DatePickerDialog datePickerDialog;
    DatePickerDialog submissionDatePickerDialog;
    DatePickerDialog replyDatePickerDialog;
    TextView cancel,save;
    Button submissionDate, replyDate;
    Spinner status,jobType;
    EditText companyName,jobTitle,salary;
    DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.job_applied_form, container, false);

        status = view.findViewById(R.id.spinnerApplicationStatus);
        jobType = view.findViewById(R.id.spinnerJobType);
        companyName = view.findViewById(R.id.editTextCompanyName);
        jobTitle = view.findViewById(R.id.editTextJobTitle);
        salary = view.findViewById(R.id.editTextSalary);
        submissionDate = view.findViewById(R.id.date_of_submission);
        replyDate = view.findViewById(R.id.date_of_reply);

        cancel = view.findViewById(R.id.btn_cancel);
        save = view.findViewById(R.id.btn_save);
        initSubmissionDatePicker();
        initReplyDatePicker();
        intialiseAead();

        submissionDate.setText(getTodaysDate());
        replyDate.setText(getTodaysDate());

        submissionDate.setOnClickListener(v -> submissionDatePickerDialog.show());
        replyDate.setOnClickListener(v -> replyDatePickerDialog.show());
        Bundle bundle = new Bundle();
        bundle.putInt("tab_index", 1);

        cancel.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_jobFormFragment_to_viewPagerFragment, bundle);


        });
        save.setOnClickListener(v ->
                {
                    // check this try catch exception
                    try {
                        saveJob();
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                    Navigation.findNavController(view).navigate(R.id.action_jobFormFragment_to_viewPagerFragment, bundle);

                }
        );
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Navigation.findNavController(view).navigate(R.id.action_jobFormFragment_to_viewPagerFragment);
            }
        });
        return view;

    }
    public void saveJob() throws GeneralSecurityException {
        // Get current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle the case where the user is not authenticated
            return;
        }
        String userId = currentUser.getUid();

        // Reference to the user's jobs node
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("jobs");

        // Generate a unique key for the job
        String key = databaseReference.push().getKey();

        // Get input data
        String sttus = status.getSelectedItem().toString();
        String jbType = jobType.getSelectedItem().toString();
        String cmpNme = companyName.getText().toString();
        String jbTitle = jobTitle.getText().toString();
        String salry = salary.getText().toString();
        String sDate = submissionDate.getText().toString();
        String rDate = replyDate.getText().toString();

        // Encryption of data

        byte [] encryptedSttus = aead.encrypt(sttus.getBytes(),"".getBytes());
        byte [] encryptedJbType = aead.encrypt(jbType.getBytes(),"".getBytes());
        byte [] encryptedCmpNme = aead.encrypt(cmpNme.getBytes(),"".getBytes());
        byte [] encryptedJbTitle = aead.encrypt(jbTitle.getBytes(),"".getBytes());
        byte [] encryptedSalry = aead.encrypt(salry.getBytes(),"".getBytes());
        byte [] encryptedSDate = aead.encrypt(sDate.getBytes(),"".getBytes());
        byte [] encryptedRDate = aead.encrypt(rDate.getBytes(),"".getBytes());
        byte [] encryptedKey = aead.encrypt(key.getBytes(),"".getBytes());

        //Convert encrypted data to a base64 string for storage
        String base64Status = Base64.encodeToString(encryptedSttus, Base64.DEFAULT);
        String base64JbType = Base64.encodeToString(encryptedJbType, Base64.DEFAULT);
        String base64CmpNme = Base64.encodeToString(encryptedCmpNme, Base64.DEFAULT);
        String base64JbTitle = Base64.encodeToString(encryptedJbTitle, Base64.DEFAULT);
        String base64Key = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        String base64Salry = Base64.encodeToString(encryptedSalry, Base64.DEFAULT);
        String base64SDate = Base64.encodeToString(encryptedSDate, Base64.DEFAULT);
        String base64RDate = Base64.encodeToString(encryptedRDate, Base64.DEFAULT);

        // Create JobDataModel instance
        JobDataModel jobDataModel = new JobDataModel(base64CmpNme,base64JbTitle,base64Status,base64Salry,base64JbType,base64Key,base64SDate,base64RDate);

        // Save data to the database
        databaseReference.child(key).setValue(jobDataModel);
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
        submissionDatePickerDialog = new DatePickerDialog(requireContext(), style, dateSetListener, year, month, day);
        submissionDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private void initReplyDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month += 1;
            String date = makeDateString(day, month, year);
            replyDate.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);

        int style = AlertDialog.THEME_HOLO_DARK;
        replyDatePickerDialog = new DatePickerDialog(requireContext(), style, dateSetListener, year, month, day);

        submissionDate.setOnClickListener(v -> {
            submissionDatePickerDialog.show();
            submissionDatePickerDialog.setOnDismissListener(dialog -> {
                String submissionDateText = submissionDate.getText().toString();
                Calendar submissionCalendar = Calendar.getInstance();
                String[] parts = submissionDateText.split(" ");
                submissionCalendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
                submissionCalendar.set(Calendar.MONTH, getMonthNumber(parts[0]) - 1);
                submissionCalendar.set(Calendar.DATE, Integer.parseInt(parts[1]));
                replyDatePickerDialog.getDatePicker().setMinDate(submissionCalendar.getTimeInMillis());
            });
        });
    }
    private int getMonthNumber(String month) {
        switch (month.toUpperCase()) {
            case "JAN": return 1;
            case "FEB": return 2;
            case "MAR": return 3;
            case "APR": return 4;
            case "MAY": return 5;
            case "JUN": return 6;
            case "JUL": return 7;
            case "AUG": return 8;
            case "SEP": return 9;
            case "OCT": return 10;
            case "NOV": return 11;
            case "DEC": return 12;
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
        month = month + 1;
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
    private void intialiseAead()
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