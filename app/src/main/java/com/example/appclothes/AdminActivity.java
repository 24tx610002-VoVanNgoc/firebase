package com.example.appclothes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements AdminProductAdapter.OnAdminProductListener {

  private static final String TAG = "AdminActivity";

  private Button btnAddProduct;
  private ProgressBar progressBar;
  private RecyclerView recyclerAdminProducts;
  private AdminProductAdapter adminAdapter;

  private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private List<Product> productList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin);

    // Thêm back button trong action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setTitle("Quản lý sản phẩm");
    }

    initViews();
    initFirebase();
    checkAdminPermission();
    setupRecyclerView();
    setupClickListeners();
    loadProducts();
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
    btnAddProduct = findViewById(R.id.btnAddProduct);
    progressBar = findViewById(R.id.progressBar);
    recyclerAdminProducts = findViewById(R.id.recyclerAdminProducts);
  }

  private void initFirebase() {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    productList = new ArrayList<>();
  }

  private void checkAdminPermission() {
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
      Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
      startActivity(new Intent(this, MainActivity.class));
      finish();
      return;
    }

    // Kiểm tra quyền admin trong Firestore
    db.collection("users").document(currentUser.getUid())
        .get()
        .addOnSuccessListener(document -> {
          if (document.exists()) {
            String role = document.getString("role");
            if (!"admin".equals(role)) {
              Toast.makeText(this, "Bạn không có quyền truy cập trang này", Toast.LENGTH_LONG).show();
              startActivity(new Intent(this, HomeActivity.class));
              finish();
            }
          } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
          }
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error checking admin permission", e);
          Toast.makeText(this, "Lỗi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          finish();
        });
  }

  private void setupRecyclerView() {
    adminAdapter = new AdminProductAdapter(this, productList, this);
    recyclerAdminProducts.setLayoutManager(new LinearLayoutManager(this));
    recyclerAdminProducts.setAdapter(adminAdapter);
  }

  private void setupClickListeners() {
    btnAddProduct.setOnClickListener(v -> {
      startActivity(new Intent(this, AddProductActivity.class));
    });
  }

  private void loadProducts() {
    showLoading(true);

    db.collection("products")
        .get()
        .addOnCompleteListener(task -> {
          showLoading(false);
          if (task.isSuccessful()) {
            productList.clear();
            for (QueryDocumentSnapshot document : task.getResult()) {
              Product product = document.toObject(Product.class);
              product.setId(document.getId());
              productList.add(product);
            }
            adminAdapter.notifyDataSetChanged();
            Log.d(TAG, "Loaded " + productList.size() + " products for admin");
          } else {
            Log.e(TAG, "Error loading products", task.getException());
            Toast.makeText(this, "Lỗi tải sản phẩm: " + task.getException().getMessage(),
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  @Override
  public void onEditProduct(Product product) {
    Intent intent = new Intent(this, EditProductActivity.class);
    intent.putExtra("productId", product.getId());
    intent.putExtra("productName", product.getName());
    intent.putExtra("productPrice", product.getPrice());
    intent.putExtra("productImageUrl", product.getImageUrl());
    intent.putExtra("productDescription", product.getDescription());
    intent.putExtra("productCategory", product.getCategory());
    intent.putExtra("productInStock", product.isInStock());
    startActivity(intent);
  }

  @Override
  public void onDeleteProduct(Product product) {
    // Hiển thị dialog xác nhận
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Xác nhận xóa")
        .setMessage("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\"?")
        .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
        .setNegativeButton("Hủy", null)
        .show();
  }

  private void deleteProduct(Product product) {
    showLoading(true);

    db.collection("products").document(product.getId())
        .delete()
        .addOnSuccessListener(aVoid -> {
          showLoading(false);
          Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
          loadProducts(); // Reload danh sách
        })
        .addOnFailureListener(e -> {
          showLoading(false);
          Log.e(TAG, "Error deleting product", e);
          Toast.makeText(this, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Reload products khi quay lại từ Add/Edit
    loadProducts();
  }

  private void showLoading(boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    btnAddProduct.setEnabled(!show);
  }
}