package com.example.appclothes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView txtTotal;
    CartAdapter cartAdapter;

    public static List<Product> cartList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerCart);
        txtTotal = findViewById(R.id.txtTotal);

        cartAdapter = new CartAdapter(this, cartList, this::updateTotal);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (Product p : cartList) {
            String cleanPrice = p.getPrice().replaceAll("[^0-9]", "");
            int price = Integer.parseInt(cleanPrice);
            total += price * p.getQuantity();
        }
        if (cartList.isEmpty()) {
            txtTotal.setText("Giỏ hàng trống");
        } else {
            txtTotal.setText("Tổng: " + String.format("%,d", total) + "đ");
        }
    }
}
