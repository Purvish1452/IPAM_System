package com.motadata.ipam.model;

import java.io.Serializable;

public class Feature implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;

    // Default constructor for Feature
    public Feature() {
    }

    // Parameterized constructor initializing id and name
    public Feature(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Gets the feature ID
    public Long getId() {
        return id;
    }

    // Sets the feature ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the feature name
    public String getName() {
        return name;
    }

    // Sets the feature name
    public void setName(String name) {
        this.name = name;
    }
}
