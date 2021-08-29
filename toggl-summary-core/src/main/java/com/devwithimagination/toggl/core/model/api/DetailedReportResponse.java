package com.devwithimagination.toggl.core.model.api;

import jakarta.json.bind.annotation.JsonbProperty;

public class DetailedReportResponse extends SuccessReportResponse<DetailedReportItem> {
    /**
     * total number of time entries that were found for the request. 
     * Pay attention to the fact that the amount of time entries 
     * returned is max the number which is returned with the per_page 
     * field (currently 50). To get the next batch of time entries you need to 
     * do a new request with same parameters and the incremented page parameter. 
     * 
     * It is not possible to get all the time entries with one request.
     */
    @JsonbProperty("total_count")
    private int totalCount;
    /**
     * how many time entries are displayed in one request
     */
    @JsonbProperty("per_page")
    private int perPage;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
}
