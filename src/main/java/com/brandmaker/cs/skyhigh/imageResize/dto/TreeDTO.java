package com.brandmaker.cs.skyhigh.imageResize.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TreeDTO implements Serializable
{
    private static final long serialVersionUID = 162512958319443879L;
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private Date endDate;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currencyId")
    private Integer currencyId;

    @JsonProperty("planningType")
    private PlanningType planningType;

    @JsonProperty("type")
    private TreeType type;

    public enum PlanningType
    {
        TOP_DOWN,
        BOTTOM_UP;
    }

    public enum TreeType
    {
        FLEXIBLE,
        STRICT;
    }

    
    public TreeDTO()
    {
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

    /**
     *  'TOP_DOWN' or 'BOTTOM_UP'
     */
    public PlanningType getPlanningType()
    {
        return planningType;
    }
    public void setPlanningType(PlanningType planningType)
    {
        this.planningType = planningType;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
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

    /**
     * can return 'FLEXIBLE' or 'STRICT' types
     * This field is deprecated and from version 3.2 always return 'FLEXIBLE'
     * @deprecated
     */
    @Deprecated
    public TreeType getType()
    {
        return type;
    }

    public void setType(TreeType type)
    {
        this.type = type;
    }

    public Integer getCurrencyId()
    {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId)
    {
        this.currencyId = currencyId;
    }
}
