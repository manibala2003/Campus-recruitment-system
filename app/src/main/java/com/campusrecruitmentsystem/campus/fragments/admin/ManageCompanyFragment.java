package com.campusrecruitmentsystem.campus.fragments.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.campusrecruitmentsystem.campus.Utils;
import com.campusrecruitmentsystem.campus.adapters.ManageCompanyAdapter;
import com.dhruv.campusrecruitmentsystem.databinding.FragmentManageCompanyBinding;
import com.campusrecruitmentsystem.campus.pojo.CompanyDetails;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class ManageCompanyFragment extends Fragment {

    private FragmentManageCompanyBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private ManageCompanyAdapter manageCompanyAdapter;
    private ArrayList<CompanyDetails> companyDetailsArrayList = new ArrayList<>();
    private Boolean isBlocked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageCompanyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference().child("Company");

        manageCompanyAdapter = new ManageCompanyAdapter(getContext(),companyDetailsArrayList,
                new ManageCompanyAdapter.OnClickListener() {
                    @Override
                    public void onBlockStatusChanged(int position, boolean isBlocked) {
                        companyDetailsArrayList.get(position).setBlocked(isBlocked);
                        mRootRef.child(companyDetailsArrayList.get(position).getUid()).child("is_blocked").setValue(isBlocked);
                    }
                });
        binding.companyRecyclerview.setAdapter(manageCompanyAdapter);

        if(Utils.isNetworkConnected(getContext())) {
            isBlocked = false;
            binding.loadingSection.getRoot().setVisibility(View.VISIBLE);
            mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    companyDetailsArrayList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        if(!dataSnapshot.hasChild("is_blocked"))
                            isBlocked = false;
                        else
                            isBlocked = (boolean) dataSnapshot.child("is_blocked").getValue();

                        DataSnapshot reference = dataSnapshot.child("Profile");
                        companyDetailsArrayList.add(new CompanyDetails(dataSnapshot.getKey(),
                                String.valueOf(reference.child("full_name").getValue()),
                                String.valueOf(reference.child("email_id").getValue()),
                                String.valueOf(reference.child("contact_no").getValue()),
                                String.valueOf(reference.child("company_location").getValue()),
                                isBlocked));
                    }
                    manageCompanyAdapter.setNotifyDatasetChanged(companyDetailsArrayList);
                    binding.loadingSection.getRoot().setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    binding.loadingSection.getRoot().setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(), "Error: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            Snackbar.make(binding.getRoot(), "Internet not connected!", Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}