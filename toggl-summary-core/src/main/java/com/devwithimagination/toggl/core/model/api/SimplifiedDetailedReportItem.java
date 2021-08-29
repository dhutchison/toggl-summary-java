package com.devwithimagination.toggl.core.model.api;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Object containing the cut down fields of a regular DetailedReportItem
 * that we are actually interested in for this program. 
 */
public class SimplifiedDetailedReportItem {
    
    /**
     * time entry description
     */
    private String description;

    /**
     * start time of the time entry in ISO 8601 date and time format (YYYY-MM-DDTHH:MM:SS)
     */
    private String start;

    /**
     * end time of the time entry in ISO 8601 date and time format (YYYY-MM-DDTHH:MM:SS)
     */
    private String end;

    /**
     * time entry duration in milliseconds
     */
    private long dur;

    /**
     * array of tag names, which assigned for the time entry
     */
    private List<String> tags;

    public String getDescription() {
        return description;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public ZonedDateTime getStartZonedDateTime() {
        return ZonedDateTime.parse(start);
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public ZonedDateTime getEndZonedDateTime() {
        return ZonedDateTime.parse(end);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getDur() {
        return dur;
    }

    public void setDur(long dur) {
        this.dur = dur;
    }

}
