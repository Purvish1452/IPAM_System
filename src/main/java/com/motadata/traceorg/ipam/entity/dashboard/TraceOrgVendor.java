package com.motadata.traceorg.ipam.entity.dashboard;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "vendor")
public class TraceOrgVendor implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vendorName;

    private String vendorMac;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorName() {

        if(vendorName!=null){
            return vendorName.trim();
        }
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorMac() {
        if(vendorMac!=null){
            return vendorMac.trim();
        }

        return vendorMac;
    }

    public void setVendorMac(String vendorMac) {
        this.vendorMac = vendorMac;
    }
}
