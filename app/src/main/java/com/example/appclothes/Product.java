package com.example.appclothes;

public class Product {
    private String id;
    private String name;
    private String price;
    private String imageUrl;
    private String description;
    private int quantity;
    private String category;
    private boolean inStock;

    // Constructor rỗng cần thiết cho Firebase
    public Product() {
    }

    // Constructor đầy đủ cho Firebase
    public Product(String id, String name, String price, String imageUrl, String description, String category,
            boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
        this.inStock = inStock;
        this.quantity = 1;
    }

    // Constructor tương thích với code cũ (tạm thời)
    public Product(String name, String price, int image, String description) {
        this.name = name;
        this.price = price;
        this.imageUrl = ""; // Tạm thời để trống
        this.description = description;
        this.quantity = 1;
        this.category = "khác";
        this.inStock = true;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public boolean isInStock() {
        return inStock;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    // Method để backward compatibility với code cũ
    public int getImage() {
        // Trả về placeholder drawable khi không có imageUrl
        return R.drawable.aothun1;
    }
}
