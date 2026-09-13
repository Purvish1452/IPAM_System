package com.motadata.ipam.model;

import java.io.Serializable;

public class GlobalSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer loggingLevel;
    private Integer cssMode;

    // Default constructor for GlobalSetting
    public GlobalSetting() {
    }

    // Parameterized constructor initializing id, loggingLevel, and cssMode
    public GlobalSetting(Long id, Integer loggingLevel, Integer cssMode) {
        this.id = id;
        this.loggingLevel = loggingLevel;
        this.cssMode = cssMode;
    }

    // Gets the global setting ID
    public Long getId() {
        return id;
    }

    // Sets the global setting ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the logging level setting value
    public Integer getLoggingLevel() {
        return loggingLevel;
    }

    // Sets the logging level setting value
    public void setLoggingLevel(Integer loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    // Gets the CSS mode setting value
    public Integer getCssMode() {
        return cssMode;
    }

    // Sets the CSS mode setting value
    public void setCssMode(Integer cssMode) {
        this.cssMode = cssMode;
    }
}
