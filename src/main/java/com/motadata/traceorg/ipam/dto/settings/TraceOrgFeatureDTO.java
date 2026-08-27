package com.motadata.traceorg.ipam.dto.settings;

public class TraceOrgFeatureDTO {
    private Long id;
    private String featureName;

    public TraceOrgFeatureDTO(Long id, String featureName) {
        this.id = id;
        this.featureName = featureName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
}
