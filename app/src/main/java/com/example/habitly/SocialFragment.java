package com.example.habitly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habitly.databinding.FragmentSocialBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SocialFragment extends Fragment {

    private FragmentSocialBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private HabitViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSocialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        updateUI(mAuth.getCurrentUser());

        binding.btnGoogleLogin.setOnClickListener(v -> signIn());
        binding.btnAddFriend.setOnClickListener(v -> addFriend());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loginLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                checkUserInFirestore(user);
            } else {
                Toast.makeText(getContext(), "Auth failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserInFirestore(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    createNewUser(user);
                } else {
                    updateUI(user);
                    syncScore();
                }
            }
        });
    }

    private void createNewUser(FirebaseUser user) {
        String friendCode = generateFriendCode();
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("displayName", user.getDisplayName());
        userData.put("friendCode", friendCode);
        userData.put("score", 0);

        db.collection("users").document(user.getUid()).set(userData).addOnSuccessListener(aVoid -> {
            updateUI(user);
            syncScore();
        });
    }

    private String generateFriendCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void syncScore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Fetching total completions from local habits
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            int total = 0;
            if (habits != null) {
                for (Habit h : habits) total += h.getTotalCompletions();
            }
            db.collection("users").document(user.getUid()).update("score", total);
        });
    }

    private void addFriend() {
        String code = binding.etFriendCode.getText().toString().trim();
        if (code.isEmpty()) return;

        db.collection("users").whereEqualTo("friendCode", code).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                String targetUid = queryDocumentSnapshots.getDocuments().get(0).getId();
                sendFriendRequest(targetUid);
            } else {
                Toast.makeText(getContext(), "Friend not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFriendRequest(String targetUid) {
        String myUid = mAuth.getCurrentUser().getUid();
        Map<String, Object> request = new HashMap<>();
        request.put("from", myUid);
        request.put("to", targetUid);
        request.put("status", "accepted"); // Auto-accepting for simplicity in this demo requirement

        db.collection("friendships").add(request).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Friend added!", Toast.LENGTH_SHORT).show();
            loadLeaderboard();
        });
    }

    private void loadLeaderboard() {
        String myUid = mAuth.getCurrentUser().getUid();
        db.collection("friendships")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> friendUids = new ArrayList<>();
                    friendUids.add(myUid); // Include self
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String from = doc.getString("from");
                        String to = doc.getString("to");
                        if (from.equals(myUid)) friendUids.add(to);
                        else if (to.equals(myUid)) friendUids.add(from);
                    }
                    queryFriendsScores(friendUids);
                });
    }

    private void queryFriendsScores(List<String> uids) {
        if (uids.isEmpty()) return;
        db.collection("users")
                .whereIn("uid", uids)
                .orderBy("score", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardAdapter.Friend> leaderboard = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        leaderboard.add(new LeaderboardAdapter.Friend(
                                doc.getString("displayName"),
                                doc.getLong("score").intValue()
                        ));
                    }
                    setupRecyclerView(leaderboard);
                });
    }

    private void setupRecyclerView(List<LeaderboardAdapter.Friend> data) {
        binding.rvSocialLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSocialLeaderboard.setAdapter(new LeaderboardAdapter(data));
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.layoutLogin.setVisibility(View.GONE);
            binding.layoutSocial.setVisibility(View.VISIBLE);
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) binding.tvFriendCode.setText(doc.getString("friendCode"));
            });
            loadLeaderboard();
        } else {
            binding.layoutLogin.setVisibility(View.VISIBLE);
            binding.layoutSocial.setVisibility(View.GONE);
        }
    }
}