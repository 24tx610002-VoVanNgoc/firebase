
package com.example.appclothes;

import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

  private EditText edtProductName, edtProductPrice, edtProductImageUrl,
      edtProductCategory, edtProductDescription;
  private CheckBox cbInStock;
  private Button btnUpdateProduct, btnCancel;
  private ProgressBar progressBar;

  private FirebaseFirestore db;
  private String productId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_product);

    // Thêm back button trong action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setTitle("Chỉnh sửa sản phẩm");
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
    edtProductName = findViewById(R.id.edtProductName);
    edtProductPrice = findViewById(R.id.edtProductPrice);
    edtProductImageUrl = findViewById(R.id.edtProductImageUrl);
    edtProductCategory = findViewById(R.id.edtProductCategory);
    edtProductDescription = findViewById(R.id.edtProductDescription);
    cbInStock = findViewById(R.id.cbInStock);
    btnUpdateProduct = findViewById(R.id.btnUpdateProduct);
    btnCancel = findViewById(R.id.btnCancel);
    progressBar = findViewById(R.id.progressBar);
  }

  private void initFirebase() {
    db = FirebaseFirestore.getInstance();
  }

  private void loadProductData() {
    // Lấy thông tin sản phẩm từ Intent
    productId = getIntent().getStringExtra("productId");
    String name = getIntent().getStringExtra("productName");
    String price = getIntent().getStringExtra("productPrice");
    String imageUrl = getIntent().getStringExtra("productImageUrl");
    String category = getIntent().getStringExtra("productCategory");
    String description = getIntent().getStringExtra("productDescription");
    boolean inStock = getIntent().getBooleanExtra("productInStock", true);

    // Hiển thị dữ liệu trong form
    edtProductName.setText(name);
    // Remove "đ" từ price để hiển thị trong EditText
    if (price != null && price.endsWith("đ")) {
      edtProductPrice.setText(price.substring(0, price.length() - 1));
    } else {
      edtProductPrice.setText(price);
    }
    edtProductImageUrl.setText(imageUrl);
    edtProductCategory.setText(category);
    edtProductDescription.setText(description);
    cbInStock.setChecked(inStock);
  }

  private void setupClickListeners() {
    btnUpdateProduct.setOnClickListener(v -> updateProduct());
    btnCancel.setOnClickListener(v -> finish());
  }

  private void updateProduct() {
    if (productId == null) {
      Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
      return;
    }

    String name = edtProductName.getText().toString().trim();
    String priceStr = edtProductPrice.getText().toString().trim();
    String imageUrl = edtProductImageUrl.getText().toString().trim();
    String category = edtProductCategory.getText().toString().trim();
    String description = edtProductDescription.getText().toString().trim();
    boolean inStock = cbInStock.isChecked();

    if (!validateInput(name, priceStr, description)) {
      return;
    }

    // Format price
    String formattedPrice = priceStr + "đ";

    showLoading(true);

    // Tạo product data để update
    Map<String, Object> updates = new HashMap<>();
    updates.put("name", name);
    updates.put("price", formattedPrice);
    updates.put("imageUrl", imageUrl.isEmpty() ? "drawable://aothun1" : imageUrl);
    updates.put("description", description);
    updates.put("category", category.isEmpty() ? "Chưa phân loại" : category);
    updates.put("inStock", inStock);
    updates.put("updatedAt", System.currentTimeMillis());

    // Cập nhật trong Firestore
    db.collection("products").document(productId)
        .update(updates)
        .addOnSuccessListener(aVoid -> {
          showLoading(false);
          Toast.makeText(this, "Đã cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
          finish();
        })
        .addOnFailureListener(e -> {
          showLoading(false);
          Toast.makeText(this, "Lỗi cập nhật sản phẩm: " + e.getMessage(),
              Toast.LENGTH_LONG).show();
        });
  }

  private boolean validateInput(String name, String price, String description) {
    if (TextUtils.isEmpty(name)) {
      edtProductName.setError("Vui lòng nhập tên sản phẩm");
      edtProductName.requestFocus();
      return false;
    }

    if (TextUtils.isEmpty(price)) {
      edtProductPrice.setError("Vui lòng nhập giá");
      edtProductPrice.requestFocus();
      return false;
    }

    try {
      Integer.parseInt(price);
    } catch (NumberFormatException e) {
      edtProductPrice.setError("Giá phải là số");
      edtProductPrice.requestFocus();
      return false;
    }

    if (TextUtils.isEmpty(description)) {
      edtProductDescription.setError("Vui lòng nhập mô tả");
      edtProductDescription.requestFocus();
      return false;
    }

    return true;
  }

  private void showLoading(boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    btnUpdateProduct.setEnabled(!show);
  }
}