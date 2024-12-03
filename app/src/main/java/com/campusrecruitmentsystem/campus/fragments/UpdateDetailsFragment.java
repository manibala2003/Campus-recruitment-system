package com.campusrecruitmentsystem.campus.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dhruv.campusrecruitmentsystem.R;
import com.campusrecruitmentsystem.campus.Utils;
import com.campusrecruitmentsystem.campus.dashboard.AdminDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.CompanyDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.StudentDashboardActivity;
import com.dhruv.campusrecruitmentsystem.databinding.FragmentUpdateDetailsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class UpdateDetailsFragment extends Fragment {

    private FragmentUpdateDetailsBinding binding;
    private Utils.AccountType accountType;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        onClickUpdateDetails();

        switch (PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getContext().getResources().getString(R.string.account_type),1)){
            case 0:
                accountType = Utils.AccountType.ADMIN;
                rootRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUser.getUid()).child("Profile");
                break;
            case 1:
                accountType = Utils.AccountType.STUDENT;
                binding.studentSection.setVisibility(View.VISIBLE);
                rootRef = FirebaseDatabase.getInstance().getReference().child("Student").child(currentUser.getUid()).child("Profile");
                break;
            case 2:
                accountType = Utils.AccountType.COMPANY;
                rootRef = FirebaseDatabase.getInstance().getReference().child("Company").child(currentUser.getUid()).child("Profile");
                binding.fullNameEditText.setHint("Company Name");
                break;
        }

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                HashMap<String, String> details = (HashMap<String, String>) snapshot.getValue();
                binding.fullNameEditText.getEditText().setText(details.get("full_name"));
                binding.contactNoEditText.getEditText().setText(details.get("contact_no"));
                if(accountType == Utils.AccountType.STUDENT){
                    binding.rollNoEditText.getEditText().setText(details.get("roll_no"));
                    if(snapshot.hasChild("resume")) {
                        DataSnapshot resume = snapshot.child("resume");
                        binding.profileSummaryEditText.getEditText().setText(resume.child("profile_summary").getValue().toString());
                        binding.keySkillsEditText.getEditText().setText(resume.child("key_skills").getValue().toString());

                        //CLASS X DETAILS
                        binding.tenthPercentageEditText.getEditText().setText(String.valueOf(resume.child("class_tenth").child("percentage").getValue()));
                        binding.tenthBoardEditText.getEditText().setText(resume.child("class_tenth").child("board").getValue().toString());
                        binding.tenthSchoolEditText.getEditText().setText(resume.child("class_tenth").child("school").getValue().toString());
                        binding.tenthCityEditText.getEditText().setText(resume.child("class_tenth").child("city").getValue().toString());

                        //CLASS XII DETAILS
                        binding.twelfthPercentageEditText.getEditText().setText(String.valueOf(resume.child("class_twelfth").child("percentage").getValue()));
                        binding.twelfthBoardEditText.getEditText().setText(resume.child("class_twelfth").child("board").getValue().toString());
                        binding.twelfthSchoolEditText.getEditText().setText(resume.child("class_twelfth").child("school").getValue().toString());
                        binding.twelfthCityEditText.getEditText().setText(resume.child("class_twelfth").child("city").getValue().toString());

                        //GRADUATION DETAILS
                        binding.graduationPercentageEditText.getEditText().setText(String.valueOf(resume.child("graduation").child("percentage").getValue()));
                        binding.graduationStreamEditText.getEditText().setText(resume.child("graduation").child("stream").getValue().toString());
                        binding.graduationCollegeEditText.getEditText().setText(resume.child("graduation").child("college").getValue().toString());
                        binding.graduationUniversityEditText.getEditText().setText(resume.child("graduation").child("university").getValue().toString());
                        binding.graduationCityEditText.getEditText().setText(resume.child("graduation").child("city").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                System.out.println(error);
            }
        });
    }

    public void onClickUpdateDetails(){
        binding.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isNetworkConnected(getContext())) {
                    rootRef.child("full_name").setValue(binding.fullNameEditText.getEditText().getText().toString());
                    rootRef.child("contact_no").setValue(binding.contactNoEditText.getEditText().getText().toString());
                    if (accountType == Utils.AccountType.STUDENT) {
                        rootRef.child("roll_no").setValue(binding.rollNoEditText.getEditText().getText().toString());
                        DatabaseReference resume = rootRef.child("resume");
                        resume.child("profile_summary").setValue(binding.profileSummaryEditText.getEditText().getText().toString());
                        resume.child("key_skills").setValue(binding.keySkillsEditText.getEditText().getText().toString());

                        //CLASS X DETAILS
                        if(!binding.tenthPercentageEditText.getEditText().getText().toString().isEmpty())
                            resume.child("class_tenth").child("percentage").setValue(Long.valueOf(binding.tenthPercentageEditText.getEditText().getText().toString()));
                        resume.child("class_tenth").child("board").setValue(binding.tenthBoardEditText.getEditText().getText().toString());
                        resume.child("class_tenth").child("school").setValue(binding.tenthSchoolEditText.getEditText().getText().toString());
                        resume.child("class_tenth").child("city").setValue(binding.tenthCityEditText.getEditText().getText().toString());

                        //CLASS XII DETAILS
                        if(!binding.twelfthPercentageEditText.getEditText().getText().toString().isEmpty())
                            resume.child("class_twelfth").child("percentage").setValue(Long.valueOf(binding.twelfthPercentageEditText.getEditText().getText().toString()));
                        resume.child("class_twelfth").child("board").setValue(binding.twelfthBoardEditText.getEditText().getText().toString());
                        resume.child("class_twelfth").child("school").setValue(binding.twelfthSchoolEditText.getEditText().getText().toString());
                        resume.child("class_twelfth").child("city").setValue(binding.twelfthCityEditText.getEditText().getText().toString());

                        //GRADUATION DETAILS
                        if(!binding.graduationPercentageEditText.getEditText().getText().toString().isEmpty())
                            resume.child("graduation").child("percentage").setValue(Long.valueOf(binding.graduationPercentageEditText.getEditText().getText().toString()));
                        resume.child("graduation").child("stream").setValue(binding.graduationStreamEditText.getEditText().getText().toString());
                        resume.child("graduation").child("college").setValue(binding.graduationCollegeEditText.getEditText().getText().toString());
                        resume.child("graduation").child("university").setValue(binding.graduationUniversityEditText.getEditText().getText().toString());
                        resume.child("graduation").child("city").setValue(binding.graduationCityEditText.getEditText().getText().toString());

                        ((StudentDashboardActivity) getActivity()).updateUserName(binding.fullNameEditText.getEditText().getText().toString());
                    } else {
                        if (getActivity() instanceof AdminDashboardActivity)
                            ((AdminDashboardActivity) getActivity()).updateUserName(binding.fullNameEditText.getEditText().getText().toString());
                        else
                            ((CompanyDashboardActivity) getActivity()).updateUserName(binding.fullNameEditText.getEditText().getText().toString());
                    }
                    Snackbar.make(getView(), "Details Updated", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(getView(), "Internet not available!", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}