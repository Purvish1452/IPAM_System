package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;

@Entity
@Table(name = "feature")
public class TraceOrgFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name; // Feature name

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

