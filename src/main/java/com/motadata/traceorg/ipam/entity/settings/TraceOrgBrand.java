package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "brand")
public class TraceOrgBrand implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String productName;

    @Column(length = 50)
    private String productImg;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {

        if(productName!=null && !productName.isEmpty()){
            return productName.trim();
        }
        return productName;
    }

    public void setProductName(String productName) {

        if(productName!=null && !productName.isEmpty())
        {
            this.productName = productName.trim();
        }
        else
        {
            this.productName = productName;
        }
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {

        if(productImg!=null && !productImg.isEmpty())
        {
            this.productImg = productImg.trim();
        }
        else
        {
            this.productImg = productImg;
        }
    }
}
