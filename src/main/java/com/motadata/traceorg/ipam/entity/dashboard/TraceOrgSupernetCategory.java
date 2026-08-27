package com.motadata.traceorg.ipam.entity.dashboard;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "supernet_category")
public class TraceOrgSupernetCategory implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String categoryName;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        if(categoryName!=null && !categoryName.isEmpty()){
            return categoryName.trim();
        }
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        if(categoryName!=null && !categoryName.isEmpty())
        {
            this.categoryName = categoryName.trim();
        }
        else
        {
            this.categoryName = categoryName;
        }
    }
}
