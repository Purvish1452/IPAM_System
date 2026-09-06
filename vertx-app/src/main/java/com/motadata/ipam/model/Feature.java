package com.motadata.ipam.model;

import java.io.Serializable;

public class Feature implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;

    public Feature() {
    }

    public Feature(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
