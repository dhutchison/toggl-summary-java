package com.devwithimagination.toggl.core.model.api;

import java.util.List;

/**
 * Object holding fields for a single data item in the summary report call. 
 */
public class SummaryReportItem {

    private SummaryReportTitle title;
    private long time;

    private List<SummaryReportItem> items;

    public List<SummaryReportItem> getItems() {
        return items;
    }
    public void setItems(List<SummaryReportItem> items) {
        this.items = items;
    }
    public SummaryReportTitle getTitle() {
        return title;
    }
    public void setTitle(SummaryReportTitle title) {
        this.title = title;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    
}
