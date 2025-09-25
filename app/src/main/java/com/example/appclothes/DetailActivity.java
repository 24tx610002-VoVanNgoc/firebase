package com.example.appclothes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    ImageView imgDetail;
    TextView txtNameDetail, txtPriceDetail, txtDescDetail;
    Button btnAddToCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgDetail = findViewById(R.id.imgDetail);
        txtNameDetail = findViewById(R.id.txtNameDetail);
        txtPriceDetail = findViewById(R.id.txtPriceDetail);
        txtDescDetail = findViewById(R.id.txtDescDetail);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int image = getIntent().getIntExtra("image", 0);
        String desc = getIntent().getStringExtra("desc");

        txtNameDetail.setText(name);
        txtPriceDetail.setText(price);
        txtDescDetail.setText(desc);
        imgDetail.setImageResource(image);

        btnAddToCart.setOnClickListener(v -> {
            CartActivity.cartList.add(new Product(name, price, image, desc));
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });
    }
}
