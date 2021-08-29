package com.devwithimagination.toggl.core.processor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.devwithimagination.toggl.core.model.GroupingType;
import com.devwithimagination.toggl.core.model.Summary;
import com.devwithimagination.toggl.core.model.TimeSummary;
import com.devwithimagination.toggl.core.model.api.SummaryReportItem;
import com.devwithimagination.toggl.core.model.api.SummaryReportTitle;

public class SummaryReporter {

    private boolean debug;

    /**
     * Create a new SummaryReporter.
     * 
     * @param debug boolean for if debug logging should be used (true) or not
     *              (false)
     */
    public SummaryReporter(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Get the type of grouping the supplied title object is for.
     * 
     * @param title the object to parse
     * @return the enum value
     */
    public GroupingType getGroupingType(final SummaryReportTitle title) {

        if (title.getClient() != null) {
            return GroupingType.CLIENT;
        } else if (title.getProject() != null) {
            return GroupingType.PROJECT;
        } else if (title.getUser() != null) {
            return GroupingType.USER;
        } else {
            return GroupingType.UNKNOWN;
        }

    }

    /**
     * Get the name of the grouping member the supplied title object is for.
     * 
     * @param title the object to parse
     * @return the string value
     */
    public String getGroupingName(final SummaryReportTitle title) {
        if (title.getClient() != null) {
            return title.getClient();
        } else if (title.getProject() != null) {
            return title.getProject();
        } else if (title.getUser() != null) {
            return title.getUser();
        } else {
            return "Unknown Client/Project";
        }

    }

    /**
     * Helper function to calculate the percentage of one number to another
     * 
     * @param partialValue the partial value
     * @param totalValue   the total value
     * @return the percentage value.
     */
    public double calculatePercentage(final int partialValue, final int totalValue) {
        if (totalValue > 0) {
            return (100 * partialValue) / totalValue;
        } else {
            return 0;
        }
    }

    /**
     * Calculate the total duration that should be used based on the input data.
     * 
     * If a TimeSummary is supplied, the totalCount value from this will be used.
     * Otherwise the total duration will be calculated from the summary report data.
     * 
     * @param reportData the input summary report data items
     * @param totalTime  the time summary calculated from detailed entries
     * @return the total time value to use.
     */
    public long calculateTotalTime(final List<SummaryReportItem> reportData, final TimeSummary totalTime) {

        var totalDuration = 0l;
        if (totalTime != null) {
            /* If a time summary has been passed through, use it for the total */
            totalDuration = totalTime.getTimeCount();
        } else {
            /* A summary was not passed through, calculate based on the supplied items */
            for (SummaryReportItem item : reportData) {
                totalDuration += item.getTime();
            }
        }

        return totalDuration;
    }

    /**
     * Calculate summary information for the report data, split into per-client and
     * per-project.
     * 
     * This assumes the summary API has been invoked with the following parameters:
     * - grouping=clients - subgrouping=projects
     * 
     * 
     * @param reportData The summary report items from the api for the reporting
     *                   period
     * @param totalTime  The total time calculated for the time period. This is used
     *                   to provide "unbooked" time in the summary.
     * 
     * @return the calculated summary object
     */
    public List<Summary> calculateSummaryTotals(final List<SummaryReportItem> reportData, final TimeSummary totalTime) {

        /* Get the total duration to use */
        var totalDuration = calculateTotalTime(reportData, totalTime);

        /* Process each data item */
        var retItems = reportData.stream().map(value -> {

            var groupingType = getGroupingType(value.getTitle());
            var groupingName = getGroupingName(value.getTitle());
            var percent = calculatePercentage((int) value.getTime(), (int) totalDuration);

            var summary = new Summary(groupingName, groupingType, value.getTime(), percent);

            if (value.getItems() != null && !value.getItems().isEmpty()) {
                /* If there are subgroup items, process recursively */
                summary.getSubgroupSummary().addAll(calculateSummaryTotals(value.getItems(), null));
            }

            return summary;

        }).collect(Collectors.toList());

        if (totalTime != null) {
            /*
             * If there are time totals supplied from detailed reports, include an item for
             * unbooked
             */
            retItems.add(new Summary("Unbooked Time", GroupingType.UNKNOWN, totalTime.getUnbookedTime(),
                    calculatePercentage((int) totalTime.getUnbookedTime(), (int) totalDuration)));
        }

        /* Sort by time */
        // TODO: may need reversed!
        Collections.sort(retItems, Comparator.comparing(Summary::getBookedTime));

        return retItems;
    }

}
