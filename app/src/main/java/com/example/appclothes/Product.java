package com.example.appclothes;

public class Product {
    private String name;
    private String price;
    private int image;
    private String description;
    private int quantity;

    public Product(String name, String price, int image, String description) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.quantity = 1;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImage() { return image; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
