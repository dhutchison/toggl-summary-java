package com.devwithimagination.toggl.core.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Base64;
import java.util.List;

import com.devwithimagination.toggl.core.model.Configuration;
import com.devwithimagination.toggl.core.model.api.DetailedReportItem;
import com.devwithimagination.toggl.core.model.api.DetailedReportResponse;
import com.devwithimagination.toggl.core.model.api.SummaryReportItem;
import com.devwithimagination.toggl.core.model.api.SummaryReportResponse;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class ApiClient {

    private final Configuration config;
    private final String apiReportBaseUrl;

    /**
     * Create a new {@link ApiClient}
     * @param config the configuration to use.
     */
    public ApiClient(final Configuration config) {
        this(config, "https://api.track.toggl.com/reports/api/v2/");
    }

    /**
     * Create a new {@link ApiClient}
     * @param config the configuration to use.
     * @param apiReportBaseUrl the base URL to use for API endpoints
     */
    public ApiClient(final Configuration config, final String apiReportBaseUrl) {
        this.config = config;
        this.apiReportBaseUrl = apiReportBaseUrl;
    }

    /**
     * Function for loading our detailed report data from the API. This handles
     * pagination by calling the API until all pages are loaded.
     * 
     * @param page   the page number to load. If this is undefined a default of 1
     *               will be used.
     * @return array of detailed report items loaded from the API.
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public List<DetailedReportItem> getDetailedReportData(final int page) 
        throws URISyntaxException, IOException, InterruptedException {

        /* Build up the query param string & URI */
        final String query = getBaseQueryParameters() + "&page=" + page;
        final URI uri = new URI(this.apiReportBaseUrl + "details?" + query);

        /* Build the HTTP request */
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .header("Accept", "application/json")
            .header("Accept-Language", "en-gb")
            .header("Authorization", basicAuth(config.getApiKey(), "api_token"))
            .build();

        /* Call the API */
        HttpResponse<String> response = HttpClient.newBuilder()
            .build()
            .send(request, BodyHandlers.ofString());

        final var responseData = parseResponseToObject(response, DetailedReportResponse.class);
        final var data = responseData.getData();

        /* Print out the total time as reported from the API */
        if (config.isDebug()) {
            System.out.println("Report page loaded " + page + 
                ", total booked time: " + 
                responseData.getGrandTotal());

            System.out.println(String.format(
                "Pagination details: items: %s, total_count: %s, per_page: %s",
                responseData.getData().size(),
                responseData.getTotalCount(), responseData.getPerPage()));
        }

        /**
         * If there are more pages, call the API again, otherwise return the data
         */
        if (responseData.getData() != null &&
                !responseData.getData().isEmpty() && 
                responseData.getData().size() == responseData.getPerPage()) {

            data.addAll(getDetailedReportData(page + 1));
        } 
        return data;
        
    }

    /**
     * Function for loading our summary report data from the API.
     * 
     * @return list of summary report items loaded from the API.
     * 
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public List<SummaryReportItem> getSummaryReportData()
            throws URISyntaxException, IOException, InterruptedException {

        /* Build the request URI */
        final String query = getBaseQueryParameters() + "&grouping=clients&subgrouping=projects";

        /* Build the HTTP request */
        final HttpRequest request = HttpRequest.newBuilder().uri(new URI(this.apiReportBaseUrl + "summary?" + query))
                .GET()
                .header("Accept", "application/json")
                .header("Accept-Language", "en-gb")
                .header("Authorization", basicAuth(config.getApiKey(), "api_token"))

                .build();

        /* Call the API */
        HttpResponse<String> response = HttpClient.newBuilder()
            .build()
            .send(request, BodyHandlers.ofString());

        final var responseData = parseResponseToObject(response, SummaryReportResponse.class);

        return responseData.getData();
    }

    /**
     * Helper method to parse a json response string into an object.
     * 
     * @param <T>        the type of object which will be created
     * @param response   the response from the HTTP call
     * @param objectType the class for the object to create
     * @return the created object
     * @throws IOException if the response cannot be parsed
     */
    private <T> T parseResponseToObject(final HttpResponse<String> response, Class<T> objectType) throws IOException {

        if (config.isDebug()) {
            System.out.println("Response body: " + response.body());
        }

        if (response.statusCode() != 200) {
            //TODO: add a test case for this
            throw new IOException("Failed to load data: " + response.body());
        }

        final T responseData;
        try (final Jsonb jsonb = JsonbBuilder.create()) {
            responseData = jsonb.fromJson(response.body(), objectType);
        } catch (Exception e) {
            throw new IOException("Failed to parse response", e);
        }

        return responseData;
    }

    String getBaseQueryParameters() {
        return String.format("workspace_id=%s&since=%s&until=%s&user_agent=%s", 
                config.getWorkspaceId(),
                config.getSince().toString(), 
                config.getUntil().toString(),
                config.getEmail());
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
