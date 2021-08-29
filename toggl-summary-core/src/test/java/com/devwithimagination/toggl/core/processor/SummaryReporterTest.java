package com.devwithimagination.toggl.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import com.devwithimagination.toggl.core.model.GroupingType;
import com.devwithimagination.toggl.core.model.api.SummaryReportResponse;
import com.devwithimagination.toggl.core.model.api.SummaryReportTitle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.json.bind.JsonbBuilder;

/**
 * Test for the {@link SummaryReporter}.
 */
class SummaryReporterTest {

    private SummaryReporter testInstance;

    /**
     * Perform setup ahead of each test. 
     */
    @BeforeEach
    void setup() {
        this.testInstance = new SummaryReporter(false);
    }



// function createEmptyEntry(): SummaryReportItem {
//     return {
//         title: {
//             client: ''
//         },
//         time: 0,
//         items: []
//     };
// }

    @ParameterizedTest()
    @MethodSource("getSummaryReportTitleParsingTests")
    void testTitleTypeParsing(final SummaryReportTitle title, final GroupingType expectedGroupingType, final String expectedName) {

        final var actualType = testInstance.getGroupingType(title);
        assertEquals(expectedGroupingType, actualType);

        final var actualName = testInstance.getGroupingName(title);
        assertEquals(expectedName, actualName);

    }

    private static Stream<Arguments> getSummaryReportTitleParsingTests() {
        return Stream.of(
            Arguments.of(
                SummaryReportTitle.forClient("test-client"),
                GroupingType.CLIENT,
                "test-client"
            ),
            Arguments.of(
                SummaryReportTitle.forProject("test-project"),
                GroupingType.PROJECT,
                "test-project"
            ),
            Arguments.of(
                SummaryReportTitle.forUser("test-user"),
                GroupingType.USER,
                "test-user"
            )
        );
        
    }

    @ParameterizedTest()
    @MethodSource("getPercentageCalculationTests")
    void testPercentageCalculation(final int partialValue, final int totalValue, final int expectedValue) {

        var actualValue = testInstance.calculatePercentage(partialValue, totalValue);
        assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> getPercentageCalculationTests() {
        return Stream.of(
            Arguments.of(
                10,
                100,
                10
            ),
            Arguments.of(
                10,
                0,
                0
            )
        );

    }

    void calculateSummaryWithoutTimeDetail() {

        /* This test uses a stripped down file based on a real API call */
        final var testData = loadTestSummaryFile();
        
        final var actualResult = testInstance.calculateSummaryTotals(testData.getData(), null);

        assertNotNull(actualResult);
        assertEquals(5, actualResult.size());


        var listItem = actualResult.get(0);
        assertEquals("Client-2", listItem.getName());
        assertEquals(29893000, listItem.getBookedTime());
        assertEquals("32.76", String.format("%.2f", listItem.getPercentageOfTotalTime()));
        assertNotNull(listItem.getSubgroupSummary());

        listItem = actualResult.get(1);
        assertEquals("Legacy", listItem.getName());
        assertEquals(22097000, listItem.getBookedTime());
        assertEquals("24.22", String.format("%.2f", listItem.getPercentageOfTotalTime()));
        assertNotNull(listItem.getSubgroupSummary());
        /* We will only test values for the a single item here.
         * Don't need to go through every item, the inner array uses the exact same process / object
         * structure as the outers. 
         */
        assertEquals(3, listItem.getSubgroupSummary().size());
        listItem = listItem.getSubgroupSummary().get(0);
        assertEquals("LP-Support", listItem.getName());
        assertEquals(13455000, listItem.getBookedTime());
        assertEquals("60.89", String.format("%.2f", listItem.getPercentageOfTotalTime()));
        assertNull(listItem.getSubgroupSummary());

        listItem = actualResult.get(2);
        assertEquals("Client-1", listItem.getName());
        assertEquals(17407000, listItem.getBookedTime());
        assertEquals("19.08", String.format("%.2f", listItem.getPercentageOfTotalTime()));
        assertNotNull(listItem.getSubgroupSummary());


        /* This is the catch-all "unknown project" item */
        listItem = actualResult.get(3);
        assertEquals("Unknown Client/Project", listItem.getName());
        assertEquals(12492000, listItem.getBookedTime());
        assertEquals("13.69", String.format("%.2f", listItem.getPercentageOfTotalTime()));

        
        listItem = actualResult.get(4);
        assertEquals("Client-3", listItem.getName());
        assertEquals(9352000, listItem.getBookedTime());
        assertEquals("10.25", String.format("%.2f", listItem.getPercentageOfTotalTime()));
        assertNotNull(listItem.getSubgroupSummary());

        
    }


    private SummaryReportResponse loadTestSummaryFile() {

            try (InputStream in = getClass().getResourceAsStream("/summary.json")) {
                return JsonbBuilder.create().fromJson(in, SummaryReportResponse.class);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }

    }
}
