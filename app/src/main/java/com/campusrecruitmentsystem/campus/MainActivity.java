package com.campusrecruitmentsystem.campus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusrecruitmentsystem.campus.dashboard.AdminDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.CompanyDashboardActivity;
import com.campusrecruitmentsystem.campus.dashboard.StudentDashboardActivity;
import com.dhruv.campusrecruitmentsystem.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        binding.loadingSection.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(currentUser != null) {
            binding.loadingSection.getRoot().setVisibility(View.VISIBLE);
            if (Utils.isNetworkConnected(getApplicationContext())) {
                navigateToDashboard();
            } else {
                Snackbar.make(binding.getRoot(), "Internet not connected!", Snackbar.LENGTH_LONG).show();
                binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
                mAuth.signOut();
            }
        } else {
            binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
        }
    }

    public void goToSignup(View v) {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    public void onLogin(View view) {
        String loginEmail = binding.userEmailId.getEditText().getText().toString();
        String loginPassword = binding.userPassword.getEditText().getText().toString();

        if(loginEmail.isEmpty()) {
            binding.userEmailId.requestFocus();
            binding.userEmailId.getEditText().setError("Email id is required");
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
            binding.userEmailId.requestFocus();
            binding.userEmailId.getEditText().setError("Enter a valid email id");
            return;
        }

        if(loginPassword.isEmpty()) {
            binding.userPassword.requestFocus();
            binding.userPassword.getEditText().setError("Password is required");
            return;
        }

        if(loginPassword.length()<6) {
            binding.userPassword.requestFocus();
            binding.userPassword.getEditText().setError("Password length must be minimum 6");
            return;
        }

        // LOADING AND KEYBOARD HIDE
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

        if(Utils.isNetworkConnected(getApplicationContext())) {
            binding.loadingSection.getRoot().setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
                        Snackbar.make(binding.getRoot(), "Error: The password or email is invalid", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Snackbar.make(binding.getRoot(), "Internet not connected!", Snackbar.LENGTH_LONG).show();
        }
    }

    public void navigateToDashboard(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child("Student").hasChild(currentUser.getUid())) {
                    Intent intent = new Intent(getApplicationContext(), StudentDashboardActivity.class);
                    if(snapshot.child("Student").child(currentUser.getUid()).hasChild("is_blocked"))
                        isBlocked = (Boolean) snapshot.child("Student").child(currentUser.getUid()).child("is_blocked").getValue();
                    intent.putExtra("isBlocked", isBlocked);
                    intent.putExtra("accountType", Utils.AccountType.STUDENT);
                    startActivity(intent);
                    finish();
                } else if (snapshot.child("Company").hasChild(currentUser.getUid())) {
                    Intent intent = new Intent(getApplicationContext(), CompanyDashboardActivity.class);
                    if(snapshot.child("Company").child(currentUser.getUid()).hasChild("is_blocked"))
                        isBlocked = (Boolean) snapshot.child("Company").child(currentUser.getUid()).child("is_blocked").getValue();
                    intent.putExtra("isBlocked", isBlocked);
                    intent.putExtra("accountType", Utils.AccountType.COMPANY);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
                    intent.putExtra("accountType", Utils.AccountType.ADMIN);
                    startActivity(intent);
                    finish();
                }
                binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                binding.loadingSection.getRoot().setVisibility(View.INVISIBLE);
                Snackbar.make(binding.getRoot(), "Error: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}