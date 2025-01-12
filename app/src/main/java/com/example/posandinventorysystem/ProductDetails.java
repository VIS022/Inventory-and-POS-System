package com.example.posandinventorysystem;

public class ProductDetails {
    private String prodName;
    private String prodCate;
    private double prodPrice;
    private String prodQuantity;
    private double prodCapital;
    private String prodBarcode;
    private String imageUri;

    public ProductDetails(String prodName, String prodCate, double prodPrice, String prodQuantity, double prodCapital, String prodBarcode, String imageUri) {
        this.prodName = prodName;
        this.prodCate = prodCate;
        this.prodPrice = prodPrice;
        this.prodQuantity = prodQuantity;
        this.prodCapital = prodCapital;
        this.prodBarcode = prodBarcode;
        this.imageUri = imageUri;
    }

    // Getter methods for retrieving product details
    public String getProdName() {
        return prodName;
    }

    public String getProdCate() {
        return prodCate;
    }

    public double getProdPrice() {
        return prodPrice;
    }

    public String getProdQuantity() {
        return prodQuantity;
    }

    public double getProdCapital() {
        return prodCapital;
    }

    public String getProdBarcode() {
        return prodBarcode;
    }

    public String getImageUri() {
        return imageUri;
    }
}
