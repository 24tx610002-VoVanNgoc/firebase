package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    RecyclerView recyclerView;
    ProductAdapter adapter;
    ArrayList<Product> productList;
    Button btnCart, btnProfile;
    FirebaseFirestore clothesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerView();
        setupFirebase();
        setupButtons();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnCart = findViewById(R.id.btnCart);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void setupFirebase() {
        try {
            clothesDB = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firebase Firestore initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase: " + e.getMessage());
        }
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
    }
}
