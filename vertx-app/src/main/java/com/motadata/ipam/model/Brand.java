package com.motadata.ipam.model;

import java.io.Serializable;

public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String productName;
    private String productImg;

    // Default constructor for serialization.
    public Brand() {
    }

    // Parameterized constructor initializing brand attributes.
    public Brand(Long id, String productName, String productImg) {
        this.id = id;
        this.productName = productName;
        this.productImg = productImg;
    }

    // Returns the brand configuration ID.
    public Long getId() {
        return id;
    }

    // Sets the brand configuration ID.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the customized product name.
    public String getProductName() {
        return productName;
    }

    // Sets the customized product name.
    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Returns the customized product image/logo path.
    public String getProductImg() {
        return productImg;
    }

    // Sets the customized product image/logo path.
    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }
}
