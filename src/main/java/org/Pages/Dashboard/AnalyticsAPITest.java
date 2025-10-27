package org.Pages.Dashboard;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This test suite:
 * 1. Logs in using username/password (sa / 123456)
 * 2. Retrieves a JWT token
 * 3. Calls all analytics APIs with Authorization header
 * 4. Fails any test if response code is not 200
 */
public class AnalyticsAPITest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    // === Analytics API Endpoints ===
    private static final String STATUS_COUNTS =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/status-counts?type=all&startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    private static final String SUMMARY =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/summary?startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    private static final String METRIC_VOLUME =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/metrics/time-series?groupBy=day&metric=volume&type=all&startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    private static final String METRIC_COUNT =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/metrics/time-series?groupBy=day&metric=count&type=all&startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    private static final String METRIC_SUCCESS_RATE =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/metrics/time-series?groupBy=day&metric=success_rate&type=all&startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    private static final String PROVIDERS =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/providers?type=all&startDate=2025-09-27T10:08:41.958Z&endDate=2025-10-27T10:08:41.958Z";

    /**
     * Logs in and returns JWT token as String.
     */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("❌ Login failed! HTTP " + code);
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();

        // Extract token from JSON
        String token = null;
        String body = response.toString();
        if (body.contains("\"token\"")) {
            token = body.split("\"token\"\\s*:\\s*\"")[1].split("\"")[0];
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ Could not extract token from login response: " + body);
        }

        System.out.println("✅ Logged in successfully, token acquired.");
        return token;
    }

    /**
     * Sends authorized GET request and returns HTTP status code.
     */
    private int getStatusCodeWithAuth(String urlStr, String token) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        if (status != 200) {
            System.err.println("❌ Failed request: " + urlStr);
            System.err.println("HTTP Status: " + status);
            if (conn.getErrorStream() != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            } else {
                System.err.println("⚠️ No error body returned from server.");
            }
        } else {
            System.out.println("✅ Success [" + urlStr + "] -> " + status);
        }

        conn.disconnect();
        return status;
    }

    // === Six API Test Cases ===

    @Test
    public void testStatusCountsAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(STATUS_COUNTS, token);
        Assert.assertEquals("Expected 200 for Status Counts API", 200, status);
    }

    @Test
    public void testSummaryAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(SUMMARY, token);
        Assert.assertEquals("Expected 200 for Summary API", 200, status);
    }

    @Test
    public void testMetricVolumeAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(METRIC_VOLUME, token);
        Assert.assertEquals("Expected 200 for Metric Volume API", 200, status);
    }

    @Test
    public void testMetricCountAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(METRIC_COUNT, token);
        Assert.assertEquals("Expected 200 for Metric Count API", 200, status);
    }

    @Test
    public void testMetricSuccessRateAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(METRIC_SUCCESS_RATE, token);
        Assert.assertEquals("Expected 200 for Metric Success Rate API", 200, status);
    }

    @Test
    public void testProvidersAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(PROVIDERS, token);
        Assert.assertEquals("Expected 200 for Providers API", 200, status);
    }
}
