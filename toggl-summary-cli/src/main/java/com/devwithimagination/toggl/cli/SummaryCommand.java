package com.devwithimagination.toggl.cli;

import java.time.LocalDate;
import java.util.concurrent.Callable;

import com.devwithimagination.toggl.core.api.ApiClient;
import com.devwithimagination.toggl.core.model.Configuration;
import com.devwithimagination.toggl.core.processor.SummaryReporter;
import com.devwithimagination.toggl.core.processor.TimeReporter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "toggl-summary", mixinStandardHelpOptions = true, version = "summary 0.0.1", description = "Produces a summary of toggl data to STDOUT.", defaultValueProvider = CustomDefaultValueProvider.class)
public class SummaryCommand implements Callable<Integer> {

    @Option(names = { "-D", "--debug" }, description = "output extra debugging")
    boolean debug;

    @Option(names = "--api-key", arity = "0..1", interactive = true, required = true)
    String apiKey;

    @Option(names = "--workspace-id", description = "ID of the Toggle workspace", required = true)
    String workspaceId;

    @Option(names = "--email", description = "Your email address", required = true)
    String emailAddress;

    @Option(names = { "-d", "--day", "--start" }, description = "Day to report on (in yyyy-MM-dd format). "
            + "If a date is not supplied then this will default to today.")
    LocalDate startDate;

    @Option(names = { "-w", "--week" }, description = "If specified, interpret the day as the start of a week.")
    boolean week;

    @Option(names = "--include-summary", description = "If specified, include client/project summary detail")
    boolean includeSummary;

    @Override
    public Integer call() throws Exception {

        /* Calculate what the end date should be */
        final LocalDate until;
        if (week) {
            until = startDate.plusDays(6);
        } else {
            until = startDate;
        }

        /* Construct the configuration object */
        final var config = new Configuration();
        config.setApiKey(apiKey);
        config.setDebug(debug);
        config.setEmail(emailAddress);
        config.setIncludeSummary(includeSummary);
        config.setSince(startDate);
        config.setUntil(until);
        config.setWorkspaceId(workspaceId);


        /* Create the API client */
        final var apiClient = new ApiClient(config);

        /* Load the detailed report and process */
        final var detailedReportData = apiClient.getDetailedReportData(1);
        final var timeReporter = new TimeReporter(config.isDebug());
        final var timeTotals = timeReporter.calculateTimeTotals(detailedReportData);

        /* Write out this information */
        System.out.println(String.format("# Totals for %s to %s", config.getSince().toString(), config.getUntil().toString()));
        System.out.println();
        System.out.println(String.format("* Booked time: %s", timeReporter.formatMillis(timeTotals.getBookedTime())));
        System.out.println(String.format("* Unbooked time: %s", timeReporter.formatMillis(timeTotals.getUnbookedTime())));
        System.out.println(String.format("* Break time: %s", timeReporter.formatMillis(timeTotals.getBreakTime())));
        System.out.println(String.format("* Total time (booked + unbooked): %s", timeReporter.formatMillis(timeTotals.getTimeCount())));
        System.out.println();

        if (includeSummary) {
            /* If we are to include the summary detail, load & process that too */
            final var summaryData = apiClient.getSummaryReportData();

            final var summaryReporter = new SummaryReporter(config.isDebug());
            final var processedSummary = summaryReporter.calculateSummaryTotals(summaryData, timeTotals);


            System.out.println("# Summary");
            System.out.println();

            processedSummary.forEach(s -> {
                System.out.println(String.format("* %s: %.2f%% (%s)", 
                    s.getName(), s.getPercentageOfTotalTime(), timeReporter.formatMillis(s.getBookedTime())));


                    if (!s.getSubgroupSummary().isEmpty()) {
                        s.getSubgroupSummary().forEach(g -> 
                          System.out.println(String.format("  * %s: %.2f%% (%s)", g.getName(), g.getPercentageOfTotalTime(), timeReporter.formatMillis(g.getBookedTime())))
                        );
                      }
                      System.out.println();
            });
        }

        

        

//       }).catch(error => {
//         System.out.println(chalk.red('Failed to load summary API response: ' + error));
//       })
//   }


// })
// .catch(error => {
//   System.out.println(chalk.red('Failed to load detailed API response: ' + error));
// })
        
        return 0;
    }

    /**
     * This is based on the example which implements Callable, so parsing, error
     * handling and handling user requests for usage help or version help can be
     * done with one line of code.
     * 
     * @param args the runtime arguments.
     */
    public static void main(String... args) {
        int exitCode = new CommandLine(new SummaryCommand()).execute(args);
        System.exit(exitCode);
    }

}
