package com.example.appclothes;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DatabasePopulator {

  private static final String TAG = "DatabasePopulator";

  public static void populateClothesDatabase() {
    try {
      FirebaseFirestore clothesDB = FirebaseFirestore.getInstance();

      // Tạo 10 sản phẩm mẫu với Firestore
      List<Product> products = new ArrayList<>();
      products.add(new Product("prod1", "Áo thun nam cotton", "120000", "drawable://aothun1",
          "Áo thun nam cotton 100% thoáng mát, form regular", "áo", true));
      products.add(new Product("prod2", "Áo thun nữ form rộng", "150000", "drawable://aothun2",
          "Áo thun nữ form rộng trendy, chất liệu cotton mềm mại", "áo", true));
      products.add(new Product("prod3", "Quần jeans nam slim", "250000", "drawable://quan1",
          "Quần jeans nam form slim, chất liệu denim cao cấp", "quần", true));
      products.add(new Product("prod4", "Quần jeans nữ skinny", "270000", "drawable://quan2",
          "Quần jeans nữ skinny co giãn, ôm dáng tôn vóc dáng", "quần", true));
      products.add(new Product("prod5", "Áo sơ mi trắng nam", "200000", "drawable://aothun3",
          "Áo sơ mi trắng nam công sở, chất liệu cotton cao cấp", "áo", true));
      products.add(new Product("prod6", "Váy midi nữ", "180000", "drawable://aothun4",
          "Váy midi nữ thanh lịch, phù hợp đi làm và dự tiệc", "váy", true));
      products.add(new Product("prod7", "Quần short nam", "90000", "drawable://quan3",
          "Quần short nam thể thao, chất liệu polyester thấm hút mồ hôi", "quần", true));
      products.add(new Product("prod8", "Áo khoác hoodie", "300000", "drawable://quan4",
          "Áo khoác hoodie unisex, chất liệu nỉ ấm áp", "áo", true));
      products.add(new Product("prod9", "Chân váy ngắn", "120000", "drawable://quan5",
          "Chân váy ngắn trẻ trung, phù hợp mix đồ casual", "váy", true));
      products.add(new Product("prod10", "Quần tây nam", "350000", "drawable://aothun1",
          "Quần tây nam formal, chất liệu wool blend cao cấp", "quần", true));

      Log.d(TAG, "Starting to upload " + products.size() + " products to Firestore clothesDB");

      // Upload từng product lên Firestore
      for (Product product : products) {
        if (product != null && product.getId() != null) {
          clothesDB.collection("products")
              .document(product.getId())
              .set(product)
              .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Successfully uploaded product: " + product.getName());
              })
              .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload product: " + product.getName() + " - " + e.getMessage());
              });
        } else {
          Log.w(TAG, "Skipping null product or product without ID");
        }
      }

      Log.d(TAG, "All products upload requests sent to clothesDB");
    } catch (Exception e) {
      Log.e(TAG, "Error in populateClothesDatabase: " + e.getMessage());
    }
  }

  public static void loadProductsFromClothesDB(ProductLoadCallback callback) {
    try {
      FirebaseFirestore clothesDB = FirebaseFirestore.getInstance();

      clothesDB.collection("products")
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
            List<Product> productList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
              Product product = doc.toObject(Product.class);
              if (product != null) {
                productList.add(product);
              }
            }

            // In ra hoặc dùng list
            for (Product p : productList) {
              Log.d("ClothesDB", "Product: " + p.getName() + " - " + p.getPrice() + "đ");
            }

            // Callback để trả dữ liệu về HomeActivity
            if (callback != null) {
              callback.onProductsLoaded(productList);
            }

            Log.d(TAG, "Loaded " + productList.size() + " products from clothesDB");
          })
          .addOnFailureListener(e -> {
            Log.e("ClothesDB", "Error getting products", e);
            if (callback != null) {
              callback.onError(e);
            }
          });
    } catch (Exception e) {
      Log.e(TAG, "Error in loadProductsFromClothesDB: " + e.getMessage());
      if (callback != null) {
        callback.onError(e);
      }
    }
  }

  public interface ProductLoadCallback {
    void onProductsLoaded(List<Product> products);

    void onError(Exception e);
  }
}