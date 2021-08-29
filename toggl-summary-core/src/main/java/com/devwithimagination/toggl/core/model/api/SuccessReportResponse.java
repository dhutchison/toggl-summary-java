package com.devwithimagination.toggl.core.model.api;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * Base object structure for a successful report response
 */
public class SuccessReportResponse<T> {

    /**
     * Total time in milliseconds for the selected report
     */
    @JsonbProperty("total_grand")
    private long grandTotal;

    /**
     * An array with detailed information of the requested report. 
     * The structure of the data in the array depends on the report.
     */
    private List<T> data;
    

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(long grandTotal) {
        this.grandTotal = grandTotal;
    }
}
