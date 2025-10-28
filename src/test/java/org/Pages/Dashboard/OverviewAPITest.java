package org.Pages.Dashboard;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Overview API Test Suite
 * -----------------------
 * 1. Logs in using username/password (sa / 123456)
 * 2. Retrieves JWT token
 * 3. Calls all Overview-related APIs
 * 4. Verifies HTTP 200 for each endpoint
 */
public class OverviewAPITest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    // === Overview API Endpoints ===
    private static final String LOCALE_FILE =
            "https://pay-web-ntwda.ondigitalocean.app/assets/locale/en-US.json";

    private static final String STATUS_COUNTS =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/status-counts"
                    + "?type=all&startDate=2025-10-20T10:11:52.892Z&endDate=2025-10-27T10:11:52.892Z";

    private static final String CURRENCIES_SUCCESS =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/currencies/successful-transactions"
                    + "?type=all&startDate=2025-10-20T10:11:52.892Z&endDate=2025-10-27T10:11:52.892Z";

    private static final String CUSTOMERS_UNIQUE =
            "https://pay-app-oilbv.ondigitalocean.app/api/v1/analytics/customers/unique"
                    + "?type=all&startDate=2025-10-20T10:11:52.892Z&endDate=2025-10-27T10:11:52.892Z";

    /**
     * Logs in and retrieves JWT token.
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

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();

        String body = response.toString();
        String token = null;
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
     * Sends an authorized GET request and returns HTTP status.
     */
    private int getStatusCodeWithAuth(String urlStr, String token, boolean isPublic) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (!isPublic) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setRequestProperty("Accept", "application/json");

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

    // === TEST CASES ===

    @Test
    public void testLocaleFile() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(LOCALE_FILE, token, true);
        Assert.assertEquals("Expected 200 for Locale JSON file", 200, status);
    }

    @Test
    public void testStatusCountsAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(STATUS_COUNTS, token, false);
        Assert.assertEquals("Expected 200 for Status Counts API", 200, status);
    }

    @Test
    public void testCurrenciesSuccessAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(CURRENCIES_SUCCESS, token, false);
        Assert.assertEquals("Expected 200 for Currencies Success API", 200, status);
    }

    @Test
    public void testCustomersUniqueAPI() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(CUSTOMERS_UNIQUE, token, false);
        Assert.assertEquals("Expected 200 for Customers Unique API", 200, status);
    }

    @Test
    public void testCurrenciesSuccessAPIRepeat() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(CURRENCIES_SUCCESS, token, false);
        Assert.assertEquals("Expected 200 for Currencies Success API (repeat)", 200, status);
    }

    @Test
    public void testCustomersUniqueAPIRepeat() throws IOException {
        String token = loginAndGetToken();
        int status = getStatusCodeWithAuth(CUSTOMERS_UNIQUE, token, false);
        Assert.assertEquals("Expected 200 for Customers Unique API (repeat)", 200, status);
    }
}
