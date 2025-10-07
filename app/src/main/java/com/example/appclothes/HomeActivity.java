package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    RecyclerView recyclerView;
    ProductAdapter adapter;
    ArrayList<Product> productList;
    Button btnCart, btnProfile, btnAdmin;
    FirebaseFirestore clothesDB;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupFirebase();
        checkAuthentication();
        setupRecyclerView();
        setupButtons();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnCart = findViewById(R.id.btnCart);
        btnProfile = findViewById(R.id.btnProfile);
        btnAdmin = findViewById(R.id.btnAdmin);
    }

    private void setupFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            clothesDB = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firebase initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase: " + e.getMessage());
        }
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User chưa đăng nhập, chuyển về MainActivity
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }
        Log.d(TAG, "User logged in: " + currentUser.getEmail());

        // Kiểm tra quyền admin để hiển thị nút Admin
        checkAdminRole(currentUser.getUid());
    }

    private void checkAdminRole(String userId) {
        clothesDB.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        if ("admin".equals(role)) {
                            btnAdmin.setVisibility(View.VISIBLE);
                            Log.d(TAG, "User is admin, showing admin button");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user role", e);
                });
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        loadProductsFromClothesDB();
    }

    private void loadProductsFromClothesDB() {
        DatabasePopulator.loadProductsFromClothesDB(new DatabasePopulator.ProductLoadCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    productList.clear();
                    productList.addAll(products);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + productList.size() + " products from clothesDB");
                    }
                } else {
                    Log.d(TAG, "No products found, populating database...");
                    populateAndLoadProducts();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading products from clothesDB: " + e.getMessage());
                populateAndLoadProducts();
            }
        });
    }

    private void populateAndLoadProducts() {
        Log.d(TAG, "Populating clothesDB with sample products");
        DatabasePopulator.populateClothesDatabase();

        // Tải lại sau khi populate
        DatabasePopulator.loadProductsFromClothesDB(new DatabasePopulator.ProductLoadCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    productList.clear();
                    productList.addAll(products);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + productList.size() + " products after populating");
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading products after populating: " + e.getMessage());
            }
        });
    }

    private void setupButtons() {
        btnCart.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        btnAdmin.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AdminActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh product data khi quay về từ màn hình khác
        // Đặc biệt hữu ích khi admin cập nhật sản phẩm
        Log.d(TAG, "HomeActivity resumed, refreshing product data");
        loadProductsFromClothesDB();
    }
}
