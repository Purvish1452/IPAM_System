package com.motadata.traceorg.ipam.entity.report;

/**
 * Created by root on 1/5/18.
 */
public class TraceOrgReportColumn
{
    private String m_propertyLabel;

    private String m_property;

    public TraceOrgReportColumn(String propertyLabel, String property)
    {
        this.m_propertyLabel = propertyLabel;
        this.m_property = property;
    }

    public String getPropertyLabel()
    {
        return m_propertyLabel;
    }

    public void setPropertyLabel(String propertyLabel)
    {
        this.m_propertyLabel = propertyLabel;
    }

    public String getProperty()
    {
        return m_property;
    }

    public void setProperty(String property)
    {
        this.m_property = property;
    }
}
