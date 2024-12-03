package com.campusrecruitmentsystem.campus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusrecruitmentsystem.campus.dashboard.AdminDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.CompanyDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.StudentDashboardActivity;
import com.dhruv.campusrecruitmentsystem.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private String email;
    private String password;
    private String confirmPassword;
    private String s;
    private Utils.AccountType accountType = Utils.AccountType.STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        
        binding.studentRadioButton.toggle();
        signUpClickAction();

        binding.loadingSection.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        binding.accountTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.studentRadioButton:
                        accountType = Utils.AccountType.STUDENT;
                        binding.userFullName.setHint("Full Name");
                        binding.companyLocation.setVisibility(View.GONE);
                        binding.userCollegeRollNo.setVisibility(View.VISIBLE);
                        break;
                    case R.id.adminRadioButton:
                        accountType = Utils.AccountType.ADMIN;
                        binding.userFullName.setHint("Full Name");
                        binding.companyLocation.setVisibility(View.GONE);
                        binding.userCollegeRollNo.setVisibility(View.GONE);
                        break;
                    case R.id.companyRadioButton:
                        accountType = Utils.AccountType.COMPANY;
                        binding.userFullName.setHint("Company Name");
                        binding.companyLocation.setVisibility(View.VISIBLE);
                        binding.userCollegeRollNo.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void signUpClickAction() {
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.userFullName.getEditText().getText().toString().isEmpty()) {
                    binding.userFullName.requestFocus();
                    if(accountType == Utils.AccountType.COMPANY)
                        binding.userFullName.getEditText().setError("Company Name is required");
                    else
                        binding.userFullName.getEditText().setError("Full Name is required");
                    return;
                }

                if(accountType == Utils.AccountType.STUDENT &&
                        binding.userCollegeRollNo.getEditText().getText().toString().isEmpty()) {
                    binding.userCollegeRollNo.requestFocus();
                    binding.userCollegeRollNo.getEditText().setError("Roll No. is required");
                    return;
                }

                email = binding.userEmailId.getEditText().getText().toString();
                password = binding.userPassword.getEditText().getText().toString();
                confirmPassword = binding.userConfirmPassword.getEditText().getText().toString();

                if(email.isEmpty()){
                    binding.userEmailId.requestFocus();
                    binding.userEmailId.getEditText().setError("Email id is required");
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.userEmailId.requestFocus();
                    binding.userEmailId.getEditText().setError("Enter a valid email id");
                    return;
                }

                if(binding.userContactNo.getEditText().getText().toString().isEmpty()) {
                    binding.userContactNo.requestFocus();
                    binding.userContactNo.getEditText().setError("User Contact No is required");
                    return;
                }

                if(binding.userContactNo.getEditText().getText().toString().length() != 10) { // !Patterns.PHONE.matcher(binding.userContactNo.getEditText().getText().toString()).matches()
                    binding.userContactNo.requestFocus();
                    binding.userContactNo.getEditText().setError("Enter a valid phone number");
                    return;
                }

                if(password.isEmpty()){
                    binding.userPassword.requestFocus();
                    binding.userPassword.getEditText().setError("Password is required");
                    return;
                }

                if(confirmPassword.length()<6) {
                    binding.userConfirmPassword.requestFocus();
                    binding.userConfirmPassword.getEditText().setError("Password length must be minimum 6");
                    return;
                }

                if(password.equals(confirmPassword)) {
                    if(Utils.isNetworkConnected(getApplicationContext())) {
                        binding.loadingSection.getRoot().setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Intent i = null;
                                    switch (accountType) {
                                        case STUDENT:
                                            i = new Intent(SignUpActivity.this, StudentDashboardActivity.class);
                                            userRef = FirebaseDatabase.getInstance().getReference().child("Student")
                                                    .child(mAuth.getCurrentUser().getUid()).child("Profile");
                                            userRef.child("roll_no").setValue(binding.userCollegeRollNo.getEditText().getText().toString());
                                            break;
                                        case COMPANY:
                                            i = new Intent(SignUpActivity.this, CompanyDashboardActivity.class);
                                            userRef = FirebaseDatabase.getInstance().getReference().child("Company")
                                                    .child(mAuth.getCurrentUser().getUid()).child("Profile");
                                            userRef.child("company_location").setValue(binding.companyLocation.getEditText().getText().toString());
                                            break;
                                        case ADMIN:
                                            i = new Intent(SignUpActivity.this, AdminDashboardActivity.class);
                                            userRef = FirebaseDatabase.getInstance().getReference().child("Admin")
                                                    .child(mAuth.getCurrentUser().getUid()).child("Profile");
                                            break;
                                    }

                                    userRef.child("full_name").setValue(binding.userFullName.getEditText().getText().toString());
                                    userRef.child("email_id").setValue(binding.userEmailId.getEditText().getText().toString());
                                    userRef.child("contact_no").setValue(binding.userContactNo.getEditText().getText().toString());

                                    i.putExtra("accountType", accountType);
                                    startActivity(i);
                                    finish();
                                } else {
                                    if (task.getException() instanceof FirebaseAuthEmailException) {
                                        Toast.makeText(getApplicationContext(), "User already registered", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(SignUpActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG);
                                        return;
                                    }
                                }
                            }
                        });
                    } else {
                        Snackbar.make(binding.getRoot(), "Internet not connected!",Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(binding.getRoot(), "Confirm Password & Password doesn't match",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}