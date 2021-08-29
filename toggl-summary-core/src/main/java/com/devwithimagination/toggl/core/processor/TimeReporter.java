package com.devwithimagination.toggl.core.processor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.devwithimagination.toggl.core.model.TimeSummary;
import com.devwithimagination.toggl.core.model.api.SimplifiedDetailedReportItem;

public class TimeReporter {

    private final boolean debug;

    /**
     * Create a new TimeReporter instance.
     * 
     * @param debug boolean for if debug logging should be used (true) or not
     *              (false)
     */
    public TimeReporter(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Function to determine if an entry contains the tag for being a marker.
     * 
     * @param entry the entry to check
     * @return true if it does, false if it doesn't
     */
    public boolean doesEntryHaveBreakStartMarker(final SimplifiedDetailedReportItem entry) {
        return entry.getTags() != null && entry.getTags().contains("marker");
    }

    /**
     * Function to determine if two entries are for the same day.
     * 
     * @param a the first entry to compare the end date for
     * @param b the second entry to compare the start date for
     */
    private boolean areEntriesForTheSameDay(final SimplifiedDetailedReportItem a,
            final SimplifiedDetailedReportItem b) {

        /* Check the entries are for the same day */
        var currentDate = LocalDate.parse(a.getEnd().substring(0, 10));
        var prevDate = LocalDate.parse(b.getStart().substring(0, 10));

        return currentDate.equals(prevDate);
    }

    /**
     * Helper function to work out if the previous entry was the start of a break.
     * 
     * @param currentIndex the index in the array for the current entry
     * @param array        the array containing the entries
     */
    public boolean wasPreviousEntryBreakStart(final int currentIndex, final List<SimplifiedDetailedReportItem> array) {

        /*
         * A previous entry counts as a break start if: - it has a marker tag
         * (doesEntryHaveBreakStartMarker) - the entry before it does is not a break
         * start - it is for the same day as the current entry
         */

        boolean isMarker;
        if (currentIndex > 0) {

            var prevEntry = array.get(currentIndex - 1);
            isMarker = doesEntryHaveBreakStartMarker(prevEntry);

            if (isMarker) {
                /* Check the previous entry to check it wasn't one too */
                isMarker = !wasPreviousEntryBreakStart(currentIndex - 1, array);
            }

            if (isMarker) {
                /* Check the entries are for the same day */
                isMarker = areEntriesForTheSameDay(array.get(currentIndex - 1), array.get(currentIndex));
            }
        } else {
            /* No previous entries */
            isMarker = false;
        }

        return isMarker;
    }

    /**
     * Helper function to calculate the duration between the start of the current
     * entry and the end of the previous one.
     * 
     * @param currentIndex the index in the array for the current entry
     * @param array        the array containing the entries
     * 
     */
    public Duration getTimeBetweenEntries(final int currentIndex, final List<SimplifiedDetailedReportItem> array) {

        /* Work out the time between this entry and the previous one */
        final Duration timeBetweenEntries;
        if (currentIndex > 0) {
            /* There is a previous entry */
            var prevEntry = array.get(currentIndex - 1);
            var currentEntry = array.get(currentIndex);

            timeBetweenEntries = Duration.between(ZonedDateTime.parse(prevEntry.getEnd()),
                    ZonedDateTime.parse(currentEntry.getStart()));
        } else {
            /* No previous entry, default to zero */
            timeBetweenEntries = Duration.ZERO;
        }

        if (debug) {
            System.out.println("Time between entries: " + timeBetweenEntries.toString());
        }

        return timeBetweenEntries;
    }

    /**
     * Format the supplied number of milliseconds as an HH:mm:ss string.
     * 
     * @param millseconds the number of milliseconds to format
     */
    public String formatMillis(long millseconds) {

        var seconds = (millseconds / 1000) % 60;
        var minutes = (millseconds / (1000 * 60)) % 60;
        /*
         * Intentionally not "% 24" this as we want to be able to have more than 24
         * hours, not using a seperate day counter
         */
        var hours = (millseconds / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Calculate summary time information for the report data.
     * 
     * Includes: - Total actual working hours (combination of booked and unbooked
     * time) - breaks (time between a marker item and the next non-marker) - time
     * booked against actual tasks (should match the API total time) - unbooked time
     * (any time between items that isn't covered as breaks)
     * 
     * @param reportData The detailed time entry items for the reporting period
     */
    public TimeSummary calculateTimeTotals(final List<SimplifiedDetailedReportItem> reportData) {

        /* Setup the initial return object with initial values */
        final var timeSummary = new TimeSummary();

        /* Sort the input data by the item start date & time */
        final var sortedReportData = new ArrayList<>(reportData);
        sortedReportData.sort(Comparator.comparing(SimplifiedDetailedReportItem::getStartZonedDateTime));

        for (int index = 0; index < sortedReportData.size(); index++) {
            var entry = sortedReportData.get(index);

            if (debug) {
                System.out.println("======================");
                System.out.println(String.format("Counts so far: total %s, breaks %s, booked %s, unbooked: %s",
                        formatMillis(timeSummary.getTimeCount()), formatMillis(timeSummary.getBreakTime()),
                        formatMillis(timeSummary.getBookedTime()), formatMillis(timeSummary.getUnbookedTime())));
                System.out.println(String.format("Time entry for %s: %s (%s - %s)", entry.getDescription(),
                        formatMillis(entry.getDur()), entry.getStart(), entry.getEnd()));
            }

            /*
             * An entry is a "break start" marker if all the following are true: - it has
             * the tag "marker" - the previous entry did not also have the tag "marker"
             */
            var entryHasMarker = !wasPreviousEntryBreakStart(index, sortedReportData)
                    && doesEntryHaveBreakStartMarker(entry);

            /*
             * Add the booked time for the entry to the running total, if it isn't a 'break'
             * entry
             */
            if (!entryHasMarker) {
                timeSummary.setBookedTime(timeSummary.getBookedTime() + entry.getDur());
            }

            /*
             * Work out the time between this entry and the previous one and add it to the
             * correct total based on our state
             */
            var timeBetweenEntries = getTimeBetweenEntries(index, sortedReportData);

            if (wasPreviousEntryBreakStart(index, sortedReportData)) {
                /*
                 * The previous entry was a 'break start' marker. Gap time is break time
                 */
                timeSummary.setBreakTime(timeSummary.getBreakTime() + timeBetweenEntries.toMillis());
                if (debug) {
                    System.out.println("Break time! " + formatMillis(timeBetweenEntries.toMillis()));
                }
            } else if (index == 0
                    || areEntriesForTheSameDay(sortedReportData.get(index - 1), sortedReportData.get(index))) {
                /*
                 * Gap time is unbooked time if the end of the last item is the same day as the
                 * current item
                 */
                timeSummary.setUnbookedTime(timeSummary.getUnbookedTime() + timeBetweenEntries.toMillis());

                /* Only log it to the console if it is more than 5 minutes */
                if (debug && timeBetweenEntries.toMinutes() > 5) {
                    System.out
                            .println("Unbooked time since last entry: " + formatMillis(timeBetweenEntries.toMillis()));
                }
            }
        }

        /* Total time is the combination of booked and unbooked time */
        timeSummary.setTimeCount(timeSummary.getBookedTime() + timeSummary.getUnbookedTime());

        /* All done, return */
        return timeSummary;
    }

}
