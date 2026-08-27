package com.motadata.traceorg.ipam.entity.rogueDetection;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rogue_detection_details")
public class TraceOrgRogueDetection
{
    @Id
    private long id;

    private String macAddress;

    private String ipAddress;

    private Date discoveredAt;

    private String nicType;

    private String authenticity;


    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDiscoveredAt()
    {
        if(discoveredAt != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(discoveredAt);
        }

        return null;
    }

    public void setDiscoveredAt(Date discoveredAt) {
        this.discoveredAt = discoveredAt;
    }

    public String getNicType() {
        return nicType;
    }

    public void setNicType(String nicType) {
        this.nicType = nicType;
    }

    public String getAuthenticity() {
        return authenticity;
    }

    public void setAuthenticity(String authenticity) {
        this.authenticity = authenticity;
    }



    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
