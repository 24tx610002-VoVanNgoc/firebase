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

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;
    private Runnable updateTotalCallback;

    public CartAdapter(Context context, List<Product> cartList, Runnable updateTotalCallback) {
        this.context = context;
        this.cartList = cartList;
        this.updateTotalCallback = updateTotalCallback;
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

        holder.imgProduct.setImageResource(product.getImage());
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(product.getPrice());
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));

        // Tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            notifyItemChanged(position);
            if (updateTotalCallback != null) updateTotalCallback.run();
        });

        // Giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                notifyItemChanged(position);
                if (updateTotalCallback != null) updateTotalCallback.run();
            }
        });

        // Xoá sản phẩm
        holder.btnRemove.setOnClickListener(v -> {
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
            if (updateTotalCallback != null) updateTotalCallback.run();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
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
