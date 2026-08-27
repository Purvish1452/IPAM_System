package com.motadata.traceorg.ipam.entity.report;

/**
 * Created by root on 1/5/18.
 */
public class TraceOrgReportHeaderFooter
{
    private String title;

    private boolean isLandscape;

    private String logoPath;

    private String poweredBy;

    public String getPoweredBy()
    {
        return poweredBy;
    }

    public void setPoweredBy(String poweredBy)
    {
        this.poweredBy = poweredBy;
    }

    public boolean isLandscape()
    {
        return isLandscape;
    }

    public void setLandscape(boolean landscape)
    {
        isLandscape = landscape;
    }

    public String getLogoPath()
    {
        return logoPath;
    }

    public void setLogoPath(String logoPath)
    {
        this.logoPath = logoPath;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
