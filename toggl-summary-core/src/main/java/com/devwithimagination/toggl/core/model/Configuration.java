package com.devwithimagination.toggl.core.model;

import java.time.LocalDate;

/**
 * Object holding configuration values. 
 */
public class Configuration {

    /**
     * boolean for if debug logging should be used (true) or not (false)
     */
    private boolean debug;
    /**
     * The API configuration for the Toggl API
     */
    // apiConfig: AxiosRequestConfig,
    /*
    const apiConfig: AxiosRequestConfig = {
        auth: {
            username: program.opts().apiKey,
            password: 'api_token'
        },
        headers: {
          'Accept': 'application/json',
          'Accept-Language': 'en-gb',
        },
        params: {
            page: 1,
            user_agent: program.opts().email,
            workspace_id: program.opts().workspaceId,
            since: since.toString(),
            until: until.toString()
        }
      };
      */

    private String apiKey;

    private String email;

    private LocalDate since;

    private LocalDate until;

    private String workspaceId;

    /**
     * Boolean for if per client/project summary information should be included or not. 
     */
    private boolean includeSummary;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getSince() {
        return since;
    }

    public void setSince(LocalDate since) {
        this.since = since;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
    
}
