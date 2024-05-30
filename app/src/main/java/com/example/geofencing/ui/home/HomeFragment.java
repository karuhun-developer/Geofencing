package com.example.geofencing.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.geofencing.Config;
import com.example.geofencing.R;
import com.example.geofencing.adapter.ChildAdapter;
import com.example.geofencing.databinding.FragmentHomeBinding;
import com.example.geofencing.dialog.ChildCodeDialog;
import com.example.geofencing.dialog.DeleteChildDialog;
import com.example.geofencing.model.Child;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference DB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupEventListener();
    }

    private void setupEventListener() {
        binding.fabAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_navigation_home_to_addChildFragment);
            }
        });
    }

    private void setupRecyclerView() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get data from db
        DB = FirebaseDatabase.getInstance(Config.getDB_URL()).getReference("users/" + uid + "/childs");
        DB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Child> childList = new ArrayList<>();

                int i = 0;
                for (DataSnapshot clidSnapshot: dataSnapshot.getChildren()) {
                    i++;

                    Log.d("child", clidSnapshot.getKey());
                    childList.add(new Child(i, clidSnapshot.child("name").getValue(String.class), clidSnapshot.getKey()));
                }

                ChildAdapter adapter = new ChildAdapter(childList);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
                binding.recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new ChildAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int i) {
                        ChildCodeDialog childCodeDialog = new ChildCodeDialog(childList.get(i).getName());
                        childCodeDialog.show(getParentFragmentManager(), "child_code");
                    }
                });

                adapter.setOnItemLongClickListener(new ChildAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(View view, int i) {
                        DeleteChildDialog deleteChildDialog = new DeleteChildDialog(childList.get(i).getId(), childList.get(i).getName());
                        deleteChildDialog.show(getParentFragmentManager(), "delete_child");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Error", databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}