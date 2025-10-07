
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

public class AddProductActivity extends AppCompatActivity {

  private EditText edtProductName, edtProductPrice, edtProductImageUrl,
      edtProductCategory, edtProductDescription;
  private CheckBox cbInStock;
  private Button btnSaveProduct, btnCancel;
  private ProgressBar progressBar;

  private FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_product);

    // Thêm back button trong action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setTitle("Thêm sản phẩm");
    }

    initViews();
    initFirebase();
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
    btnSaveProduct = findViewById(R.id.btnSaveProduct);
    btnCancel = findViewById(R.id.btnCancel);
    progressBar = findViewById(R.id.progressBar);
  }

  private void initFirebase() {
    db = FirebaseFirestore.getInstance();
  }

  private void setupClickListeners() {
    btnSaveProduct.setOnClickListener(v -> saveProduct());
    btnCancel.setOnClickListener(v -> finish());
  }

  private void saveProduct() {
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

    // Tạo product data
    Map<String, Object> product = new HashMap<>();
    product.put("name", name);
    product.put("price", formattedPrice);
    product.put("imageUrl", imageUrl.isEmpty() ? "drawable://aothun1" : imageUrl);
    product.put("description", description);
    product.put("category", category.isEmpty() ? "Chưa phân loại" : category);
    product.put("inStock", inStock);
    product.put("createdAt", System.currentTimeMillis());

    // Thêm vào Firestore
    db.collection("products")
        .add(product)
        .addOnSuccessListener(documentReference -> {
          showLoading(false);
          Toast.makeText(this, "Đã thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
          finish();
        })
        .addOnFailureListener(e -> {
          showLoading(false);
          Toast.makeText(this, "Lỗi thêm sản phẩm: " + e.getMessage(),
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
    btnSaveProduct.setEnabled(!show);
  }
}