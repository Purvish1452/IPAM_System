package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;

/**
 * Created by hardik on 7/7/18.
 */
@Entity
@Table(name = "global_setting")
public class TraceOrgGlobalSetting
{
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private Integer loggingLevel;

    private Integer cssMode;

    public Integer getCssMode() {
        return cssMode;
    }

    public void setCssMode(Integer cssMode) {
        this.cssMode = cssMode;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(Integer loggingLevel) {
        this.loggingLevel = loggingLevel;
    }
}
