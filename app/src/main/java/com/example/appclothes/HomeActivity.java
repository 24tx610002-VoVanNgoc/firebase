package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProductAdapter adapter;
    ArrayList<Product> productList;
    Button btnCart, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        btnCart = findViewById(R.id.btnCart);
        btnProfile = findViewById(R.id.btnProfile);

        productList = new ArrayList<>();
        productList.add(new Product("Áo thun nam", "120.000đ", R.drawable.aothun1, "Áo thun nam cotton thoáng mát"));
        productList.add(new Product("Áo thun nữ", "150.000đ", R.drawable.aothun2, "Áo thun nữ form rộng"));
        productList.add(new Product("Quần jeans nam", "250.000đ", R.drawable.quan1, "Quần jeans nam phong cách"));
        productList.add(new Product("Quần jeans nữ", "270.000đ", R.drawable.quan2, "Quần jeans nữ co giãn"));

        adapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        btnCart.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }
}
