package com.example.appclothes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView txtProfile = findViewById(R.id.txtProfile);
        txtProfile.setText("Tên: Admin\nĐịa chỉ: Hà Nội\nSĐT: 0123456789");
    }
}
