package com.example.appclothes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;
    private OnCartUpdateListener updateListener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<Product> cartList, OnCartUpdateListener updateListener) {
        this.context = context;
        this.cartList = cartList;
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);

        // Load hình ảnh
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            if (product.getImageUrl().startsWith("drawable://")) {
                String drawableName = product.getImageUrl().replace("drawable://", "");
                int drawableId = getDrawableIdByName(drawableName);
                if (drawableId != 0) {
                    holder.imgProduct.setImageResource(drawableId);
                } else {
                    holder.imgProduct.setImageResource(R.drawable.aothun1);
                }
            } else {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.aothun1)
                        .error(R.drawable.aothun1)
                        .into(holder.imgProduct);
            }
        } else {
            holder.imgProduct.setImageResource(product.getImage());
        }

        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(product.getPrice());
        holder.txtQuantity.setText("Số lượng: " + product.getQuantity());

        // Tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            notifyItemChanged(position);
            if (updateListener != null)
                updateListener.onCartUpdated();
        });

        // Giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                notifyItemChanged(position);
                if (updateListener != null)
                    updateListener.onCartUpdated();
            }
        });

        // Xoá sản phẩm
        holder.btnRemove.setOnClickListener(v -> {
            if (context instanceof CartActivity) {
                ((CartActivity) context).removeItemFromCart(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // Helper method để get drawable ID từ tên
    private int getDrawableIdByName(String drawableName) {
        try {
            return context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtQuantity;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProductCart);
            txtName = itemView.findViewById(R.id.txtNameCart);
            txtPrice = itemView.findViewById(R.id.txtPriceCart);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
