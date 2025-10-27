package org.Pages.Payments;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Fraud Analysis API Tests
 * ----------------------------
 * - Logs in (sa / 123456)
 * - Calls Fraud Analytics endpoints
 */
public class FraudAnalysisApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String FRAUD_ANALYSIS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/fraud/analysis";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Login to get token */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
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

    /** 🌐 Generic request sender */
    private int sendRequest(String urlStr, String token) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

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
            System.out.println("✅ Fraud Analysis API success: " + code);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String firstLine = br.readLine();
                if (firstLine != null)
                    System.out.println("🔹 Response preview: " + firstLine.substring(0, Math.min(150, firstLine.length())));
            }
        }

        conn.disconnect();
        return code;
    }

    // === TEST CASE ===
    @Test
    public void testFraudAnalysisAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendRequest(FRAUD_ANALYSIS_URL, token);
        Assert.assertEquals("Expected 200 for Fraud Analysis API", 200, status);
    }
}
