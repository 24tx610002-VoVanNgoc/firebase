package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

  private EditText edtRegisterEmail, edtRegisterPassword, edtConfirmPassword;
  private CheckBox cbIsAdmin;
  private Button btnRegister;
  private TextView tvGoToLogin;
  private ProgressBar progressBar;

  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Thêm back button trong action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setTitle("Đăng ký tài khoản");
    }

    initViews();
    initFirebase();
    setupClickListeners();
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  private void initViews() {
    edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
    edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
    edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
    cbIsAdmin = findViewById(R.id.cbIsAdmin);
    btnRegister = findViewById(R.id.btnRegister);
    tvGoToLogin = findViewById(R.id.tvGoToLogin);
    progressBar = findViewById(R.id.progressBar);
  }

  private void initFirebase() {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
  }

  private void setupClickListeners() {
    btnRegister.setOnClickListener(v -> registerUser());
    tvGoToLogin.setOnClickListener(v -> {
      startActivity(new Intent(RegisterActivity.this, MainActivity.class));
      finish();
    });
  }

  private void registerUser() {
    String email = edtRegisterEmail.getText().toString().trim();
    String password = edtRegisterPassword.getText().toString().trim();
    String confirmPassword = edtConfirmPassword.getText().toString().trim();
    boolean isAdmin = cbIsAdmin.isChecked();

    if (!validateInput(email, password, confirmPassword)) {
      return;
    }

    showLoading(true);

    mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
          if (task.isSuccessful()) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
              createUserProfile(user.getUid(), email, isAdmin);
            }
          } else {
            showLoading(false);
            String error = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
          }
        });
  }

  private void createUserProfile(String userId, String email, boolean isAdmin) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("role", isAdmin ? "admin" : "user");
    user.put("createdAt", System.currentTimeMillis());

    db.collection("users").document(userId)
        .set(user)
        .addOnSuccessListener(aVoid -> {
          showLoading(false);
          Toast.makeText(RegisterActivity.this,
              "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

          // Chuyển đến HomeActivity
          startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
          finish();
        })
        .addOnFailureListener(e -> {
          showLoading(false);
          Toast.makeText(RegisterActivity.this,
              "Lỗi tạo profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private boolean validateInput(String email, String password, String confirmPassword) {
    if (TextUtils.isEmpty(email)) {
      edtRegisterEmail.setError("Vui lòng nhập email");
      edtRegisterEmail.requestFocus();
      return false;
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      edtRegisterEmail.setError("Email không hợp lệ");
      edtRegisterEmail.requestFocus();
      return false;
    }

    if (TextUtils.isEmpty(password)) {
      edtRegisterPassword.setError("Vui lòng nhập mật khẩu");
      edtRegisterPassword.requestFocus();
      return false;
    }

    if (password.length() < 6) {
      edtRegisterPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
      edtRegisterPassword.requestFocus();
      return false;
    }

    if (!password.equals(confirmPassword)) {
      edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
      edtConfirmPassword.requestFocus();
      return false;
    }

    return true;
  }

  private void showLoading(boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    btnRegister.setEnabled(!show);
  }
}