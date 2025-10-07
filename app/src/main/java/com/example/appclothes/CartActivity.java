package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdateListener {

    private static final String TAG = "CartActivity";

    RecyclerView recyclerView;
    TextView txtTotal;
    Button btnCheckout;
    ProgressBar progressBar;
    CartAdapter cartAdapter;
    ImageButton btnBackHome;

    public static List<Product> cartList = new ArrayList<>(); // Backward compatibility

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<CartItem> firestoreCartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Thêm back button trong action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng");
        }

        initViews();
        initFirebase();
        setupRecyclerView();
        loadCartFromFirestore();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Luôn về HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        progressBar = findViewById(R.id.progressBar);
        btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firestoreCartItems = new ArrayList<>();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
    }

    private void loadCartFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        showLoading(true);

        db.collection("carts")
                .document(currentUser.getUid())
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        cartList.clear();
                        firestoreCartItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                CartItem cartItem = document.toObject(CartItem.class);
                                cartItem.setDocumentId(document.getId());
                                firestoreCartItems.add(cartItem);

                                // Convert to Product for backward compatibility
                                Product product = new Product();
                                product.setId(cartItem.getProductId());
                                product.setName(cartItem.getProductName());
                                product.setPrice(cartItem.getProductPrice());
                                product.setImageUrl(cartItem.getProductImageUrl());
                                product.setQuantity(cartItem.getQuantity());
                                cartList.add(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi mapping dữ liệu giỏ hàng Firestore:", e);
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateTotal();
                        Log.d(TAG, "Loaded " + cartList.size() + " items from Firestore cart");
                    } else {
                        Log.e(TAG, "Error loading cart", task.getException());
                        Toast.makeText(this,
                                "Lỗi tải giỏ hàng: "
                                        + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                        // Fallback to local cart
                        updateTotal();
                    }
                });
    }

    @Override
    public void onCartUpdated() {
        updateTotal();
        syncCartWithFirestore();
    }

    private void updateTotal() {
        long total = 0;
        for (Product p : cartList) {
            try {
                String cleanPrice = p.getPrice().replaceAll("[^0-9]", "");
                long price = Long.parseLong(cleanPrice);
                total += price * p.getQuantity();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing price: " + p.getPrice());
            }
        }

        if (cartList.isEmpty()) {
            txtTotal.setText("Giỏ hàng trống");
            btnCheckout.setEnabled(false);
        } else {
            txtTotal.setText("Tổng: " + String.format("%,d", total) + "đ");
            btnCheckout.setEnabled(true);
        }
    }

    private void syncCartWithFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            return;

        // Cập nhật từng item
        for (int i = 0; i < cartList.size(); i++) {
            Product product = cartList.get(i);
            if (i < firestoreCartItems.size()) {
                CartItem cartItem = firestoreCartItems.get(i);
                cartItem.setQuantity(product.getQuantity());

                db.collection("carts")
                        .document(currentUser.getUid())
                        .collection("items")
                        .document(cartItem.getDocumentId())
                        .set(cartItem)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating cart item", e);
                        });
            }
        }
    }

    public void removeItemFromCart(int position) {
        if (position < 0 || position >= cartList.size())
            return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && position < firestoreCartItems.size()) {
            CartItem cartItem = firestoreCartItems.get(position);

            db.collection("carts")
                    .document(currentUser.getUid())
                    .collection("items")
                    .document(cartItem.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Cart item deleted from Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting cart item", e);
                    });

            firestoreCartItems.remove(position);
        }

        cartList.remove(position);
        cartAdapter.notifyItemRemoved(position);
        updateTotal();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // CartItem class for Firestore
    public static class CartItem {
        private String documentId;
        private String productId;
        private String productName;
        private String productPrice;
        private String productImageUrl;
        private int quantity;
        private long addedAt;

        public CartItem() {
        } // Required for Firestore

        // Getters and setters
        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(String productPrice) {
            this.productPrice = productPrice;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public long getAddedAt() {
            return addedAt;
        }

        public void setAddedAt(long addedAt) {
            this.addedAt = addedAt;
        }
    }
}
