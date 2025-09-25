package com.example.appclothes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Load image từ URL nếu có, nếu không thì dùng drawable
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Log.d(TAG, "Loading image for " + product.getName() + ": " + product.getImageUrl());
            // Kiểm tra nếu là drawable resource
            if (product.getImageUrl().startsWith("drawable://")) {
                String drawableName = product.getImageUrl().replace("drawable://", "");
                int drawableId = getDrawableIdByName(drawableName);
                Log.d(TAG, "Drawable name: " + drawableName + ", ID: " + drawableId);
                if (drawableId != 0) {
                    holder.imgProduct.setImageResource(drawableId);
                } else {
                    Log.w(TAG, "Drawable not found: " + drawableName);
                    holder.imgProduct.setImageResource(R.drawable.aothun1); // fallback
                }
            } else {
                // Load từ URL thực tế
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.aothun1) // Placeholder khi đang load
                        .error(R.drawable.aothun1) // Fallback khi load lỗi
                        .into(holder.imgProduct);
            }
        } else {
            Log.d(TAG, "No imageUrl for " + product.getName() + ", using fallback");
            // Fallback cho compatibility với code cũ
            holder.imgProduct.setImageResource(product.getImage());
        }

        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(product.getPrice() + (product.getPrice().contains("đ") ? "" : "đ"));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("image", product.getImage()); // Backward compatibility
            intent.putExtra("desc", product.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Helper method để get drawable ID từ tên
    private int getDrawableIdByName(String drawableName) {
        try {
            return context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
