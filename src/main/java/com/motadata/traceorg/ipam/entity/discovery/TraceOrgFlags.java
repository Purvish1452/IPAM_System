package com.motadata.traceorg.ipam.entity.discovery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "flags")
public class TraceOrgFlags
{

    @Id
    @Column(name = "flag", nullable = false, unique = true, length = 100)
    private String flag;

    @Column(name = "value", nullable = false)
    private boolean value;

    public TraceOrgFlags()
    {

    }

    public TraceOrgFlags(String flag, boolean value)
    {
        this.flag = flag;
        this.value = value;
    }

    public String getFlag()
    {
        return flag;
    }

    public void setFlag(String flag)
    {
        this.flag = flag;
    }

    public boolean isValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "Flag{" +
                "flag='" + flag + '\'' +
                ", value=" + value +
                '}';
    }
}