package com.devwithimagination.toggl.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import com.devwithimagination.toggl.core.model.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for aspects of ApiClient.
 */
class ApiClientTest {

    private static final String EMAIL = "test@example.com";
    private static final String WORKSPACE_ID = "abcd";
    private static final String START_DATE = "2021-07-11";
    private static final String END_DATE = "2021-07-16";
    private static final String API_KEY = "00001111222233334444555566667777";

    private ApiClient testInstance;
    private Configuration config;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupMockApi() {
        wireMockServer = new WireMockServer(
            WireMockConfiguration.wireMockConfig().port(8089));

        wireMockServer.stubFor(any(urlPathEqualTo("/summary"))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("Accept-Language", equalTo("en-gb"))
            .withQueryParam("workspace_id", equalTo(WORKSPACE_ID))
            .withQueryParam("since", equalTo(START_DATE))
            .withQueryParam("until", equalTo(END_DATE))
            .withQueryParam("user_agent", equalTo(EMAIL))
            .withBasicAuth(API_KEY, "api_token")
            .willReturn(aResponse().withStatus(200).withBodyFile("summary.json")));

        wireMockServer.stubFor(any(urlPathEqualTo("/details"))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("Accept-Language", equalTo("en-gb"))
            .withQueryParam("workspace_id", equalTo(WORKSPACE_ID))
            .withQueryParam("page", equalTo("1"))
            .withQueryParam("since", equalTo(START_DATE))
            .withQueryParam("until", equalTo(END_DATE))
            .withQueryParam("user_agent", equalTo(EMAIL))
            .withBasicAuth(API_KEY, "api_token")
            .willReturn(aResponse().withStatus(200).withBodyFile("details.json")));

        wireMockServer.stubFor(any(urlPathEqualTo("/details"))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("Accept-Language", equalTo("en-gb"))
            .withQueryParam("workspace_id", equalTo(WORKSPACE_ID))
            .withQueryParam("page", equalTo("2"))
            .withQueryParam("since", equalTo(START_DATE))
            .withQueryParam("until", equalTo(END_DATE))
            .withQueryParam("user_agent", equalTo(EMAIL))
            .withBasicAuth(API_KEY, "api_token")
            .willReturn(aResponse().withStatus(200).withBodyFile("details2.json")));

        wireMockServer.stubFor(any(urlPathEqualTo("/details"))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("Accept-Language", equalTo("en-gb"))
            .withQueryParam("workspace_id", equalTo(WORKSPACE_ID))
            .withQueryParam("page", equalTo("3"))
            .withQueryParam("since", equalTo(START_DATE))
            .withQueryParam("until", equalTo(END_DATE))
            .withQueryParam("user_agent", equalTo(EMAIL))
            .withBasicAuth(API_KEY, "api_token")
            .willReturn(aResponse().withStatus(200).withBodyFile("details3.json")));
        

        wireMockServer.start();
    }

    @BeforeEach
    void setupTestConfiguration() {

        config = new Configuration();
        config.setApiKey(API_KEY);
        config.setDebug(true);
        config.setEmail(EMAIL);
        config.setSince(LocalDate.parse(START_DATE));
        config.setUntil(LocalDate.parse(END_DATE));
        config.setWorkspaceId(WORKSPACE_ID);


        this.testInstance = new ApiClient(config, "http://localhost:8089/");
        
    }

    @AfterAll
    static void stopMockApi() {
        wireMockServer.stop();
    }

    @Test
    void testBaseQueryStringBuild() {


        final var expected = String.format("workspace_id=%s&since=%s&until=%s&user_agent=%s", WORKSPACE_ID, START_DATE, END_DATE, EMAIL);

        final var actual = testInstance.getBaseQueryParameters();

        assertEquals(expected, actual);

    }

    @Test
    void testLoadDetailedReportData() throws Exception {

        var actual = testInstance.getDetailedReportData(1);
        assertEquals(2, actual.size());

        //TODO: add in additional assertions
    }

    @Test
    void testLoadSummaryReportData() throws Exception {

        final var actual = testInstance.getSummaryReportData();

        assertEquals(5, actual.size());

        //TODO: add in additional assertions
    }
    
}
