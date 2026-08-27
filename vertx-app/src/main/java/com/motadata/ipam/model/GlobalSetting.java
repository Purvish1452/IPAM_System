package com.motadata.ipam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer loggingLevel;
    private Integer cssMode;

    public GlobalSetting() {
    }

    public GlobalSetting(Long id, Integer loggingLevel, Integer cssMode) {
        this.id = id;
        this.loggingLevel = loggingLevel;
        this.cssMode = cssMode;
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

    public Integer getCssMode() {
        return cssMode;
    }

    public void setCssMode(Integer cssMode) {
        this.cssMode = cssMode;
    }
}
