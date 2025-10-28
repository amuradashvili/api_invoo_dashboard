package org.Pages.Custumers;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Risk Management Dashboard API Tests
 * --------------------------------------
 * - Logs in (sa / 123456)
 * - Gets JWT token
 * - Tests:
 *   1. GET /assets/locale/en-US.json
 *   2. GET /api/v1/customers/analytics
 *   3. GET /api/v1/customers?riskLevel=HIGH...
 *   4. GET /api/v1/customers?riskLevel=CRITICAL...
 * - Verifies HTTP 200 responses
 */
public class RiskManagementDashboardApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String LOCALE_URL = "https://pay-web-ntwda.ondigitalocean.app/assets/locale/en-US.json";
    private static final String CUSTOMERS_ANALYTICS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/customers/analytics";
    private static final String HIGH_RISK_CUSTOMERS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/customers?riskLevel=HIGH&size=10&sortBy=riskScore&sortDirection=DESC";
    private static final String CRITICAL_RISK_CUSTOMERS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/customers?riskLevel=CRITICAL&size=10&sortBy=riskScore&sortDirection=DESC";

    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Logs in and retrieves JWT token */
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

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("❌ Login failed! HTTP " + conn.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();

        String response = sb.toString();
        String token = null;
        if (response.contains("\"token\"")) {
            token = response.split("\"token\"\\s*:\\s*\"")[1].split("\"")[0];
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ Token not found in login response: " + response);
        }

        System.out.println("✅ Logged in successfully, token acquired.");
        return token;
    }

    /** 🌐 Generic GET request sender */
    private int sendGetRequest(String urlStr, String token, boolean withAuth) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (withAuth && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();

        if (code != 200) {
            System.err.println("❌ Request failed: " + urlStr);
            System.err.println("HTTP Status: " + code);
            if (conn.getErrorStream() != null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) System.err.println(line);
                }
            }
        } else {
            System.out.println("✅ Success: " + urlStr + " → " + code);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String firstLine = br.readLine();
                if (firstLine != null)
                    System.out.println("🔹 Response preview: " + firstLine.substring(0, Math.min(150, firstLine.length())));
            }
        }

        conn.disconnect();
        return code;
    }

    // === TEST CASES ===

    /** 🌍 Locale file check (no auth) */
    @Test
    public void testLocaleAPI() throws IOException {
        int status = sendGetRequest(LOCALE_URL, null, false);
        Assert.assertEquals("Expected 200 for Locale API", 200, status);
    }

    /** 📈 Customer analytics (auth) */
    @Test
    public void testCustomerAnalyticsAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendGetRequest(CUSTOMERS_ANALYTICS_URL, token, true);
        Assert.assertEquals("Expected 200 for Customers Analytics API", 200, status);
    }

    /** ⚠️ High Risk Customers (auth) */
    @Test
    public void testHighRiskCustomersAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendGetRequest(HIGH_RISK_CUSTOMERS_URL, token, true);
        Assert.assertEquals("Expected 200 for High Risk Customers API", 200, status);
    }

    /** 🚨 Critical Risk Customers (auth) */
    @Test
    public void testCriticalRiskCustomersAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendGetRequest(CRITICAL_RISK_CUSTOMERS_URL, token, true);
        Assert.assertEquals("Expected 200 for Critical Risk Customers API", 200, status);
    }
}
