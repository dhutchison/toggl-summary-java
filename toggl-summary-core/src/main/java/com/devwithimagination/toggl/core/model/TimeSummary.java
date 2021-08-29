package com.devwithimagination.toggl.core.model;

/**
 * Object holding the calculated times
 */
public class TimeSummary {

    /**
     * Total actual working hours (combination of booked and unbooked time)
     */
    private long timeCount;

    /**
     * breaks (time between a marker item and the next non-marker)
     */
    private long breakTime;

    /**
     * time booked against actual tasks (should match the API total time)
     */
    private long bookedTime;

    /**
     * unbooked (any time between items that isn't covered as breaks)
     */
    private long unbookedTime;

    public long getTimeCount() {
        return timeCount;
    }
    public void setTimeCount(long timeCount) {
        this.timeCount = timeCount;
    }
    public long getBreakTime() {
        return breakTime;
    }
    public void setBreakTime(long breakTime) {
        this.breakTime = breakTime;
    }
    public long getBookedTime() {
        return bookedTime;
    }
    public void setBookedTime(long bookedTime) {
        this.bookedTime = bookedTime;
    }
    public long getUnbookedTime() {
        return unbookedTime;
    }
    public void setUnbookedTime(long unbookedTime) {
        this.unbookedTime = unbookedTime;
    }
    
}
