package com.motadata.traceorg.ipam.entity.settings;


import com.motadata.traceorg.ipam.entity.TraceOrgAuditable;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "custom_column")
public class TraceOrgCustomColumn extends TraceOrgAuditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Column name is required")
    @Size(max = 50, message = "Column name must be at most 50 characters")
    @Column(name = "column_name", nullable = false, length = 50)
    private String columnName;

    @NotBlank(message = "Column location is required")
    @Column(name = "column_at", nullable = false)
    private String columnAt;

    @Size(max = 255, message = "Description must be at most 255 characters")
    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnAt() {
        return columnAt;
    }

    public void setColumnAt(String columnAt) {
        this.columnAt = columnAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

