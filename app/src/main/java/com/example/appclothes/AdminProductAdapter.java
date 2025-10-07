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

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

  private Context context;
  private List<Product> productList;
  private OnAdminProductListener listener;

  public interface OnAdminProductListener {
    void onEditProduct(Product product);

    void onDeleteProduct(Product product);
  }

  public AdminProductAdapter(Context context, List<Product> productList, OnAdminProductListener listener) {
    this.context = context;
    this.productList = productList;
    this.listener = listener;
  }

  @NonNull
  @Override
  public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
    return new AdminProductViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
    Product product = productList.get(position);

    // Load image
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
      holder.imgProduct.setImageResource(R.drawable.aothun1);
    }

    holder.txtName.setText(product.getName());
    holder.txtPrice.setText(product.getPrice());
    holder.txtCategory
        .setText("Danh mục: " + (product.getCategory() != null ? product.getCategory() : "Chưa phân loại"));
    holder.txtStock.setText(product.isInStock() ? "Còn hàng" : "Hết hàng");
    holder.txtStock.setTextColor(product.isInStock() ? context.getResources().getColor(android.R.color.holo_green_dark)
        : context.getResources().getColor(android.R.color.holo_red_dark));

    // Set click listeners
    holder.btnEdit.setOnClickListener(v -> {
      if (listener != null) {
        listener.onEditProduct(product);
      }
    });

    holder.btnDelete.setOnClickListener(v -> {
      if (listener != null) {
        listener.onDeleteProduct(product);
      }
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

  public static class AdminProductViewHolder extends RecyclerView.ViewHolder {
    ImageView imgProduct;
    TextView txtName, txtPrice, txtCategory, txtStock;
    Button btnEdit, btnDelete;

    public AdminProductViewHolder(@NonNull View itemView) {
      super(itemView);
      imgProduct = itemView.findViewById(R.id.imgAdminProduct);
      txtName = itemView.findViewById(R.id.txtAdminProductName);
      txtPrice = itemView.findViewById(R.id.txtAdminProductPrice);
      txtCategory = itemView.findViewById(R.id.txtAdminProductCategory);
      txtStock = itemView.findViewById(R.id.txtAdminProductStock);
      btnEdit = itemView.findViewById(R.id.btnEditProduct);
      btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
    }
  }
}