package com.devwithimagination.toggl.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.devwithimagination.toggl.core.model.api.SimplifiedDetailedReportItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for the {@link TimeReporter}.
 */
class TimeReporterTest {

    /**
     * The instance under test.
     */
    private TimeReporter testInstance;

    @BeforeEach
    void setup() {
        this.testInstance = new TimeReporter(true);
    }

    /**
     * Test that a marker tag is detected.
     */
    @Test
    void testCanDetectAMarkerEntryWithTags() {

        final var testEntry = new SimplifiedDetailedReportItem();
        testEntry.setTags(List.of("tag1", "marker", "tag3"));

        var isMarker = testInstance.doesEntryHaveBreakStartMarker(testEntry);

        assertTrue(isMarker);
    }

    /**
     * Test that an entry without a marker tag is not detected as a marker entry.
     */
    @Test
    void testCanDetectAMarkerEntryWithoutTags() {

        final var testEntry = new SimplifiedDetailedReportItem();
        testEntry.setTags(List.of("tag1", "tag2", "tag3"));

        var isMarker = testInstance.doesEntryHaveBreakStartMarker(testEntry);

        assertFalse(isMarker);

    }

    @Test
    void testPreviousEntryIsDetectedAsAMarkerEntry() {

        final var entry1 = new SimplifiedDetailedReportItem();
        entry1.setStart("2020-09-04T10:10:10+01:00");
        entry1.setEnd("2020-09-04T11:10:11+01:00");
        entry1.setTags(List.of("marker"));

        final var entry2 = new SimplifiedDetailedReportItem();
        entry2.setStart("2020-09-04T11:10:10+01:00");
        entry2.setEnd("2020-09-04T12:10:11+01:00");

        final var entries = List.of(entry1, entry2);

        final var previousIsMarker = testInstance.wasPreviousEntryBreakStart(1, entries);
        assertTrue(previousIsMarker);

    }

    @Test
    void testPreviousEntryIsNotDetectedAsAMarkerEntryNoTags() {

        final var entries = List.of(new SimplifiedDetailedReportItem(), new SimplifiedDetailedReportItem());

        final var previousIsMarker = testInstance.wasPreviousEntryBreakStart(1, entries);
        assertFalse(previousIsMarker);

    }

    @Test
    void testPreviousEntryIsNotDetectedAsAMarkerEntryNoPrevious() {

        final var entries = List.of(new SimplifiedDetailedReportItem());

        final var previousIsMarker = testInstance.wasPreviousEntryBreakStart(0, entries);
        assertFalse(previousIsMarker);

    }

    @Test
    void testPreviousEntryIsNotDetectedAsAMarkerEntryPrevDay() {

        final var entry1 = new SimplifiedDetailedReportItem();
        entry1.setStart("2020-09-03T10:10:10+01:00");
        entry1.setEnd("2020-09-03T11:10:11+01:00");
        entry1.setTags(List.of("marker"));

        final var entry2 = new SimplifiedDetailedReportItem();
        entry2.setStart("2020-09-04T11:10:10+01:00");
        entry2.setEnd("2020-09-04T12:10:11+01:00");

        final var entries = List.of(entry1, entry2);

        final var previousIsMarker = testInstance.wasPreviousEntryBreakStart(1, entries);

        assertFalse(previousIsMarker);

    }

    @Test
    void testCalculateDurationBetweenEntries() {

        final var entry1 = new SimplifiedDetailedReportItem();
        entry1.setStart("2020-09-03T10:10:00+01:00");
        entry1.setEnd("2020-09-03T10:10:00+01:00");

        final var entry2 = new SimplifiedDetailedReportItem();
        entry2.setStart("2020-09-03T10:20:00+01:00");
        entry2.setEnd("2020-09-03T10:20:00+01:00");

        final var entries = List.of(entry1, entry2);

        final var duration = testInstance.getTimeBetweenEntries(1, entries);

        assertEquals(10L, duration.toMinutes());

    }

    /**
     * should calculate the duration between entries to be zero when there is only
     * one
     */
    @Test
    void testCalulateForSingleEntry() {

        final var entry = new SimplifiedDetailedReportItem();
        entry.setStart("2020-09-03T10:10:00+01:00");
        entry.setEnd("2020-09-03T10:10:00+01:00");

        final var duration = testInstance.getTimeBetweenEntries(0, Collections.singletonList(entry));

        assertEquals(0L, duration.toMinutes());
    }

    /**
     * should format milliseconds into HH:mm:ss format
     */
    @Test
    void testMillisecondFormatting() {

        final var shortDuration = Duration.ofMinutes(9).plusSeconds(2);
        final var formattedShort = testInstance.formatMillis(shortDuration.toMillis());
        assertEquals("00:09:02", formattedShort);

        final var longDuration = Duration.ofHours(10).plusMinutes(30).plusSeconds(59);
        final var formattedLong = testInstance.formatMillis(longDuration.toMillis());
        assertEquals("10:30:59", formattedLong);

    }

    /**
     * should calculate totals in the same day
     */
    @Test
    void testCalculateTotalsInSameDay() {

        final var entry1 = new SimplifiedDetailedReportItem();
        entry1.setStart("2020-09-03T10:10:00+01:00");
        entry1.setEnd("2020-09-03T10:20:00+01:00");
        entry1.setDur(Duration.ofMinutes(10).toMillis());

        final var entry2 = new SimplifiedDetailedReportItem();
        entry2.setStart("2020-09-03T10:30:00+01:00");
        entry2.setEnd("2020-09-03T10:50:00+01:00");
        entry2.setDur(Duration.ofMinutes(20).toMillis());

        final var entry3 = new SimplifiedDetailedReportItem();
        entry3.setStart("2020-09-03T10:50:00+01:00");
        entry3.setEnd("2020-09-03T10:50:00+01:00");
        entry3.setDur(Duration.ZERO.toMillis());
        entry3.setTags(List.of("marker"));

        final var entry4 = new SimplifiedDetailedReportItem();
        entry4.setStart("2020-09-03T12:00:00+01:00");
        entry4.setEnd("2020-09-03T12:50:00+01:00");
        entry4.setDur(Duration.ofMinutes(50).toMillis());

        final var entries = List.of(entry1, entry2, entry3, entry4);

        final var totals = testInstance.calculateTimeTotals(entries);

        assertEquals(Duration.ofMinutes(80).toMillis(), totals.getBookedTime());
        assertEquals(Duration.ofMinutes(70).toMillis(), totals.getBreakTime());
        assertEquals(Duration.ofMinutes(10).toMillis(), totals.getUnbookedTime());
        assertEquals(Duration.ofMinutes(90).toMillis(), totals.getTimeCount());

    }

    /**
     * should calculate totals accross days
     */
    @Test
    void testCalculateTotalsAcrossDays() {

        final var entry1 = new SimplifiedDetailedReportItem();
        entry1.setStart("2020-09-03T10:10:00+01:00");
        entry1.setEnd("2020-09-03T10:20:00+01:00");
        entry1.setDur(Duration.ofMinutes(10).toMillis());

        final var entry2 = new SimplifiedDetailedReportItem();
        entry2.setStart("2020-09-03T10:50:00+01:00");
        entry2.setEnd("2020-09-03T10:50:00+01:00");
        entry2.setDur(Duration.ZERO.toMillis());
        entry2.setTags(List.of("marker"));

        final var entry3 = new SimplifiedDetailedReportItem();
        entry3.setStart("2020-09-04T12:00:00+01:00");
        entry3.setEnd("2020-09-04T12:50:00+01:00");
        entry3.setDur(Duration.ofMinutes(50).toMillis());

        final var entries = List.of(entry1, entry2, entry3);
        final var totals = testInstance.calculateTimeTotals(entries);

        assertEquals(Duration.ofMinutes(60).toMillis(), totals.getBookedTime());
        assertEquals(Duration.ZERO.toMillis(), totals.getBreakTime());
        assertEquals(Duration.ofMinutes(30).toMillis(), totals.getUnbookedTime());
        assertEquals(Duration.ofMinutes(90).toMillis(), totals.getTimeCount());

    }

}
