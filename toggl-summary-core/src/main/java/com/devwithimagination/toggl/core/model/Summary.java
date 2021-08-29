package com.devwithimagination.toggl.core.model;

import java.util.ArrayList;
import java.util.List;

public class Summary {

    /**
     * Name of the grouping member
     */
    private String name;

    /**
     * The type of the grouping. 
     */
    private GroupingType groupingType;

    /**
     * time booked against actual tasks (should match the API total time)
     */
     private long bookedTime;

     /**
     * The percentage of the total time this client had booked against it. 
     */
    private double percentageOfTotalTime;

    private List<Summary> subgroupSummary;

    public Summary(final String name, final GroupingType groupingType, final long bookedTime, final double percentageOfTotalTime) {
        this.name = name;
        this.groupingType = groupingType;
        this.bookedTime = bookedTime; 
        this.percentageOfTotalTime = percentageOfTotalTime;
        this.subgroupSummary = new ArrayList<>();
    }

    public long getBookedTime() {
        return bookedTime;
    }
    public GroupingType getGroupingType() {
        return groupingType;
    }
    public String getName() {
        return name;
    }
    public double getPercentageOfTotalTime() {
        return percentageOfTotalTime;
    }
    public List<Summary> getSubgroupSummary() {
        return subgroupSummary;
    }
    
}
