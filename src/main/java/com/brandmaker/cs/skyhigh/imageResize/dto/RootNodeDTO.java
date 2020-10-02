package com.brandmaker.cs.skyhigh.imageResize.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RootNodeDTO implements Serializable
{
    private static final long serialVersionUID = 2000576303247723563L;
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("startDate")
    private Date startDate;
    
    @JsonProperty("endDate")
    private Date endDate;
    
    @JsonProperty("leaf")
    private Boolean leaf;
    
    @JsonIgnore
    @JsonProperty("nodeType")
    private String nodeType;
    
    
    public RootNodeDTO()
    {
    }
    
    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }


    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public Boolean getLeaf()
    {
        return leaf;
    }

    public void setLeaf(Boolean leaf)
    {
        this.leaf = leaf;
    }
    
    
}
