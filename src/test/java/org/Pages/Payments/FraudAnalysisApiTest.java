package org.Pages.Payments;

import org.junit.Assert;
import org.junit.Test;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Fraud Analysis API Tests (Fixed Version)
 * -------------------------------------------
 * - Logs in as (sa / 123456)
 * - Tests multiple fraud analysis endpoints
 */
public class FraudAnalysisApiTest {

    private static final String BASE_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1";
    private static final String LOGIN_URL = BASE_URL + "/auth/login";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Login to get JWT token */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("❌ Login failed! HTTP " + conn.getResponseCode());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }
        conn.disconnect();

        String token = null;
        if (response.toString().contains("\"token\"")) {
            token = response.toString().split("\"token\"\\s*:\\s*\"")[1].split("\"")[0];
        }

        if (token == null) throw new RuntimeException("❌ Token not found!");
        System.out.println("✅ Logged in successfully, token acquired.");
        return token;
    }

    /** 🌐 Generic GET request with token */
    private int sendGetRequest(String urlStr, String token) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        int code = conn.getResponseCode();

        if (code == 200) {
            System.out.println("✅ Success: " + urlStr + " → " + code);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String firstLine = br.readLine();
                if (firstLine != null)
                    System.out.println("🔹 Response preview: " + firstLine.substring(0, Math.min(150, firstLine.length())));
            }
        } else {
            System.err.println("❌ Request failed: " + urlStr);
            System.err.println("HTTP " + code);
            if (conn.getErrorStream() != null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) System.err.println(line);
                }
            }
        }

        conn.disconnect();
        return code;
    }

    // === TEST CASES ===

    /** 🧪 1. Test fraud analyses list */
    @Test
    public void testFraudAnalysesList() throws IOException {
        String token = loginAndGetToken();
        String url = BASE_URL + "/fraud-analyses?page=0&size=20&sort=createdAt,desc";
        int status = sendGetRequest(url, token);
        Assert.assertEquals("Expected 200 for fraud analyses list", 200, status);
    }

    /** 🧪 2. Test fraud statistics */
    @Test
    public void testFraudStatistics() throws IOException {
        String token = loginAndGetToken();
        String url = BASE_URL + "/fraud-analyses/statistics";
        int status = sendGetRequest(url, token);
        Assert.assertEquals("Expected 200 for fraud statistics", 200, status);
    }

    /** 🧪 3. Test high-risk visitors */
    @Test
    public void testHighRiskVisitors() throws IOException {
        String token = loginAndGetToken();
        String url = BASE_URL + "/fraud-analyses/high-risk-visitors?minRiskScore=10&page=0&size=20";
        int status = sendGetRequest(url, token);
        Assert.assertEquals("Expected 200 for high-risk visitors", 200, status);
    }
}
