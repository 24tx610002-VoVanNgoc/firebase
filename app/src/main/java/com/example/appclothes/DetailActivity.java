
package com.example.appclothes;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    ImageView imgDetail;
    TextView txtNameDetail, txtPriceDetail, txtDescDetail;
    Button btnAddToCart;
    ImageView btnBackHome;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Thêm back button trong action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
        }

        initViews();
        initFirebase();
        loadProductData();
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
        imgDetail = findViewById(R.id.imgDetail);
        txtNameDetail = findViewById(R.id.txtNameDetail);
        txtPriceDetail = findViewById(R.id.txtPriceDetail);
        txtDescDetail = findViewById(R.id.txtDescDetail);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBackHome = findViewById(R.id.btnBackHome);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void loadProductData() {
        // Lấy thông tin sản phẩm từ Intent
        String productId = getIntent().getStringExtra("productId");
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String desc = getIntent().getStringExtra("desc");
        int image = getIntent().getIntExtra("image", 0);

        // Tạo Product object
        currentProduct = new Product();
        currentProduct.setId(productId);
        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setImageUrl(imageUrl);
        currentProduct.setDescription(desc);

        // Hiển thị thông tin
        txtNameDetail.setText(name);
        txtPriceDetail.setText(price);
        txtDescDetail.setText(desc);

        // Load hình ảnh
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load từ URL bằng Glide
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.aothun1) // Placeholder khi đang load
                    .error(R.drawable.aothun1) // Hình mặc định khi lỗi
                    .into(imgDetail);
        } else if (image != 0) {
            // Load từ resource (backward compatibility)
            imgDetail.setImageResource(image);
        } else {
            // Hình mặc định
            imgDetail.setImageResource(R.drawable.aothun1);
        }
    }

    private void setupClickListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void addToCart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        addToFirestoreCart(currentUser.getUid());

        // Thêm vào cart local để backward compatibility
        CartActivity.cartList.add(currentProduct);
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void addToFirestoreCart(String userId) {
        // Tạo cart item
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", currentProduct.getId());
        cartItem.put("productName", currentProduct.getName());
        cartItem.put("productPrice", currentProduct.getPrice());
        cartItem.put("productImageUrl", currentProduct.getImageUrl());
        cartItem.put("quantity", 1);
        cartItem.put("addedAt", System.currentTimeMillis());

        // Thêm vào collection carts/{userId}/items
        db.collection("carts")
                .document(userId)
                .collection("items")
                .document(currentProduct.getId() != null ? currentProduct.getId()
                        : String.valueOf(System.currentTimeMillis()))
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật timestamp của cart
                    Map<String, Object> cartUpdate = new HashMap<>();
                    cartUpdate.put("updatedAt", System.currentTimeMillis());
                    db.collection("carts").document(userId).set(cartUpdate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi thêm vào giỏ hàng: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
