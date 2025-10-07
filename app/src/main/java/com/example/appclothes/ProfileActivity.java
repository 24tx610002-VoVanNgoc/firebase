package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtProfile;
    private Button btnLogout;
    private ImageView btnBackHome;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Thêm back button trong action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thông tin tài khoản");
        }

        initViews();
        initFirebase();
        loadUserProfile();
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private void initViews() {
        txtProfile = findViewById(R.id.txtProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();

        // Hiển thị thông tin cơ bản trước
        txtProfile.setText("Email: " + email + "\nĐang tải thông tin...");

        // Tải thông tin chi tiết từ Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            Long createdAt = document.getLong("createdAt");

                            String profileInfo = "Email: " + email + "\n" +
                                    "Vai trò: " + (role != null ? role : "user") + "\n" +
                                    "UID: " + userId;

                            if (createdAt != null) {
                                profileInfo += "\nTạo tài khoản: " + new java.util.Date(createdAt);
                            }

                            txtProfile.setText(profileInfo);
                        } else {
                            txtProfile.setText("Email: " + email + "\nKhông tìm thấy thông tin profile");
                        }
                    } else {
                        txtProfile.setText("Email: " + email + "\nLỗi tải thông tin: " +
                                task.getException().getMessage());
                    }
                });
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }
}
