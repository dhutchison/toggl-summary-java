package com.devwithimagination.toggl.core.model.api;

/**
 * The title of a data item which is returned by the summary report API call. 
 */
public class SummaryReportTitle {
    
    private String client;
    private String project;
    private String user;

    public static SummaryReportTitle forClient(final String client) {
        final var instance = new SummaryReportTitle();
        instance.setClient(client);

        return instance;
    }

    public static SummaryReportTitle forProject(final String project) {
        final var instance = new SummaryReportTitle();
        instance.setProject(project);

        return instance;
    }

    public static SummaryReportTitle forUser(final String user) {
        final var instance = new SummaryReportTitle();
        instance.setUser(user);

        return instance;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
